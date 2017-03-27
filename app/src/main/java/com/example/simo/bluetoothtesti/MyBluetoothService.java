package com.example.simo.bluetoothtesti;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Simo on 24.3.2017.
 */

class MyBluetoothService {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private ConnectedThread cnt;

    MyBluetoothService(BluetoothSocket socket) {
        cnt = new ConnectedThread(socket);
    }

    OutputStream getOutputStream() {

        return cnt.mmOutStream;
    }

    void cancel() {
        cnt.cancel();
    }

    void setValues(int right, int left) {
        cnt.right = right;
        cnt.left = left;
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;
        Timer timer;
        TimerTask timerTask;
        int right = 7, left = 7;

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmOutStream = tmpOut;
            startTimer();
        }

        // Call this from the main activity to send data to the remote device.
        void write(byte bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        // Call this method from the main activity to shut down the connection.
        void cancel() {
            try {
                mmSocket.close();
                stopTimerTask();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

        private void startTimer() {
            timer = new Timer();

            initializeTimerTask();

            timer.schedule(timerTask, 100, 50);
        }

        private void initializeTimerTask() {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    int cmd = right * 15 + left;
                    write((byte) cmd);
                }
            };
        }

        private void stopTimerTask() {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }
}
