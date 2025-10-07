package com.example.wellnessbuddy.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.HydrationManager
import com.example.wellnessbuddy.data.AuthManager
import com.example.wellnessbuddy.data.OnboardingManager
import com.example.wellnessbuddy.theme.ThemeManager
import com.example.wellnessbuddy.dialogs.ThemeSelectorDialog
import com.example.wellnessbuddy.sensors.WellnessSensorManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

/**
 * Fragment for app settings and preferences
 */
class SettingsFragment : Fragment() {
    
    private lateinit var hydrationManager: HydrationManager
    private lateinit var authManager: AuthManager
    private lateinit var onboardingManager: OnboardingManager
    private lateinit var themeManager: ThemeManager
    private lateinit var sensorManager: WellnessSensorManager
    private lateinit var hydrationGoalText: MaterialTextView
    private lateinit var sensorStatusText: MaterialTextView
    private lateinit var notificationSettingsCard: MaterialCardView
    private lateinit var themeSettingsCard: MaterialCardView
    private lateinit var sensorSettingsCard: MaterialCardView
    private lateinit var aboutCard: MaterialCardView
    private lateinit var shareDataBtn: MaterialButton
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        hydrationManager = HydrationManager(requireContext())
        authManager = AuthManager(requireContext())
        onboardingManager = OnboardingManager(requireContext())
        themeManager = ThemeManager(requireContext())
        sensorManager = WellnessSensorManager(requireContext())
        
        hydrationGoalText = view.findViewById(R.id.hydration_goal_text)
        sensorStatusText = view.findViewById(R.id.sensor_status_text)
        notificationSettingsCard = view.findViewById(R.id.notification_settings_card)
        themeSettingsCard = view.findViewById(R.id.theme_settings_card)
        sensorSettingsCard = view.findViewById(R.id.sensor_settings_card)
        aboutCard = view.findViewById(R.id.about_card)
        shareDataBtn = view.findViewById(R.id.share_data_btn)
        
        setupViews()
        loadSettings()
    }
    
    private fun setupViews() {
        notificationSettingsCard.setOnClickListener {
            showNotificationSettingsDialog()
        }
        
        themeSettingsCard.setOnClickListener {
            showThemeSelectorDialog()
        }
        
        sensorSettingsCard.setOnClickListener {
            showSensorSettingsDialog()
        }
        
        aboutCard.setOnClickListener {
            showAboutDialog()
        }
        
        shareDataBtn.setOnClickListener {
            shareWellnessData()
        }
        
        // Add logout functionality
        view?.findViewById<MaterialCardView>(R.id.logout_card)?.setOnClickListener {
            showLogoutDialog()
        }
        
        // Add view onboarding functionality
        view?.findViewById<MaterialCardView>(R.id.view_onboarding_card)?.setOnClickListener {
            showViewOnboardingDialog()
        }
    }
    
    private fun loadSettings() {
        val hydrationSettings = hydrationManager.loadSettings()
        hydrationGoalText.text = "${hydrationSettings.dailyGoal} glasses per day"
        
        // Update sensor status
        updateSensorStatusDisplay()
    }
    
    private fun updateSensorStatusDisplay() {
        val isAvailable = sensorManager.isSensorAvailable()
        val isActive = sensorManager.isCurrentlyListening()
        
        sensorStatusText.text = when {
            !isAvailable -> "Not Available"
            isActive -> "Active"
            else -> "Inactive"
        }
        
        // Update text color based on status
        val color = when {
            !isAvailable -> R.color.status_error
            isActive -> R.color.neon_success
            else -> R.color.neon_warning
        }
        sensorStatusText.setTextColor(requireContext().getColor(color))
    }
    
    private fun showNotificationSettingsDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_notification_settings, null)
        val hydrationGoalEditText = dialogView.findViewById<EditText>(R.id.hydration_goal_edit)
        
        val currentSettings = hydrationManager.loadSettings()
        hydrationGoalEditText.setText(currentSettings.dailyGoal.toString())
        
        AlertDialog.Builder(requireContext())
            .setTitle("Notification Settings")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val dailyGoal = hydrationGoalEditText.text.toString().toIntOrNull() ?: 8
                val newSettings = currentSettings.copy(dailyGoal = dailyGoal)
                hydrationManager.saveSettings(newSettings)
                loadSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showThemeSelectorDialog() {
        val dialog = ThemeSelectorDialog.newInstance { selectedTheme ->
            themeManager.setTheme(selectedTheme)
            // Restart activity to apply theme changes
            activity?.recreate()
        }
        dialog.show(parentFragmentManager, "theme_selector")
    }
    
    private fun showSensorSettingsDialog() {
        val isAvailable = sensorManager.isSensorAvailable()
        val isActive = sensorManager.isCurrentlyListening()
        val stepCount = sensorManager.getStepCount()
        val lastAcceleration = sensorManager.getLastAcceleration()
        
        val message = """
            📱 Sensor Information
            
            Status: ${if (isAvailable) "Available" else "Not Available"}
            Active: ${if (isActive) "Yes" else "No"}
            
            📊 Current Data:
            • Steps Today: $stepCount
            • Last Acceleration: ${String.format("%.1f", lastAcceleration)} m/s²
            
            ⚙️ Settings:
            • Shake Threshold: ${sensorManager.getShakeThreshold()} m/s²
            • Step Threshold: ${sensorManager.getStepThreshold()} m/s²
            
            💡 Tips:
            • Shake your device to log moods quickly
            • Walk with your device to count steps
            • Sensors help track your activity automatically
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("📱 Sensor Settings")
            .setMessage(message)
            .setPositiveButton("Reset Steps") { _, _ ->
                sensorManager.resetStepCount()
                updateSensorStatusDisplay()
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun showAboutDialog() {
        val message = """
            🧘‍♀️ WellnessBuddy v1.0
            
            A modern wellness tracking app to help you:
            • Track daily habits
            • Monitor your mood
            • Stay hydrated
            • Smart sensor features (shake to log moods, auto step counting)
            • Build healthy routines
            
            Built with ❤️ for your wellness journey.
            
            💡 Tip: Try the "View Onboarding" option to see all features including the new sensor capabilities!
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("About WellnessBuddy")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun shareWellnessData() {
        val hydrationSettings = hydrationManager.loadSettings()
        val shareText = """
            My Wellness Summary 📊
            
            💧 Hydration Progress: ${hydrationSettings.glassesConsumed}/${hydrationSettings.dailyGoal} glasses today
            
            🌟 Keep up the great work on your wellness journey!
            
            #WellnessBuddy #HealthyLiving
        """.trimIndent()
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share Wellness Data"))
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout? You'll need to sign in again to access your data.")
            .setPositiveButton("Logout") { _, _ ->
                authManager.logout()
                // Navigate to auth activity
                val intent = Intent(requireContext(), com.example.wellnessbuddy.AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showViewOnboardingDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("View Onboarding")
            .setMessage("Would you like to view the onboarding screens again? This will take you through the app introduction.")
            .setPositiveButton("View Onboarding") { _, _ ->
                // Reset onboarding state and navigate to AuthActivity
                onboardingManager.resetOnboarding()
                val intent = Intent(requireContext(), com.example.wellnessbuddy.AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        loadSettings()
        updateSensorStatusDisplay()
    }
}
