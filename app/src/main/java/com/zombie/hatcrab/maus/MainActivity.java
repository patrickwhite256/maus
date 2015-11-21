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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    final double NOISE_THRESHOLD_X = 0.07;
    final double NOISE_THRESHOLD_Y = 0.07;
    final double NOISE_THRESHOLD_Z = 0.07;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gravity;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;

    private final UUID SERVER_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println("Found: " + device.getName() + "; " + device.getAddress());
            }
        }
    };

    float lastGX = 0;
    float lastGY = 0;
    float lastGZ = 0;
    long lastT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            System.err.println("No bluetooth adapter...");
        } else if(!bluetoothAdapter.isEnabled()) {
            System.err.println("Bluetooth disabled...");
        } else {
            Set<BluetoothDevice> bonded = bluetoothAdapter.getBondedDevices();
            System.out.println("bonded: " + bonded.size());
            for(BluetoothDevice device : bonded) {
                if(device.getName().equals("mint-0")){
                    try {
                        socket = device.createInsecureRfcommSocketToServiceRecord(SERVER_UUID);
                        socket.connect();
                        System.out.println(socket.isConnected());
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(device.getName() + ":" + device.getAddress());
            }
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
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float dx = (event.values[0] - lastGX);
            float dy = (event.values[1] - lastGY);
            float dz = (event.values[2] - lastGZ);
            lastT = System.currentTimeMillis();
            if(socket.isConnected()) {
                try {
                    OutputStream os = socket.getOutputStream();
                    if(Math.abs(dx) > NOISE_THRESHOLD_X) {
                        os.write(("dx:" + dx + "\n").getBytes());
                    }
                    if(Math.abs(dy) > NOISE_THRESHOLD_Y) {
                        os.write(("dy:" + dy + "\n").getBytes());
                    }
                    if(Math.abs(dz) > NOISE_THRESHOLD_Z) {
                        //os.write(("dz" + dz).getBytes());
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            lastGX = event.values[0];
            lastGY = event.values[1];
            lastGZ = event.values[2];
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
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btReceiver);
    }
}
