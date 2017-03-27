package com.example.simo.bluetoothtesti;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.OutputStream;
import java.util.Set;
public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ConnectThread cnt;
    MyBluetoothService mService;
    OutputStream oStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((TouchView) findViewById(R.id.TouchView)).setTextView((TextView) findViewById(R.id.textView2), (TextView) findViewById(R.id.textView3));


        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }
    }

    public void queryBtPaired(View view) {

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        final ViewGroup constraintLayout = (ViewGroup) findViewById(R.id.ConstraintLayout);

        if(findViewById(R.id.ScrollView) == null) {
            getLayoutInflater().inflate(R.layout.devicelist, constraintLayout, true);
            final LinearLayout list = (LinearLayout) findViewById(R.id.LinearLayout);

            String string;

            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    final BluetoothDevice dev = device;

                    string = device.getName(); // Device Name
                    string += "\n";
                    string += device.getAddress(); // MAC address

                    Button newButton = new Button(this);
                    newButton.setText(string);
                    newButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            cnt = new ConnectThread(dev, (TextView) findViewById(R.id.textView));
                            cnt.run();

                            mService = cnt.getBluetoothService();
                            TouchView touchView = (TouchView) findViewById(R.id.TouchView);
                            touchView.setService(mService);
                            oStream = mService.getOutputStream();
                            constraintLayout.removeView(findViewById(R.id.ScrollView));
                        }
                    });

                    list.addView(newButton);
                }
            } else {

                TextView textView = (TextView) findViewById(R.id.textView);
                textView.setText(R.string.noPairedDevices);

            }
        }
    }

    public void disconnectButton(View view) {
        if(cnt != null) {
            mService.cancel();
        }
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(R.string.text_not_connected);
    }

    public void closeButtonClicked(View view) {
        ((ViewGroup) findViewById(R.id.ConstraintLayout)).removeView(findViewById(R.id.ScrollView));
    }
}