package com.fabiorogeriosj.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.hardware.*;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

public class Sensors extends CordovaPlugin {

    private HashMap<String, CordovaSensor> sensors;
    private CallbackContext callbackContext;
    private SensorManager sensorManager;// Sensor manager

    /**
     * Constructor.
     */
    public Sensors() {
        this.sensors = new HashMap<>();
    }

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.sensorManager = (SensorManager) cordova.getActivity().getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * Executes the request.
     *
     * This method is called from the WebView thread. To do a non-trivial amount of work, use:
     *     cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     *     cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments in JSON form.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return                Whether the action was valid.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("start")) {
            String sensorType = args.getString(0);
            if (this.sensors.containsKey(sensorType)) return true;
            this.sensors.put(sensorType, new CordovaSensor(sensorType, this.sensorManager, callbackContext));
        }
        else if (action.equals("stop")) {
            this.stop(args.getString(0));
        }
        else if (action.equals("getState")) {
            String sensorType = args.getString(0);
            // If its stopped then user needs to enable sensor using "start" method
            if (!this.sensors.containsKey(sensorType)) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Sensors disabled, run start method before getState"));
            }
            CordovaSensor cordovaSensor = this.sensors.get(sensorType);
            // If not running, then this is an async call, so don't worry about waiting
            if (cordovaSensor.status != CordovaSensor.RUNNING) {

                int r = cordovaSensor.start();
                if (r == CordovaSensor.ERROR_FAILED_TO_START) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION, CordovaSensor.ERROR_FAILED_TO_START));
                    return true;
                }
                // Set a timeout callback on the main thread.
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    public void run() {
                        cordovaSensor.timeout();
                    }
                }, 2000);
            }
            else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, cordovaSensor.getValue()));
            }
        } else {
            // Unsupported action
            return false;
        }
        return true;
    }

    /**
     * Called when listener is to be shut down and object is being destroyed.
     */
    public void onDestroy() {
        this.destroy();
    }

    /**
     * Called when app has navigated and JS listeners have been destroyed.
     */
    public void onReset() {
        this.destroy();
    }

    /**
     * Stop all listeners.
     */
    public void destroy() {
        for (Map.Entry<String, CordovaSensor> entry: this.sensors.entrySet()) {
            entry.getValue().stop();
        }
        this.sensors.clear();
    }

    /**
     * Stop listening to compass sensor.
     */
    public boolean stop(String sensor) {
        if (!this.sensors.containsKey(sensor)) return false;
        this.sensors.get(sensor).stop();
        this.sensors.remove(sensor);
        return true;
    }

}
