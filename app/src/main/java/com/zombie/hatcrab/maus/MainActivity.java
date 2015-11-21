package com.zombie.hatcrab.maus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    final int CALIB_REQUIRED = 100;


    private SensorManager sensorManager;
    private Sensor linearAccelerator;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private TextView connectionStatus;
    private TextView calibrationStatus;

    private final UUID SERVER_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    float calibTotalX = 0;
    float calibTotalY = 0;
    int calibCount = 0;
    float calibX;
    float calibY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        linearAccelerator = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, linearAccelerator, SensorManager.SENSOR_DELAY_NORMAL);

        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        calibrationStatus = (TextView) findViewById(R.id.calibrationStatus);
        connectionStatus.setText("Not connected.");

        Button connectButton = (Button) findViewById(R.id.connect);
        Button calibrateButton = (Button) findViewById(R.id.calibrate);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bonded = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : bonded) {
                    try {
                        socket = device.createInsecureRfcommSocketToServiceRecord(SERVER_UUID);
                        socket.connect();
                        connectionStatus.setText("Connected.");
                        break;
                    } catch (IOException e) {
                        connectionStatus.setText("Connection failed.");
                        e.printStackTrace();
                    }
                }
            }
        });
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibCount = 0;
                calibTotalX = 0;
                calibTotalY = 0;
            }
        });
        Button lClickButton = (Button) findViewById(R.id.lclick);
        lClickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(socket.isConnected()) {
                    try {
                        OutputStream os = socket.getOutputStream();
                        os.write(("lc:\n").getBytes());
                    } catch (IOException e) {
                        try {
                            socket.close();
                            connectionStatus.setText("Not connected.");
                        } catch (IOException ex) {
                            System.err.println("now what");
                        }
                    }
                }
            }
        });

        Button rClickButton = (Button) findViewById(R.id.rclick);
        rClickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(socket.isConnected()) {
                    try {
                        OutputStream os = socket.getOutputStream();
                        os.write(("rc:\n").getBytes());
                    } catch (IOException e) {
                        try {
                            socket.close();
                            connectionStatus.setText("Not connected.");
                        } catch (IOException ex) {
                            System.err.println("now what");
                        }
                    }
                }
            }
        });


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            System.err.println("No bluetooth adapter...");
        } else if(!bluetoothAdapter.isEnabled()) {
            System.err.println("Bluetooth disabled...");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float dx = (event.values[0]);
            float dy = (event.values[1]);
            if(calibCount < CALIB_REQUIRED) {
                calibTotalX += dx;
                calibTotalY += dy;
                calibCount++;
                calibrationStatus.setText("Calibrating: " + calibCount + "/" + CALIB_REQUIRED);
                return;
            } else if (calibCount == CALIB_REQUIRED) {
                calibX = calibTotalX / CALIB_REQUIRED;
                calibY = calibTotalY / CALIB_REQUIRED;
                calibrationStatus.setText("Calibrated!");
            }

            dx -= calibX;
            dy -= calibY;

            if(socket != null && socket.isConnected()) {
                try {
                    OutputStream os = socket.getOutputStream();
                    os.write(("dx:" + dx + "\n").getBytes());
                    os.write(("dy:" + dy + "\n").getBytes());
                } catch(IOException e) {
                    e.printStackTrace();
                    try {
                        socket.close();
                        connectionStatus.setText("Not connected.");
                    } catch(IOException ex) {
                        System.err.println("now what");
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, linearAccelerator, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
