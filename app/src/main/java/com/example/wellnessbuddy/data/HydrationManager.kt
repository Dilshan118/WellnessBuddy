package com.example.wellnessbuddy.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manager class for handling hydration settings and tracking with SharedPreferences
 */
class HydrationManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hydration", Context.MODE_PRIVATE)
    
    companion object {
        private const val DAILY_GOAL_KEY = "daily_goal"
        private const val REMINDER_INTERVAL_KEY = "reminder_interval"
        private const val IS_ENABLED_KEY = "is_enabled"
        private const val START_TIME_KEY = "start_time"
        private const val END_TIME_KEY = "end_time"
        private const val GLASSES_CONSUMED_KEY = "glasses_consumed"
        private const val LAST_REMINDER_KEY = "last_reminder"
        private const val LAST_RESET_KEY = "last_reset_date"
    }
    
    /**
     * Load hydration settings from SharedPreferences
     */
    fun loadSettings(): HydrationSettings {
        return HydrationSettings(
            dailyGoal = prefs.getInt(DAILY_GOAL_KEY, 8),
            reminderInterval = prefs.getInt(REMINDER_INTERVAL_KEY, 2),
            isEnabled = prefs.getBoolean(IS_ENABLED_KEY, true),
            startTime = prefs.getString(START_TIME_KEY, "08:00") ?: "08:00",
            endTime = prefs.getString(END_TIME_KEY, "22:00") ?: "22:00",
            glassesConsumed = prefs.getInt(GLASSES_CONSUMED_KEY, 0),
            lastReminderTime = prefs.getLong(LAST_REMINDER_KEY, 0L)
        )
    }
    
    /**
     * Save hydration settings to SharedPreferences
     */
    fun saveSettings(settings: HydrationSettings) {
        prefs.edit()
            .putInt(DAILY_GOAL_KEY, settings.dailyGoal)
            .putInt(REMINDER_INTERVAL_KEY, settings.reminderInterval)
            .putBoolean(IS_ENABLED_KEY, settings.isEnabled)
            .putString(START_TIME_KEY, settings.startTime)
            .putString(END_TIME_KEY, settings.endTime)
            .putInt(GLASSES_CONSUMED_KEY, settings.glassesConsumed)
            .putLong(LAST_REMINDER_KEY, settings.lastReminderTime)
            .apply()
    }
    
    /**
     * Add a glass of water consumed
     */
    fun addGlass() {
        val settings = loadSettings()
        val newSettings = settings.copy(glassesConsumed = settings.glassesConsumed + 1)
        saveSettings(newSettings)
    }
    
    /**
     * Remove a glass of water (if user made a mistake)
     */
    fun removeGlass() {
        val settings = loadSettings()
        val newSettings = settings.copy(
            glassesConsumed = (settings.glassesConsumed - 1).coerceAtLeast(0)
        )
        saveSettings(newSettings)
    }
    
    /**
     * Reset daily consumption (call this daily)
     */
    fun resetDailyConsumption() {
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000) // Days since epoch
        val lastReset = prefs.getLong(LAST_RESET_KEY, 0L) / (24 * 60 * 60 * 1000)
        
        if (today > lastReset) {
            val settings = loadSettings()
            val newSettings = settings.copy(glassesConsumed = 0)
            saveSettings(newSettings)
            prefs.edit().putLong(LAST_RESET_KEY, System.currentTimeMillis()).apply()
        }
    }
    
    /**
     * Update reminder time
     */
    fun updateReminderTime() {
        val settings = loadSettings()
        val newSettings = settings.copy(lastReminderTime = System.currentTimeMillis())
        saveSettings(newSettings)
    }
    
    /**
     * Check if it's time for next reminder
     */
    fun isTimeForReminder(): Boolean {
        val settings = loadSettings()
        if (!settings.isEnabled) return false
        
        val currentTime = System.currentTimeMillis()
        val timeSinceLastReminder = currentTime - settings.lastReminderTime
        val reminderIntervalMs = settings.reminderInterval * 60 * 60 * 1000L // Convert hours to ms
        
        return timeSinceLastReminder >= reminderIntervalMs
    }
}
