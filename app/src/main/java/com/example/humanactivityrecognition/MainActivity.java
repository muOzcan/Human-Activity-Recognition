package com.example.humanactivityrecognition;

import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView tv1,tv2,tv3;
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Değerleri ekranda göstermek için text tanımlaması
        tv1=findViewById(R.id.tv1);
        tv2=findViewById(R.id.tv2);
        tv3=findViewById(R.id.tv3);
        // SensorManageri al
        mSensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        // Accelerometer sensörünü al
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Gyroscope sensörünü al
        gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        // Accelerometer için dinleme
        if (accelerometer != null){
            mSensorManager.registerListener(MainActivity.this,accelerometer,mSensorManager.SENSOR_DELAY_UI);
        }
        // Gyroscope için dinleme
        if (gyroscope != null){
            mSensorManager.registerListener(this, gyroscope, mSensorManager.SENSOR_DELAY_UI);
        }
    }




    @Override
    public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Ivmeölcer değerleri burada alınır
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                // Gyroskop değerleri burada alınır
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

            }
        tv1.setText("X ="+ event.values[0]);
        tv2.setText("Y ="+ event.values[1]);
        tv3.setText("Z ="+ event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
