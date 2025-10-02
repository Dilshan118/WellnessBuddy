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
import com.example.wellnessbuddy.theme.ThemeManager
import com.example.wellnessbuddy.dialogs.ThemeSelectorDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

/**
 * Fragment for app settings and preferences
 */
class SettingsFragment : Fragment() {
    
    private lateinit var hydrationManager: HydrationManager
    private lateinit var themeManager: ThemeManager
    private lateinit var hydrationGoalText: MaterialTextView
    private lateinit var notificationSettingsCard: MaterialCardView
    private lateinit var themeSettingsCard: MaterialCardView
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
        themeManager = ThemeManager(requireContext())
        
        hydrationGoalText = view.findViewById(R.id.hydration_goal_text)
        notificationSettingsCard = view.findViewById(R.id.notification_settings_card)
        themeSettingsCard = view.findViewById(R.id.theme_settings_card)
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
        
        aboutCard.setOnClickListener {
            showAboutDialog()
        }
        
        shareDataBtn.setOnClickListener {
            shareWellnessData()
        }
    }
    
    private fun loadSettings() {
        val hydrationSettings = hydrationManager.loadSettings()
        hydrationGoalText.text = "${hydrationSettings.dailyGoal} glasses per day"
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
    
    private fun showAboutDialog() {
        val message = """
            Wellness Buddy v1.0
            
            A simple and effective wellness tracking app to help you:
            • Track daily habits
            • Monitor your mood
            • Stay hydrated
            • Build healthy routines
            
            Built with ❤️ for your wellness journey.
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("About Wellness Buddy")
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
    
    override fun onResume() {
        super.onResume()
        loadSettings()
    }
}
