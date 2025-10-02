package com.example.wellnessbuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.AuthManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

/**
 * Fragment for user registration
 */
class SignupFragment : Fragment() {
    
    private lateinit var authManager: AuthManager
    private lateinit var usernameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var signupButton: MaterialButton
    private lateinit var loginLink: MaterialTextView
    private lateinit var loadingOverlay: View
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        authManager = AuthManager(requireContext())
        
        // Initialize views
        usernameInput = view.findViewById(R.id.username_input)
        emailInput = view.findViewById(R.id.email_input)
        passwordInput = view.findViewById(R.id.password_input)
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input)
        signupButton = view.findViewById(R.id.signup_button)
        loginLink = view.findViewById(R.id.login_link)
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        signupButton.setOnClickListener {
            performSignup()
        }
        
        loginLink.setOnClickListener {
            navigateToLogin()
        }
    }
    
    private fun performSignup() {
        val username = usernameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields")
            return
        }
        
        if (password != confirmPassword) {
            showError("Passwords do not match")
            return
        }
        
        if (password.length < 6) {
            showError("Password must be at least 6 characters")
            return
        }
        
        showLoading(true)
        
        // Simulate network delay (in real app, this would be an async operation)
        view?.postDelayed({
            val result = authManager.registerUser(email, username, password)
            showLoading(false)
            
            if (result.success) {
                showSuccess("Registration successful!")
                navigateToMain()
            } else {
                showError(result.errorMessage ?: "Registration failed")
            }
        }, 1000)
    }
    
    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
    }
    
    private fun navigateToMain() {
        findNavController().navigate(R.id.action_signupFragment_to_mainActivity)
    }
    
    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        signupButton.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
