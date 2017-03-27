package com.example.simo.bluetoothtesti;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by Simo on 23.3.2017.
 */

class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final TextView textView;
    private MyBluetoothService mService;

    ConnectThread(BluetoothDevice device, TextView errLog) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;
        textView = errLog;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
            textView.setText(R.string.socketCreateFailed);
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
            textView.setText("Connected to " + mmDevice.getName());
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.

            textView.setText("Could not connect to " + mmDevice.getName());
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
                textView.setText(R.string.clientSocketConnectFailed);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        mService = new MyBluetoothService(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
            textView.setText(R.string.clientSocketCloseFailed);
        }
    }

    MyBluetoothService getBluetoothService() {

        return mService;
    }
}