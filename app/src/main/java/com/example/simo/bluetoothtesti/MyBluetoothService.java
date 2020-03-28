package com.example.simo.bluetoothtesti;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by Simo on 24.3.2017.
 */

class MyBluetoothService {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private ConnectedThread cnt;
    private static int buffer = 30;

    MyBluetoothService(BluetoothSocket socket) {
        cnt = new ConnectedThread(socket);
    }

    void cancel() {
        cnt.cancel();
    }

    void setMovementValues(double x, double y, float highY) {
        cnt.x = x;
        cnt.y = y;
        cnt.fromLow = -highY;
        cnt.fromHigh = highY;
    }

    void setTurretValues(double x, double y, float highX, float highY) {
        cnt.elevation = y;
        cnt.rotation = x;
        cnt.maxElevation = highY;
        cnt.maxRotation = highX;
    }

    void setLaserButton(ToggleButton lb) {
        cnt.laserButton = lb;
    }

    void setAmpu(boolean val) {
        if(val)
            cnt.ampu = 2;
        else
            cnt.ampu = 0;
    }

    void setKonsu(boolean val) {
        if(val)
            cnt.konsu = 2;
        else
            cnt.konsu = 0;
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;
        Timer timer;
        TimerTask timerTask;
        double x = 0, y = 0;
        float fromLow = -1, fromHigh = 1;
        double elevation = 0;
        double rotation = 0;
        float maxElevation = 1;
        float maxRotation = 1;
        int konsu = 0, laser = 0, ampu = 0;
        ToggleButton laserButton;

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
                    //Right track = f(x, y)
                    //Left track = f(-x, y)
                    int vasen = mapTouchToTracks(-x, y);
                    int oikea = mapTouchToTracks(x, y);
                    int elevationFinal = mapElevation();
                    int rotationFinal = mapRotation();

                    if(laserButton.isChecked()){
                        laser = 2;
                    }else{
                        laser = 0;
                    }

                    byte[] cmd = { 1, (byte) 255, (byte) oikea, (byte) vasen, (byte) elevationFinal, (byte) rotationFinal, (byte) ampu, (byte) laser, (byte) konsu};
                    for (byte aCmd : cmd) {
                        write(aCmd);
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

        private int mapTouchToTracks(double x, double y) {
            double val = 0;
            double toLow = 0, toHigh = 254;

            // the delinearise function makes the x-coordinate of the touch not linearly relative to the amount of turning
            // so the turn-response of the tank is not linear, but still makes use of the full degree of movement
            // so a touch on the max still causes max turning, but the amount in between is adjusted to theoretically turn more naturally
            if(x >= 0)
                x *= delinearise(x / fromHigh, 30);
            else
                x *= delinearise(-(x / fromHigh), 30);

            // The following lines implement a deadzone
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

            // Calculate the track speed factor for the position of the touch

            if (y <= 0) {
                if (x > 0) {
                    val = - y - x;
                } else {
                    if (Math.abs(x) >= Math.abs(y)) {
                        val = Math.abs(x);
                    } else if (Math.abs(x) < Math.abs(y)) {
                        val = Math.abs(y);
                    }
                }
            } else {
                if (x > 0) {
                    if (x > y ) {
                        val = y - x;
                    } else {
                        val = x - y;
                    }
                } else {
                    if (Math.abs(x) < Math.abs(y)) {
                        val = -y;
                    } else {
                        val = -(x + 2 * y);
                    }
                }
            }

            // Map the value from the original range to the desired range
            val = (val - fromLow) * (toHigh-toLow) / (fromHigh-fromLow) + toLow;

            return (int)Math.round(val);
        }

        private double delinearise(double x, int y)
        // return a number that is taken from a segment of a circle of radius y, where the circle periphery goes through the origin and x = 1, y = 1
        // y -value means the amount of linearity, x is the ratio of current number to the max value of that number
        // infinity y is the same as a perfect line, 0 y is the same as a circle of radius 1
        {
            double offsetX;
            double offsetY;

            if (y > 0)
            {
                offsetX = ((sqrt(y * 2 - 1) - 1) / 2);
                offsetY = ((sqrt(y * 2 - 1) / 2) + 0.5);
                return -(sqrt(y - (pow(x + offsetX, 2) ) ) ) + offsetY ;
            }

            if (y < 0)
            {
                y = -y;
                offsetX = (-(sqrt(y * 2 - 1) + 1 ) / 2);
                offsetY = (-(sqrt(y * 2 - 1) - 1 ) / 2);
                return sqrt(y - (pow(x + offsetX, 2))) + offsetY;

            }

            return x;

        }

        private int mapRotation() {
            float fromHighRot = maxRotation;
            float fromLowRot = -maxRotation;
            double toLow = 0, toHigh = 254;
            double val = rotation;

            val = (val - fromLowRot) * (toHigh-toLow) / (fromHighRot-fromLowRot) + toLow;

            return (int)Math.round(val);
        }

        private int mapElevation() {
            float fromHighEle = maxElevation;
            float fromLowEle = -maxElevation;
            double toLow = 0, toHigh = 254;
            double val = elevation;

            val = (val - fromLowEle) * (toHigh-toLow) / (fromHighEle-fromLowEle) + toLow;

            return (int)Math.round(val);
        }

    }
}
