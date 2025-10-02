package com.example.wellnessbuddy.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Manager class for handling mood entries with SharedPreferences
 */
class MoodManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("moods", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val MOODS_KEY = "moods_list"
    }
    
    /**
     * Save mood entries to SharedPreferences
     */
    fun saveMoods(moods: List<MoodEntry>) {
        val moodsJson = gson.toJson(moods)
        prefs.edit().putString(MOODS_KEY, moodsJson).apply()
    }
    
    /**
     * Load mood entries from SharedPreferences
     */
    fun loadMoods(): List<MoodEntry> {
        val moodsJson = prefs.getString(MOODS_KEY, null)
        return if (moodsJson != null) {
            try {
                val type = object : TypeToken<List<MoodEntry>>() {}.type
                gson.fromJson(moodsJson, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Add a new mood entry
     */
    fun addMood(mood: MoodEntry): MoodEntry {
        val moods = loadMoods().toMutableList()
        val newMood = mood.copy(id = generateId())
        moods.add(newMood)
        saveMoods(moods)
        return newMood
    }
    
    /**
     * Update an existing mood entry
     */
    fun updateMood(mood: MoodEntry) {
        val moods = loadMoods().toMutableList()
        val index = moods.indexOfFirst { it.id == mood.id }
        if (index != -1) {
            moods[index] = mood
            saveMoods(moods)
        }
    }
    
    /**
     * Delete a mood entry
     */
    fun deleteMood(moodId: String) {
        val moods = loadMoods().toMutableList()
        moods.removeAll { it.id == moodId }
        saveMoods(moods)
    }
    
    /**
     * Get moods for a specific date
     */
    fun getMoodsForDate(date: Date): List<MoodEntry> {
        val allMoods = loadMoods()
        val targetDate = date.time / (24 * 60 * 60 * 1000) // Days since epoch
        return allMoods.filter { mood ->
            val moodDate = mood.date.time / (24 * 60 * 60 * 1000)
            moodDate == targetDate
        }
    }
    
    /**
     * Get moods for the last N days
     */
    fun getMoodsForLastDays(days: Int): List<MoodEntry> {
        val allMoods = loadMoods()
        val cutoffDate = Date(System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L))
        return allMoods.filter { it.date.after(cutoffDate) }
    }
    
    /**
     * Get average mood intensity for a date range
     */
    fun getAverageMoodIntensity(startDate: Date, endDate: Date): Float {
        val moods = loadMoods().filter { mood ->
            mood.date.after(startDate) && mood.date.before(endDate)
        }
        return if (moods.isNotEmpty()) {
            moods.map { it.intensity }.average().toFloat()
        } else 0f
    }
    
    /**
     * Generate unique ID for mood entries
     */
    private fun generateId(): String {
        return "mood_${System.currentTimeMillis()}_${(0..999).random()}"
    }
}
