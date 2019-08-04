package com.rover.bigbywolf.rovercontroller;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class Sender {

//    final ExecutorService pool = Executors.newFixedThreadPool(10);
    private boolean inUse = false;
    private int mTimeOutPeriod = 100;

    Sender(int timeOutPeriod){
        mTimeOutPeriod = timeOutPeriod;
    }

    public void send(String serverAddress, int serverPort, byte[] bytes) {

//        if(inUse){
//            return;
//        }
//        inUse = true;
//        pool.execute(new UDPSend(serverAddress, serverPort, bytes));
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<?> future = pool.submit(new UDPSend(serverAddress, serverPort, bytes));
        try {
            future.get(mTimeOutPeriod-10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        pool.shutdown();
//        inUse = true;
    }
}

class UDPSend implements Runnable {

        private byte[] message;
        String serverAddress;
        int serverPort;

        String TAG = "UDP";

        UDPSend(String serverAddress, int serverPort, byte[] message) {
            this.message = message;
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            try {
                DatagramSocket datagramSocket = new DatagramSocket();

                InetAddress receiverAddress = InetAddress.getByName(serverAddress);

                DatagramPacket packet = new DatagramPacket(
                        message,
                        message.length,
                        receiverAddress,
                        serverPort
                );

                datagramSocket.send(packet);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

}
