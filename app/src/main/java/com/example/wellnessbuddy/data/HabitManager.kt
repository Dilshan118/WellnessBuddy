package com.example.wellnessbuddy.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Manager class for handling habit data with SharedPreferences
 */
class HabitManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("habits", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val HABITS_KEY = "habits_list"
        private const val LAST_RESET_KEY = "last_reset_date"
    }
    
    /**
     * Save habits to SharedPreferences
     */
    fun saveHabits(habits: List<Habit>) {
        val habitsJson = gson.toJson(habits)
        prefs.edit().putString(HABITS_KEY, habitsJson).apply()
    }
    
    /**
     * Load habits from SharedPreferences
     */
    fun loadHabits(): List<Habit> {
        val habitsJson = prefs.getString(HABITS_KEY, null)
        return if (habitsJson != null) {
            try {
                val type = object : TypeToken<List<Habit>>() {}.type
                gson.fromJson(habitsJson, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Add a new habit
     */
    fun addHabit(habit: Habit): Habit {
        val habits = loadHabits().toMutableList()
        val newHabit = habit.copy(id = generateId())
        habits.add(newHabit)
        saveHabits(habits)
        return newHabit
    }
    
    /**
     * Update an existing habit
     */
    fun updateHabit(habit: Habit) {
        val habits = loadHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit.copy(lastUpdated = Date())
            saveHabits(habits)
        }
    }
    
    /**
     * Delete a habit
     */
    fun deleteHabit(habitId: String) {
        val habits = loadHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
    }
    
    /**
     * Mark habit as completed for today
     */
    fun completeHabit(habitId: String) {
        val habits = loadHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habitId }
        if (index != -1) {
            val habit = habits[index]
            val newCompletedCount = habit.completedCount + 1
            val isCompleted = newCompletedCount >= habit.targetCount
            habits[index] = habit.copy(
                completedCount = newCompletedCount,
                isCompleted = isCompleted,
                lastUpdated = Date()
            )
            saveHabits(habits)
        }
    }
    
    /**
     * Reset daily progress for all habits (call this daily)
     */
    fun resetDailyProgress() {
        val today = Date().time / (24 * 60 * 60 * 1000) // Days since epoch
        val lastReset = prefs.getLong(LAST_RESET_KEY, 0L) / (24 * 60 * 60 * 1000)
        
        if (today > lastReset) {
            val habits = loadHabits().map { habit ->
                habit.copy(
                    completedCount = 0,
                    isCompleted = false,
                    lastUpdated = Date()
                )
            }
            saveHabits(habits)
            prefs.edit().putLong(LAST_RESET_KEY, Date().time).apply()
        }
    }
    
    /**
     * Generate unique ID for habits
     */
    private fun generateId(): String {
        return "habit_${System.currentTimeMillis()}_${(0..999).random()}"
    }
}
