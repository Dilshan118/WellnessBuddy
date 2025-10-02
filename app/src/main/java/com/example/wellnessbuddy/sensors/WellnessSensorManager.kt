package com.example.wellnessbuddy.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import kotlin.math.sqrt

/**
 * Manages accelerometer sensor for step counting and shake detection
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
    
    private var onShakeDetected: (() -> Unit)? = null
    private var onStepDetected: (() -> Unit)? = null
    
    private val handler = Handler(Looper.getMainLooper())
    
    fun setOnShakeDetected(callback: () -> Unit) {
        onShakeDetected = callback
    }
    
    fun setOnStepDetected(callback: () -> Unit) {
        onStepDetected = callback
    }
    
    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    
    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
    
    fun getStepCount(): Int = stepCount
    
    fun resetStepCount() {
        stepCount = 0
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            if (sensorEvent.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = sensorEvent.values[0]
                val y = sensorEvent.values[1]
                val z = sensorEvent.values[2]
                
                val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                
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
            handler.post {
                onStepDetected?.invoke()
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
}
