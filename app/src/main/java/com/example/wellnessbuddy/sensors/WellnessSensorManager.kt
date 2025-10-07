package com.example.wellnessbuddy.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.sqrt

/**
 * Manages accelerometer sensor for step counting and shake detection
 * with UI display functionality
 */
class WellnessSensorManager(private val context: Context) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var lastShakeTime = 0L
    private val shakeThreshold = 15.0f
    private val shakeDelay = 1000L // 1 second between shakes
    
    private var stepCount = 0
    private var lastStepTime = 0L
    private val stepThreshold = 12.0f
    private val stepDelay = 300L // 300ms between steps
    
    // UI callback functions
    private var onShakeDetected: (() -> Unit)? = null
    private var onStepDetected: ((Int) -> Unit)? = null
    private var onSensorStatusChanged: ((Boolean) -> Unit)? = null
    private var onAccelerationChanged: ((Float) -> Unit)? = null
    
    private val handler = Handler(Looper.getMainLooper())
    
    // Sensor status tracking
    private var isListening = false
    private var lastAcceleration = 0.0f
    
    fun setOnShakeDetected(callback: () -> Unit) {
        onShakeDetected = callback
    }
    
    fun setOnStepDetected(callback: (Int) -> Unit) {
        onStepDetected = callback
    }
    
    fun setOnSensorStatusChanged(callback: (Boolean) -> Unit) {
        onSensorStatusChanged = callback
    }
    
    fun setOnAccelerationChanged(callback: (Float) -> Unit) {
        onAccelerationChanged = callback
    }
    
    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            isListening = true
            onSensorStatusChanged?.invoke(true)
            Log.d("Sensor", "Started listening to accelerometer")
        } ?: run {
            Log.w("Sensor", "Accelerometer not available on this device")
            onSensorStatusChanged?.invoke(false)
        }
    }
    
    fun stopListening() {
        sensorManager.unregisterListener(this)
        isListening = false
        onSensorStatusChanged?.invoke(false)
        Log.d("Sensor", "Stopped listening to accelerometer")
    }
    
    fun getStepCount(): Int = stepCount
    
    fun resetStepCount() {
        stepCount = 0
        onStepDetected?.invoke(stepCount)
        Log.d("Sensor", "Step count reset to 0")
    }
    
    fun isSensorAvailable(): Boolean {
        return accelerometer != null
    }
    
    fun isCurrentlyListening(): Boolean {
        return isListening
    }
    
    fun getLastAcceleration(): Float {
        return lastAcceleration
    }
    
    fun getShakeThreshold(): Float {
        return shakeThreshold
    }
    
    fun getStepThreshold(): Float {
        return stepThreshold
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            if (sensorEvent.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = sensorEvent.values[0]
                val y = sensorEvent.values[1]
                val z = sensorEvent.values[2]
                
                val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                lastAcceleration = acceleration
                
                // Notify UI of acceleration changes
                onAccelerationChanged?.invoke(acceleration)
                
                // Detect shake
                detectShake(acceleration)
                
                // Detect step
                detectStep(acceleration)
            }
        }
    }
    
    private fun detectShake(acceleration: Float) {
        val currentTime = System.currentTimeMillis()
        
        if (acceleration > shakeThreshold && currentTime - lastShakeTime > shakeDelay) {
            lastShakeTime = currentTime
            handler.post {
                onShakeDetected?.invoke()
            }
        }
    }
    
    private fun detectStep(acceleration: Float) {
        val currentTime = System.currentTimeMillis()
        
        if (acceleration > stepThreshold && currentTime - lastStepTime > stepDelay) {
            lastStepTime = currentTime
            stepCount++
            Log.d("Sensor", "Step detected! Total steps: $stepCount")
            handler.post {
                onStepDetected?.invoke(stepCount)
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
}
