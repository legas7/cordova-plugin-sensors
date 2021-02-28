package com.fabiorogeriosj.plugin;

import java.util.List;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import android.hardware.*;
import java.util.ArrayList;

public class CordovaSensor implements SensorEventListener {

    private CallbackContext callbackContext;

    public static int STOPPED = 0;
    public static int STARTING = 1;
    public static int RUNNING = 2;
    public static int ERROR_FAILED_TO_START = 3;

    // sensor result

    public long TIMEOUT = 30000;        // Timeout in msec to shut off listener

    int status;                         // status of listener
    long timeStamp;                     // time of most recent value
    long lastAccessTime;                // time the value was last retrieved

    JSONArray value;
    String TYPE_SENSOR;

    private SensorManager sensorManager;// Sensor manager
    Sensor mSensor;                     // Compass sensor returned by sensor manager

    /**
     * Constructor.
     */
    public CordovaSensor(String sensor, SensorManager sensorManager, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        this.sensorManager = sensorManager;
        this.value = new JSONArray();
        this.TYPE_SENSOR = sensor;
        this.timeStamp = 0;
        this.setStatus(CordovaSensor.STOPPED);
        this.start();
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Start listening for compass sensor.
     *
     * @return          status of listener
     */
    public int start() {

        // If already starting or running, then just return
        if ((this.status == CordovaSensor.RUNNING) || (this.status == CordovaSensor.STARTING)) {
            return this.status;
        }

        // Get sensor from sensor manager
        @SuppressWarnings("deprecation")
        List<Sensor> list = new ArrayList<Sensor>();
        if(this.TYPE_SENSOR.equals("PROXIMITY")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
        } else if(this.TYPE_SENSOR.equals("ACCELEROMETER")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        } else if(this.TYPE_SENSOR.equals("GRAVITY")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_GRAVITY);
        } else if(this.TYPE_SENSOR.equals("GYROSCOPE")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        } else if(this.TYPE_SENSOR.equals("GYROSCOPE_UNCALIBRATED")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        } else if(this.TYPE_SENSOR.equals("LINEAR_ACCELERATION")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        } else if(this.TYPE_SENSOR.equals("ROTATION_VECTOR")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
        } else if(this.TYPE_SENSOR.equals("SIGNIFICANT_MOTION")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_SIGNIFICANT_MOTION);
        } else if(this.TYPE_SENSOR.equals("STEP_COUNTER")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_STEP_COUNTER);
        } else if(this.TYPE_SENSOR.equals("STEP_DETECTOR")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_STEP_DETECTOR);
        } else if(this.TYPE_SENSOR.equals("GAME_ROTATION_VECTOR")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_GAME_ROTATION_VECTOR);
        } else if(this.TYPE_SENSOR.equals("GEOMAGNETIC_ROTATION_VECTOR")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        } else if(this.TYPE_SENSOR.equals("MAGNETIC_FIELD")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        } else if(this.TYPE_SENSOR.equals("MAGNETIC_FIELD_UNCALIBRATED")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        } else if(this.TYPE_SENSOR.equals("ORIENTATION")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        } else if(this.TYPE_SENSOR.equals("AMBIENT_TEMPERATURE")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
        } else if(this.TYPE_SENSOR.equals("LIGHT")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        } else if(this.TYPE_SENSOR.equals("PRESSURE")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_PRESSURE);
        } else if(this.TYPE_SENSOR.equals("RELATIVE_HUMIDITY")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY);
        } else if(this.TYPE_SENSOR.equals("TEMPERATURE")){
            list = this.sensorManager.getSensorList(Sensor.TYPE_TEMPERATURE);
        }

        // If found, then register as listener
        if (list != null && list.size() > 0) {
            this.mSensor = list.get(0);
            this.sensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            this.lastAccessTime = System.currentTimeMillis();
            this.setStatus(CordovaSensor.STARTING);
        } else {
            this.setStatus(CordovaSensor.ERROR_FAILED_TO_START);
        }

        return this.status;
    }

    /**
     * Stop listening to compass sensor.
     */
    public void stop() {
        if (this.status != CordovaSensor.STOPPED) {
            this.sensorManager.unregisterListener(this);
        }
        this.setStatus(CordovaSensor.STOPPED);
        this.value = new JSONArray();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    /**
     * Called after a delay to time out if the listener has not attached fast enough.
     */
    public void timeout() {
        if (this.status == CordovaSensor.STARTING) {
            this.setStatus(CordovaSensor.ERROR_FAILED_TO_START);
            if (this.callbackContext != null) {
                this.callbackContext.error("Compass listener failed to start.");
            }
        }
    }

    /**
     * Sensor listener event.
     *
     * @param SensorEvent event
     */
    public void onSensorChanged(SensorEvent event) {
        try {
            JSONArray value = new JSONArray();
            for(int i=0;i<event.values.length;i++){

                    value.put(Float.parseFloat(event.values[i]+""));

            }

            this.timeStamp = System.currentTimeMillis();
            this.value = value;
            this.setStatus(CordovaSensor.RUNNING);

            // If proximity hasn't been read for TIMEOUT time, then turn off sensor to save power
            if ((this.timeStamp - this.lastAccessTime) > this.TIMEOUT) {
                this.stop();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get status of sensor.
     *
     * @return          status
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * Get the most recent distance.
     *
     * @return          distance
     */
    public JSONArray getValue() {
        this.lastAccessTime = System.currentTimeMillis();
        return this.value;
    }


    /**
     * Set the timeout to turn off sensor if getValue() hasn't been called.
     *
     * @param timeout       Timeout in msec.
     */
    public void setTimeout(long timeout) {
        this.TIMEOUT = timeout;
    }

    /**
     * Get the timeout to turn off sensor if getValue() hasn't been called.
     *
     * @return timeout in msec
     */
    public long getTimeout() {
        return this.TIMEOUT;
    }

    /**
     * Set the status and send it to JavaScript.
     * @param status
     */
    private void setStatus(int status) {
        this.status = status;
    }
}
