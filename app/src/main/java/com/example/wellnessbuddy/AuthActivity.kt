package com.example.wellnessbuddy

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.wellnessbuddy.data.AuthManager
import com.example.wellnessbuddy.data.OnboardingManager
import com.example.wellnessbuddy.fragments.LoginFragment
import com.example.wellnessbuddy.fragments.SignupFragment

/**
 * Activity for handling authentication flow
 */
class AuthActivity : AppCompatActivity() {
    
    private lateinit var authManager: AuthManager
    private lateinit var onboardingManager: OnboardingManager
    private lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.auth_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        authManager = AuthManager(this)
        onboardingManager = OnboardingManager(this)
        
        // Setup navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.auth_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        
        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            navigateToMain()
        } else {
            // Check if onboarding is needed
            if (!onboardingManager.isOnboardingCompleted()) {
                startOnboarding()
            } else {
                // Start with login fragment
                if (savedInstanceState == null) {
                    navController.navigate(R.id.loginFragment)
                }
            }
        }
    }
    
    fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun startOnboarding() {
        val bundle = Bundle().apply {
            putInt("pageIndex", 0)
        }
        navController.navigate(R.id.onboardingFragment, bundle)
    }
    
    fun completeOnboarding() {
        onboardingManager.completeOnboarding()
    }
    
    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.loginFragment) {
            // If on login screen, exit app
            super.onBackPressed()
        } else {
            // Otherwise, navigate back in the auth flow
            navController.navigateUp()
        }
    }
}
