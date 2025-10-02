package com.example.wellnessbuddy.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.OnboardingPage
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

/**
 * Fragment for displaying onboarding screens
 */
class OnboardingFragment : Fragment() {
    
    private lateinit var onboardingIcon: MaterialTextView
    private lateinit var onboardingTitle: MaterialTextView
    private lateinit var onboardingDescription: MaterialTextView
    private lateinit var pageIndicator: ViewGroup
    private lateinit var skipButton: MaterialTextView
    private lateinit var previousButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var gradientBackground: View
    
    private var currentPageIndex = 0
    private var totalPages = 5
    private var onboardingPages: List<OnboardingPage> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        onboardingIcon = view.findViewById(R.id.onboarding_icon)
        onboardingTitle = view.findViewById(R.id.onboarding_title)
        onboardingDescription = view.findViewById(R.id.onboarding_description)
        pageIndicator = view.findViewById(R.id.page_indicator)
        skipButton = view.findViewById(R.id.skip_button)
        previousButton = view.findViewById(R.id.previous_button)
        nextButton = view.findViewById(R.id.next_button)
        gradientBackground = view.findViewById(R.id.gradient_background)
        
        // Get onboarding pages
        onboardingPages = com.example.wellnessbuddy.data.OnboardingManager(requireContext()).getOnboardingPages()
        totalPages = onboardingPages.size
        
        // Get current page index from arguments
        currentPageIndex = arguments?.getInt("pageIndex", 0) ?: 0
        
        setupClickListeners()
        loadPageData()
        setupPageIndicator()
        animateContent()
    }
    
    private fun setupClickListeners() {
        skipButton.setOnClickListener {
            navigateToAuth()
        }
        
        previousButton.setOnClickListener {
            navigateToPrevious()
        }
        
        nextButton.setOnClickListener {
            if (currentPageIndex == totalPages - 1) {
                navigateToAuth()
            } else {
                navigateToNext()
            }
        }
    }
    
    private fun loadPageData() {
        if (currentPageIndex < onboardingPages.size) {
            val page = onboardingPages[currentPageIndex]
            
            onboardingIcon.text = page.icon
            onboardingTitle.text = page.title
            onboardingDescription.text = page.description
            
            // Update button text for last page
            if (page.isLastPage) {
                nextButton.text = getString(R.string.get_started)
                nextButton.setIconResource(R.drawable.ic_check)
                skipButton.visibility = View.GONE
            } else {
                nextButton.text = getString(R.string.next)
                nextButton.setIconResource(R.drawable.ic_arrow_forward)
                skipButton.visibility = View.VISIBLE
            }
            
            // Show/hide previous button
            previousButton.visibility = if (currentPageIndex > 0) View.VISIBLE else View.GONE
            
            // Update gradient background based on page
            updateGradientBackground(page.backgroundColor)
        }
    }
    
    private fun setupPageIndicator() {
        pageIndicator.removeAllViews()
        
        for (i in 0 until totalPages) {
            val indicator = LayoutInflater.from(context)
                .inflate(R.layout.item_page_indicator, pageIndicator, false)
            
            val indicatorView = indicator.findViewById<View>(R.id.indicator_dot)
            
            if (i == currentPageIndex) {
                indicatorView.setBackgroundResource(R.drawable.indicator_active)
            } else {
                indicatorView.setBackgroundResource(R.drawable.indicator_inactive)
            }
            
            pageIndicator.addView(indicator)
        }
    }
    
    private fun updateGradientBackground(gradientType: String) {
        val gradientRes = when (gradientType) {
            "gradient_1" -> R.drawable.gradient_background
            "gradient_2" -> R.drawable.gradient_background_2
            "gradient_3" -> R.drawable.gradient_background_3
            else -> R.drawable.gradient_background
        }
        
        gradientBackground.setBackgroundResource(gradientRes)
    }
    
    private fun animateContent() {
        // Animate icon with bounce effect
        val iconScaleX = ObjectAnimator.ofFloat(onboardingIcon, "scaleX", 0f, 1f)
        val iconScaleY = ObjectAnimator.ofFloat(onboardingIcon, "scaleY", 0f, 1f)
        val iconAnimator = AnimatorSet().apply {
            playTogether(iconScaleX, iconScaleY)
            duration = 800
            interpolator = OvershootInterpolator(1.2f)
        }
        
        // Animate title with slide up
        val titleTranslationY = ObjectAnimator.ofFloat(onboardingTitle, "translationY", 100f, 0f)
        val titleAlpha = ObjectAnimator.ofFloat(onboardingTitle, "alpha", 0f, 1f)
        val titleAnimator = AnimatorSet().apply {
            playTogether(titleTranslationY, titleAlpha)
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        // Animate description with slide up and delay
        val descTranslationY = ObjectAnimator.ofFloat(onboardingDescription, "translationY", 100f, 0f)
        val descAlpha = ObjectAnimator.ofFloat(onboardingDescription, "alpha", 0f, 1f)
        val descAnimator = AnimatorSet().apply {
            playTogether(descTranslationY, descAlpha)
            duration = 600
            startDelay = 200
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        // Animate buttons with fade in
        val buttonAlpha = ObjectAnimator.ofFloat(nextButton, "alpha", 0f, 1f)
        val buttonTranslationY = ObjectAnimator.ofFloat(nextButton, "translationY", 50f, 0f)
        val buttonAnimator = AnimatorSet().apply {
            playTogether(buttonAlpha, buttonTranslationY)
            duration = 500
            startDelay = 400
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        // Start animations
        iconAnimator.start()
        titleAnimator.start()
        descAnimator.start()
        buttonAnimator.start()
    }
    
    private fun navigateToNext() {
        val nextIndex = currentPageIndex + 1
        if (nextIndex < totalPages) {
            val bundle = Bundle().apply {
                putInt("pageIndex", nextIndex)
            }
            findNavController().navigate(R.id.action_onboardingFragment_self, bundle)
        }
    }
    
    private fun navigateToPrevious() {
        val prevIndex = currentPageIndex - 1
        if (prevIndex >= 0) {
            val bundle = Bundle().apply {
                putInt("pageIndex", prevIndex)
            }
            findNavController().navigate(R.id.action_onboardingFragment_self, bundle)
        }
    }
    
    private fun navigateToAuth() {
        // Complete onboarding before navigating
        (requireActivity() as com.example.wellnessbuddy.AuthActivity).completeOnboarding()
        findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
    }
}
