package com.example.wellnessbuddy.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.HydrationManager
import com.example.wellnessbuddy.data.HydrationSettings
import com.example.wellnessbuddy.notifications.HydrationReminderManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView

/**
 * Fragment for hydration tracking and reminders
 */
class HydrationFragment : Fragment() {
    
    private lateinit var hydrationManager: HydrationManager
    private lateinit var reminderManager: HydrationReminderManager
    private lateinit var hydrationProgress: CircularProgressIndicator
    private lateinit var hydrationText: MaterialTextView
    private lateinit var addGlassBtn: MaterialButton
    private lateinit var removeGlassBtn: MaterialButton
    private lateinit var settingsBtn: MaterialButton
    private lateinit var hydrationCard: MaterialCardView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hydration, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        hydrationManager = HydrationManager(requireContext())
        reminderManager = HydrationReminderManager(requireContext())
        
        hydrationProgress = view.findViewById(R.id.hydration_progress)
        hydrationText = view.findViewById(R.id.hydration_text)
        addGlassBtn = view.findViewById(R.id.add_glass_btn)
        removeGlassBtn = view.findViewById(R.id.remove_glass_btn)
        settingsBtn = view.findViewById(R.id.settings_btn)
        hydrationCard = view.findViewById(R.id.hydration_card)
        
        setupViews()
        loadData()
    }
    
    private fun setupViews() {
        addGlassBtn.setOnClickListener {
            hydrationManager.addGlass()
            loadData()
        }
        
        removeGlassBtn.setOnClickListener {
            hydrationManager.removeGlass()
            loadData()
        }
        
        settingsBtn.setOnClickListener {
            showSettingsDialog()
        }
        
        hydrationCard.setOnClickListener {
            showSettingsDialog()
        }
    }
    
    private fun loadData() {
        val settings = hydrationManager.loadSettings()
        updateHydrationDisplay(settings)
    }
    
    private fun updateHydrationDisplay(settings: HydrationSettings) {
        val progress = settings.getCompletionPercentage()
        hydrationProgress.progress = (progress * 100).toInt()
        hydrationText.text = "${settings.glassesConsumed}/${settings.dailyGoal} glasses"
        
        // Update button states
        removeGlassBtn.isEnabled = settings.glassesConsumed > 0
        
        // Show completion message
        if (settings.isGoalReached()) {
            hydrationText.text = "🎉 Goal reached! ${settings.glassesConsumed}/${settings.dailyGoal} glasses"
        } else {
            val remaining = settings.getRemainingGlasses()
            hydrationText.text = "${settings.glassesConsumed}/${settings.dailyGoal} glasses (${remaining} remaining)"
        }
    }
    
    private fun showSettingsDialog() {
        // Redirect to the advanced notification settings in SettingsFragment
        AlertDialog.Builder(requireContext())
            .setTitle("💧 Hydration Settings")
            .setMessage("For advanced hydration reminder settings including minute-based intervals, please go to:\n\nSettings → 🔔 Notification Settings\n\nThis will give you access to:\n• Minute-based reminders\n• Custom time ranges\n• Quick presets\n• Test notifications")
            .setPositiveButton("Go to Settings") { _, _ ->
                // Navigate to settings fragment
                findNavController().navigate(R.id.action_nav_hydration_to_nav_settings)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        loadData()
    }
}
