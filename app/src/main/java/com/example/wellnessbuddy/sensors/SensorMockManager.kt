package com.example.wellnessbuddy.sensors

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.random.Random

/**
 * Mock sensor manager for testing sensor features on emulators or devices without sensors
 */
class SensorMockManager(private val context: Context) {
    
    private val handler = Handler(Looper.getMainLooper())
    
    // Mock sensor callbacks
    private var onStepDetected: ((Int) -> Unit)? = null
    private var onSensorStatusChanged: ((Boolean) -> Unit)? = null
    private var onAccelerationChanged: ((Float) -> Unit)? = null
    private var onShakeDetected: (() -> Unit)? = null
    
    // Mock data
    private var stepCount = 0
    private var isListening = false
    private var mockAcceleration = 9.8f
    private var isMockMode = false
    
    // Mock thresholds
    private val shakeThreshold = 15.0f
    private val stepThreshold = 12.0f
    
    fun setOnStepDetected(callback: (Int) -> Unit) {
        onStepDetected = callback
    }
    
    fun setOnSensorStatusChanged(callback: (Boolean) -> Unit) {
        onSensorStatusChanged = callback
    }
    
    fun setOnAccelerationChanged(callback: (Float) -> Unit) {
        onAccelerationChanged = callback
    }
    
    fun setOnShakeDetected(callback: () -> Unit) {
        onShakeDetected = callback
    }
    
    fun startListening() {
        isListening = true
        isMockMode = true
        onSensorStatusChanged?.invoke(true)
        Log.d("SensorMock", "Started mock sensor listening")
        
        // Start simulating sensor data
        simulateSensorData()
    }
    
    fun stopListening() {
        isListening = false
        isMockMode = false
        onSensorStatusChanged?.invoke(false)
        Log.d("SensorMock", "Stopped mock sensor listening")
    }
    
    fun getStepCount(): Int = stepCount
    
    fun resetStepCount() {
        stepCount = 0
        onStepDetected?.invoke(stepCount)
        Log.d("SensorMock", "Step count reset to 0")
    }
    
    fun isSensorAvailable(): Boolean = true // Always available in mock mode
    
    fun isCurrentlyListening(): Boolean = isListening
    
    fun getLastAcceleration(): Float = mockAcceleration
    
    fun getShakeThreshold(): Float = shakeThreshold
    
    fun getStepThreshold(): Float = stepThreshold
    
    // Mock sensor simulation methods
    fun simulateShake() {
        if (!isListening) return
        
        Log.d("SensorMock", "Simulating shake")
        mockAcceleration = shakeThreshold + Random.nextFloat() * 5f
        onAccelerationChanged?.invoke(mockAcceleration)
        onShakeDetected?.invoke()
        
        // Reset acceleration after shake
        handler.postDelayed({
            mockAcceleration = 9.8f + Random.nextFloat() * 2f
            onAccelerationChanged?.invoke(mockAcceleration)
        }, 1000)
    }
    
    fun simulateStep() {
        if (!isListening) return
        
        Log.d("SensorMock", "Simulating step")
        stepCount++
        mockAcceleration = stepThreshold + Random.nextFloat() * 2f
        onStepDetected?.invoke(stepCount)
        onAccelerationChanged?.invoke(mockAcceleration)
        
        // Reset acceleration after step
        handler.postDelayed({
            mockAcceleration = 9.8f + Random.nextFloat() * 2f
            onAccelerationChanged?.invoke(mockAcceleration)
        }, 500)
    }
    
    fun simulateWalking() {
        if (!isListening) return
        
        Log.d("SensorMock", "Simulating walking")
        repeat(10) { i ->
            handler.postDelayed({
                simulateStep()
            }, i * 500L) // Simulate 10 steps over 5 seconds
        }
    }
    
    private fun simulateSensorData() {
        if (!isListening) return
        
        // Simulate random acceleration changes
        val randomAcceleration = 9.8f + Random.nextFloat() * 3f
        mockAcceleration = randomAcceleration
        onAccelerationChanged?.invoke(mockAcceleration)
        
        // Schedule next simulation
        handler.postDelayed({
            simulateSensorData()
        }, Random.nextLong(1000, 3000)) // Random interval between 1-3 seconds
    }
}
