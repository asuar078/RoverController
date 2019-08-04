package com.rover.bigbywolf.rovercontroller;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rover.bigbywolf.rovercontroller.messagetypes.FlightControlMessage;
import com.rover.bigbywolf.rovercontroller.messagetypes.ControlMessage;
import com.rover.bigbywolf.rovercontroller.networkhandler.ConnectionEventListener;
import com.rover.bigbywolf.rovercontroller.networkhandler.ServerCommunicationThread;
import com.rover.bigbywolf.rovercontroller.threadwrapper.ThreadWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import io.github.controlwear.virtual.joystick.android.JoystickView;

enum DroneConnectionStatus {
    DISCONNECT, CONNECTED
}

public class MainActivity extends AppCompatActivity {

    static private final String TAG = "Main";

//    private String ipAddr = "10.0.2.2";
    private String ipAddr = "192.168.1.201";
    private int portNum = 1337;

    private int updatePeriod_ms = 1000;
    private int leftAngle = 0;
    private int leftStrength = 0;

    private int rightAngle = 0;
    private int rightStrength = 0;

    private DroneConnectionStatus mStatus = DroneConnectionStatus.DISCONNECT;

    private Button connectBtn;
    private TextView connectTextView;

    Timer timer;
    TimerTask timerTask;

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    private ServerCommunicationThread comThread = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        setContentView(R.layout.activity_main);

        //This will make your app run only in Landscape mode!
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        connectBtn = (Button)findViewById(R.id.btn_network);
        connectTextView = (TextView)findViewById(R.id.networkTextView);

        // start joy sticks
        JoystickView joystickLeft = (JoystickView) findViewById(R.id.joystickViewLeft);
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                leftAngle = angle;
                leftStrength = strength;
            }
        });

        JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickViewRight);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                rightAngle = angle;
                rightStrength = strength;
            }
        });

        // start timer to update joysticks
        startTimer();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //onResume we start our timer so it can start when the app comes from the background
        startTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stoptimertask();
    }

    public String writeJSON() {
        try {

            Log.i(TAG, "left angle: " + leftAngle + ", left strength: " + leftStrength + "\nright angle: " + rightAngle + ", right strength: " + rightStrength);

            return FlightControlMessage.joyStickToFlightControl(leftAngle, leftStrength,
                    rightAngle, rightStrength);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        if(comThread != null && mStatus == DroneConnectionStatus.CONNECTED) {
                            if (comThread.isConnectionOpen()) {
                                Log.i(TAG, "sending message");
                                comThread.send(writeJSON());
                            }
                        }
                    }
                });
            }
        };
    }

    public void updateNetwork(final View view){

        if(mStatus == DroneConnectionStatus.CONNECTED) {
            Log.i(TAG, "disconnecting from drone");

            if((comThread.isConnectionOpen()) && comThread != null){
                comThread.closeConnection();
                comThread.quit();
            }

            mStatus = DroneConnectionStatus.DISCONNECT;
            connectBtn.setBackgroundResource(R.drawable.disconnected);
            connectTextView.setText("CONNECT");
            return;
        }
        else if (mStatus == DroneConnectionStatus.DISCONNECT) {
            Log.i(TAG, "connecting to drone");
//            view.setEnabled(false);

            comThread = new ServerCommunicationThread(ipAddr, portNum);
            comThread.registerConnectionEventListener(new ConnectionEventListener() {
                @Override
                public void connectEvent() {
//                    view.setEnabled(true);
                    mStatus = DroneConnectionStatus.CONNECTED;
                    connectBtn.setBackgroundResource(R.drawable.connected);
                    connectTextView.setText("DISCONNECT");
                }

                @Override
                public void disconnectEvent() {

                    new Handler(Looper.getMainLooper()).post(new Runnable(){
                        @Override
                        public void run() {
    //                    view.setEnabled(true);
                            mStatus = DroneConnectionStatus.DISCONNECT;
                            connectBtn.setBackgroundResource(R.drawable.disconnected);
                            connectTextView.setText("CONNECT");
                            comThread.quit();
                        }
                    });
                }
            });
            comThread.start();

        }
    }


    public void armBtn(final View view){


        if(comThread != null && mStatus == DroneConnectionStatus.CONNECTED) {
            if (comThread.isConnectionOpen()) {
                Log.i(TAG, "sending arm message");
                try {
                    comThread.send(ControlMessage.sendArmMessage());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
//        timer.schedule(timerTask, 5000, 10000);
        timer.schedule(timerTask, 100, updatePeriod_ms);
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


}
