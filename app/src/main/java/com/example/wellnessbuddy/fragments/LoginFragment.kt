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
 * Fragment for user login
 */
class LoginFragment : Fragment() {
    
    private lateinit var authManager: AuthManager
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var signupLink: MaterialTextView
    private lateinit var loadingOverlay: View
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        authManager = AuthManager(requireContext())
        
        // Initialize views
        emailInput = view.findViewById(R.id.email_input)
        passwordInput = view.findViewById(R.id.password_input)
        loginButton = view.findViewById(R.id.login_button)
        signupLink = view.findViewById(R.id.signup_link)
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            performLogin()
        }
        
        signupLink.setOnClickListener {
            navigateToSignup()
        }
    }
    
    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields")
            return
        }
        
        showLoading(true)
        
        // Simulate network delay (in real app, this would be an async operation)
        view?.postDelayed({
            val result = authManager.loginUser(email, password)
            showLoading(false)
            
            if (result.success) {
                showSuccess("Login successful!")
                navigateToMain()
            } else {
                showError(result.errorMessage ?: "Login failed")
            }
        }, 1000)
    }
    
    private fun navigateToSignup() {
        findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
    }
    
    private fun navigateToMain() {
        findNavController().navigate(R.id.action_loginFragment_to_mainActivity)
    }
    
    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        loginButton.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
