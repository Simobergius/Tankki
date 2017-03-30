package com.example.simo.bluetoothtesti;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.TextView;

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
    private static int buffer = 50;

    MyBluetoothService(BluetoothSocket socket) {
        cnt = new ConnectedThread(socket);
    }

    OutputStream getOutputStream() {

        return cnt.mmOutStream;
    }

    void cancel() {
        cnt.cancel();
    }

    void setValues(float x, float y, float low, float high) {
        cnt.x = x;
        cnt.y = y;
        cnt.fromLow = low + buffer;
        cnt.fromHigh = high - buffer;
    }

    void setTextView(TextView tw, TextView tw2) {
        cnt.textView = tw;
        cnt.textView2 = tw2;
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;
        private TextView textView, textView2;
        Timer timer;
        TimerTask timerTask;
        float x = 0, y = 0;
        float fromLow = -1, fromHigh = 1;

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
                    int vasen = mapTouchToTracks(-x, y);
                    int oikea = mapTouchToTracks(x, y);
                    String str = "Oikea: " + Integer.toString(oikea) + " Vasen: " + Integer.toString(vasen);
                    //textView.setText(str);
                    int[] cmd = { 1, 2, 3, (byte) oikea, (byte) vasen, 4 };
                    for (int i = 0; i < 6; i++) {
                        write((byte) cmd[i]);
                    }

                }
            };
        }

        private void stopTimerTask() {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }

        private int mapTouchToTracks(float x, float y) {
            float val = 0;
            float toLow = 0, toHigh = 254;

            if (x > 0){
                if (x - buffer < 0) {
                    x = 0;
                } else x -= buffer;
            }else{
                if (x + buffer > 0) {
                    x = 0;
                } else x += buffer;
            }

            if (y > 0){
                if (y - buffer < 0)
                    y = 0;
                else y -= buffer;
            }else{
                if (y + buffer > 0)
                    y = 0;
                else y += buffer;
            }

            if (y <= 0) {
                if (x > 0) {
                    val = - y - x;
                } else if (x <= 0) {
                    if (Math.abs(x) >= Math.abs(y)) {
                        val = Math.abs(x);
                    } else if (Math.abs(x) < Math.abs(y)) {
                        val = Math.abs(y);
                    }
                }
            }else if (y > 0) {
                if (x > 0) {
                    if (x > y ) {
                        val = y - x;
                    } else if (x < y) {
                        val = x - y;
                    }
                } else if (x <= 0) {
                    if (x <= y) {
                        val = -y;
                    } else if (x > y) {
                        val = -(x + 2 * y);
                    }
                }
            }

            val = (val - fromLow) * (toHigh-toLow) / (fromHigh-fromLow) + toLow;

            return Math.round(val);
        }
    }
}
