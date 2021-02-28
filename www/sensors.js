/*global cordova, module*/

module.exports = {
    getState: function(TYPE_SENSOR, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Sensors", "getState", [TYPE_SENSOR]);
    },

    /**
     *  Enable the sensor. Needs to be called before getting the state.
     */
    enableSensor: function(TYPE_SENSOR) {
        cordova.exec(null, null, "Sensors", "start", [TYPE_SENSOR]);
    },

    /**
     *  Disable the sensor.
     */
    disableSensor: function(TYPE_SENSOR) {
        cordova.exec(null, null, "Sensors", "stop", [TYPE_SENSOR]);
    }
};


/*
DOC:

"PROXIMITY"
"ACCELEROMETER"
"GRAVITY"
"GYROSCOPE"
"GYROSCOPE_UNCALIBRATED"
"LINEAR_ACCELERATION"
"ROTATION_VECTOR"
"SIGNIFICANT_MOTION"
"STEP_COUNTER"
"STEP_DETECTOR"
"GAME_ROTATION_VECTOR"
"GEOMAGNETIC_ROTATION_VECTOR"
"MAGNETIC_FIELD"
"MAGNETIC_FIELD_UNCALIBRATED"
"ORIENTATION"
"AMBIENT_TEMPERATURE"
"LIGHT"
"PRESSURE"
"RELATIVE_HUMIDITY"
"TEMPERATURE"
*/