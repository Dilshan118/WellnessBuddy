package com.example.wellnessbuddy.sensors

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

/**
 * Manages accelerometer sensor for step counting and shake detection
 * with UI display functionality and local persistence.
 */
class WellnessSensorManager(private val context: Context) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
    
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
    
    init {
        loadStepCount()
    }
    
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
        saveStepCount()
        onStepDetected?.invoke(stepCount)
        Log.d("Sensor", "Step count reset to 0")
    }
    
    private fun saveStepCount() {
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        sharedPreferences.edit().apply {
            putInt("step_count", stepCount)
            putString("step_date", currentDate)
            apply()
        }
    }

    private fun loadStepCount() {
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val savedDate = sharedPreferences.getString("step_date", "")
        
        if (savedDate == currentDate) {
            stepCount = sharedPreferences.getInt("step_count", 0)
        } else {
            // Reset if it's a new day
            stepCount = 0
            saveStepCount()
        }
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
            // Check for day change before incrementing
            val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val savedDate = sharedPreferences.getString("step_date", "")
            
            if (savedDate != currentDate && savedDate != "") {
                stepCount = 0
                Log.d("Sensor", "New day detected, resetting steps")
            }

            lastStepTime = currentTime
            stepCount++
            saveStepCount() // Save on every step
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
