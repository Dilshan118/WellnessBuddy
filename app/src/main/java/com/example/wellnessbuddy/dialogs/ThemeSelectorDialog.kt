package com.example.wellnessbuddy.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.theme.ThemeManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

/**
 * Modern bottom sheet dialog for theme selection
 */
class ThemeSelectorDialog : BottomSheetDialogFragment() {
    
    private var onThemeSelected: ((String) -> Unit)? = null
    
    companion object {
        fun newInstance(onThemeSelected: (String) -> Unit): ThemeSelectorDialog {
            return ThemeSelectorDialog().apply {
                this.onThemeSelected = onThemeSelected
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_theme_selector, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val themeManager = ThemeManager(requireContext())
        val currentTheme = themeManager.getCurrentTheme()
        
        // Theme option cards
        val lightCard = view.findViewById<MaterialCardView>(R.id.light_theme_card)
        val darkCard = view.findViewById<MaterialCardView>(R.id.dark_theme_card)
        val systemCard = view.findViewById<MaterialCardView>(R.id.system_theme_card)
        
        // Theme option buttons
        val lightBtn = view.findViewById<MaterialButton>(R.id.light_theme_btn)
        val darkBtn = view.findViewById<MaterialButton>(R.id.dark_theme_btn)
        val systemBtn = view.findViewById<MaterialButton>(R.id.system_theme_btn)
        
        // Update selection state
        updateSelectionState(lightCard, lightBtn, currentTheme == ThemeManager.THEME_LIGHT)
        updateSelectionState(darkCard, darkBtn, currentTheme == ThemeManager.THEME_DARK)
        updateSelectionState(systemCard, systemBtn, currentTheme == ThemeManager.THEME_SYSTEM)
        
        // Set click listeners
        lightBtn.setOnClickListener {
            onThemeSelected?.invoke(ThemeManager.THEME_LIGHT)
            dismiss()
        }
        
        darkBtn.setOnClickListener {
            onThemeSelected?.invoke(ThemeManager.THEME_DARK)
            dismiss()
        }
        
        systemBtn.setOnClickListener {
            onThemeSelected?.invoke(ThemeManager.THEME_SYSTEM)
            dismiss()
        }
    }
    
    private fun updateSelectionState(card: MaterialCardView, button: MaterialButton, isSelected: Boolean) {
        if (isSelected) {
            card.strokeWidth = 3
            card.strokeColor = requireContext().getColor(R.color.neon_primary)
            button.setBackgroundColor(requireContext().getColor(R.color.neon_primary))
            button.setTextColor(requireContext().getColor(R.color.text_primary))
        } else {
            card.strokeWidth = 1
            card.strokeColor = requireContext().getColor(R.color.glass_border)
            button.setBackgroundColor(requireContext().getColor(R.color.modern_surface))
            button.setTextColor(requireContext().getColor(R.color.text_secondary))
        }
    }
}
