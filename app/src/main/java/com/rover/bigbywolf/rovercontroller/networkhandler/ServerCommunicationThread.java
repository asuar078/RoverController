package com.rover.bigbywolf.rovercontroller.networkhandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.os.Handler;
import android.util.Log;

import com.rover.bigbywolf.rovercontroller.threadwrapper.ThreadWrapper;

public class ServerCommunicationThread extends ThreadWrapper {

    public final static int TCP_SERVER_PORT = 13337;
    static private final String TAG = "ServerComThread";

    private ConnectionEventListener mListener;

//    private ArrayList<String> mMessages = new ArrayList<>();

    ConcurrentLinkedQueue<String>
            mMessages = new ConcurrentLinkedQueue<>();

    private String mServer;
    private int mPortNum;

    private final static int MAX_ERROR_COUNT = 5;
    private Socket s = null;

    public ServerCommunicationThread(String server, int portNum) {
        this.mServer = server;
        this.mPortNum = portNum;

        Log.i(TAG, "starting server on " + server + ":" + portNum);
    }

    public void registerConnectionEventListener(ConnectionEventListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {

        Log.i(TAG, "top of run");
        int errorCount = 0;
        boolean sentConnectedEvent = false;
        s = null;

        while (!mQuit) {
            s = null;
            try {
                Log.i(TAG, "Attempting to connect to socket");
                s = new Socket(mServer, mPortNum);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                if(!sentConnectedEvent){
                    mListener.connectEvent();
                    sentConnectedEvent = true;
                }

                while (!mQuit) {
                    String message;

                    // Wait for message
                    synchronized (mMessages) {
                        while (mMessages.isEmpty()) {
                            try {
                                mMessages.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        // Get message and remove from the list
                        message = mMessages.poll();
//                        message = mMessages.get(0);
//                        mMessages.remove(0);
                    }

                    if(s != null){
                        Log.i(TAG, "sending message");
                        //send output msg
                        String outMsg = message;
                        out.write(outMsg);
                        out.flush();
                    }
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "Unknownhost exception");
                Log.e(TAG, e.toString());
                e.printStackTrace();
            } catch (IOException e) {
//                e.printStackTrace();
                Log.e(TAG, "IOException exception");
                Log.e(TAG, e.toString());
                e.printStackTrace();
            } finally {
                errorCount++;
                if (errorCount > MAX_ERROR_COUNT) {
                    Log.e(TAG,"max error hit, closing thread");
                    closeConnection();
                    if(sentConnectedEvent){
                        mListener.disconnectEvent();
                        sentConnectedEvent = false;
                    }
                    return;
                }
                sleep(200);
                continue;
            }
        }

        // just to be sure connection is closed
        closeConnection();
        if(sentConnectedEvent){
            mListener.disconnectEvent();
            sentConnectedEvent = false;
        }
    }

    public void closeConnection() {
        mQuit = true;
        //close connection
        if (s != null) {
            try {
                synchronized (mMessages) {
                    mMessages.add("");
                    mMessages.notify();
                }
                Log.i(TAG, "closing socket");
                s.close();
                s = null;

            } catch (IOException e) {
                Log.e(TAG, "error closing socket");
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    public void send(String message) {
        if(s != null && message != null) {
            synchronized (mMessages) {
                mMessages.add(message);
                mMessages.notify();
            }
        }
    }

    public boolean isConnectionOpen() {
        return s != null;
    }

}