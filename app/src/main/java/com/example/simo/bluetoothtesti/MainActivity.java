package com.example.simo.bluetoothtesti;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Set;
public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ConnectThread cnt;
    MyBluetoothService mService;
    TouchView touchViewMovement, touchViewTurret;
    Button fireButton, mgButton;
    ToggleButton laserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }

        touchViewMovement = (TouchView) findViewById(R.id.TouchViewMovement);
        touchViewTurret = (TouchView) findViewById(R.id.TouchViewTurret);
        laserButton = (ToggleButton) findViewById(R.id.LaserToggleButton);
        fireButton = (Button) findViewById(R.id.FireButton);
        mgButton = (Button) findViewById(R.id.MGButton);

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
                            touchViewMovement.setService(mService);
                            touchViewTurret.setService(mService);
                            mService.setLaserButton(laserButton);
                            constraintLayout.removeView(findViewById(R.id.ScrollView));
                            fireButton.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                                        mService.setAmpu(true);
                                    }else if(event.getAction() == MotionEvent.ACTION_CANCEL
                                            || event.getAction() == MotionEvent.ACTION_UP){
                                        mService.setAmpu(false);
                                    }
                                    return true;
                                }
                            });
                            mgButton.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                                            mService.setKonsu(true);
                                    }else if(event.getAction() == MotionEvent.ACTION_CANCEL
                                            || event.getAction() == MotionEvent.ACTION_UP){
                                            mService.setKonsu(false);
                                    }
                                    return true;
                                }
                            });
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
            cnt.cancel();
            mService.cancel();
        }
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(R.string.text_not_connected);
    }

    public void closeButtonClicked(View view) {
        ((ViewGroup) findViewById(R.id.ConstraintLayout)).removeView(findViewById(R.id.ScrollView));
    }
}
