package com.example.humanactivityrecognition;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private LineGraphSeries<DataPoint> seriesAccX,seriesAccY,seriesAccZ;
    private static double currentX,currentY;
    private LineGraphSeries<DataPoint> seriesGyroX,seriesGyroY,seriesGyroZ;
    private ThreadPoolExecutor liveChartExecutor, liveChartExecutorGyro;
    private LinkedBlockingQueue<Double> accelerationQueue = new LinkedBlockingQueue<>(10);
    private LinkedBlockingQueue<Double> gyroscopeQueue = new LinkedBlockingQueue<>(10);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        GraphView graph = (GraphView) findViewById(R.id.graphAccelerometer);
        GraphView graphGyroscope = (GraphView) findViewById(R.id.graphGyroscope);

        seriesAccX = new LineGraphSeries<>();
        seriesAccX.setColor(Color.RED);
        graph.addSeries(seriesAccX);

        seriesAccY = new LineGraphSeries<>();
        seriesAccY.setColor(Color.GREEN);
        graph.addSeries(seriesAccY);

        seriesAccZ = new LineGraphSeries<>();
        seriesAccZ.setColor(Color.BLUE);
        graph.addSeries(seriesAccZ);

        seriesGyroX = new LineGraphSeries<>();
        seriesGyroX.setColor(Color.RED);
        graphGyroscope.addSeries(seriesGyroX);

        seriesGyroY = new LineGraphSeries<>();
        seriesGyroY.setColor(Color.GREEN);
        graphGyroscope.addSeries(seriesGyroY);

        seriesGyroZ = new LineGraphSeries<>();
        seriesGyroZ.setColor(Color.BLUE);
        graphGyroscope.addSeries(seriesGyroZ);


        // activate horizontal zooming and scrolling
        graph.getViewport().setScalable(true);
        graphGyroscope.getViewport().setScalable(true);
        // activate horizontal scrolling
        graph.getViewport().setScrollable(true);
        graphGyroscope.getViewport().setScrollable(true);

        // activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScalableY(true);
        graphGyroscope.getViewport().setScalableY(true);
        // activate vertical scrolling
        graph.getViewport().setScrollableY(true);
        graphGyroscope.getViewport().setScrollableY(true);
        // To set a fixed manual viewport use this:
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0.5);
        graph.getViewport().setMaxX(6.5);
        graphGyroscope.getViewport().setXAxisBoundsManual(true);
        graphGyroscope.getViewport().setMinX(0.5);
        graphGyroscope.getViewport().setMaxX(6.5);

        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(10);
        graphGyroscope.getViewport().setYAxisBoundsManual(true);
        graphGyroscope.getViewport().setMinY(0);
        graphGyroscope.getViewport().setMaxY(10);

        currentX = 0;
        currentY = 0;

        // Start chart thread
        liveChartExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        if (liveChartExecutor != null)
            liveChartExecutor.execute(new AccelerationChart(new AccelerationChartHandler()));

        liveChartExecutorGyro = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        if (liveChartExecutorGyro != null)
            liveChartExecutorGyro.execute(new GyroscopeChart(new GyroscopeChartHandler()));

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            getGyroscope(sensorEvent);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

        }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        double x = values[0];
        double y = values[1];
        double z = values[2];

        double accelerationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        double acceleration = Math.sqrt(accelerationSquareRoot);

        accelerationQueue.offer(acceleration);

        seriesAccX.appendData(new DataPoint(currentX, x), true, 10);
        seriesAccY.appendData(new DataPoint(currentX, y), true, 10);
        seriesAccZ.appendData(new DataPoint(currentX, z), true, 10);
    }

    private void getGyroscope(SensorEvent event) {
        float[] values = event.values;
        double x = values[0];
        double y = values[1];
        double z = values[2];

        double gyroscopeSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        double gyroscope = Math.sqrt(gyroscopeSquareRoot);

        gyroscopeQueue.offer(gyroscope);

        seriesGyroX.appendData(new DataPoint(currentY, x), true, 10);
        seriesGyroY.appendData(new DataPoint(currentY, y), true, 10);
        seriesGyroZ.appendData(new DataPoint(currentY, z), true, 10);
    }

    private class AccelerationChartHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Double accelerationY = 0.0D;
            if (!msg.getData().getString("ACCELERATION_VALUE").equals(null) && !msg.getData().getString("ACCELERATION_VALUE").equals("null")) {
                accelerationY = (Double.parseDouble(msg.getData().getString("ACCELERATION_VALUE")));
            }

            seriesAccX.appendData(new DataPoint(currentX, accelerationY), true, 10);
            currentX = currentX + 1;
        }
    }

    private class AccelerationChart implements Runnable {
        private boolean drawChart = true;
        private Handler handler;

        public AccelerationChart(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            while (drawChart) {
                Double accelerationY;
                try {
                    Thread.sleep(300); // Speed up the X axis
                    accelerationY = accelerationQueue.poll();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                if (accelerationY == null)
                    continue;

                // currentX value will be exceed the limit of double type range
                // To overcome this problem comment of this line
                // currentX = (System.currentTimeMillis() / 1000) * 8 + 0.6;

                Message msgObj = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("ACCELERATION_VALUE", String.valueOf(accelerationY));
                msgObj.setData(b);
                handler.sendMessage(msgObj);
            }
        }
    }
    private class GyroscopeChartHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Double gyroscopeY = 0.0D;
            if (!msg.getData().getString("GYROSCOPE_VALUE").equals(null) && !msg.getData().getString("GYROSCOPE_VALUE").equals("null")) {
                gyroscopeY = Double.parseDouble(msg.getData().getString("GYROSCOPE_VALUE"));
            }

            seriesGyroX.appendData(new DataPoint(currentY, gyroscopeY), true, 10);
            currentY = currentY + 1;
        }
    }

    private class GyroscopeChart implements Runnable {
        private boolean drawChart = true;
        private Handler handler;

        public GyroscopeChart(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            while (drawChart) {
                Double gyroscopeX;
                try {
                    Thread.sleep(300); // Hızlı bir X ekseni için bekleme süresini ayarlayın
                    gyroscopeX = gyroscopeQueue.poll();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                if (gyroscopeX == null)
                    continue;

                Message msgObj = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("GYROSCOPE_VALUE", String.valueOf(gyroscopeX));
                msgObj.setData(b);
                handler.sendMessage(msgObj);
            }
        }
    }



}

