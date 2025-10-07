package com.example.wellnessbuddy

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.wellnessbuddy.fragments.*
import com.example.wellnessbuddy.sensors.WellnessSensorManager
import com.example.wellnessbuddy.data.MoodManager
import com.example.wellnessbuddy.data.MoodEntry
import com.example.wellnessbuddy.data.AuthManager
import com.example.wellnessbuddy.theme.ThemeManager
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var sensorManager: WellnessSensorManager
    private lateinit var moodManager: MoodManager
    private lateinit var themeManager: ThemeManager
    private lateinit var authManager: AuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        authManager = AuthManager(this)
        
        // Check if user is logged in
        if (!authManager.isLoggedIn()) {
            navigateToAuth()
            return
        }
        
        setupBottomNavigation()
        setupTheme()
        setupSensors()
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }
    
    private fun setupTheme() {
        themeManager = ThemeManager(this)
        themeManager.initializeTheme()
    }
    
    private fun setupSensors() {
        sensorManager = WellnessSensorManager(this)
        moodManager = MoodManager(this)
        
        // Setup shake detection for quick mood logging
        sensorManager.setOnShakeDetected {
            showQuickMoodDialog()
        }
        
        // Setup step detection
        sensorManager.setOnStepDetected { stepCount ->
            // Step count is now handled by HomeFragment
            // Could add additional logic here if needed
        }
        
        sensorManager.startListening()
    }
    
    private fun showQuickMoodDialog() {
        val moodOptions = MoodEntry.MOOD_OPTIONS
        val moodNames = moodOptions.map { "${it.emoji} ${it.name}" }.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Quick Mood Log")
            .setMessage("Shake detected! How are you feeling?")
            .setItems(moodNames) { _, which ->
                val selectedMood = moodOptions[which]
                val mood = MoodEntry(
                    emoji = selectedMood.emoji,
                    moodName = selectedMood.name,
                    notes = "Logged via shake gesture",
                    intensity = selectedMood.defaultIntensity,
                    date = Date()
                )
                moodManager.addMood(mood)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_habits -> {
                    loadFragment(HabitTrackerFragment())
                    true
                }
                R.id.nav_mood -> {
                    loadFragment(MoodJournalFragment())
                    true
                }
                R.id.nav_hydration -> {
                    loadFragment(HydrationFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    override fun onResume() {
        super.onResume()
        sensorManager.startListening()
    }
    
    override fun onPause() {
        super.onPause()
        sensorManager.stopListening()
    }
    
    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    fun logout() {
        authManager.logout()
        navigateToAuth()
    }
}