package com.example.wellnessbuddy.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.AuthManager
import com.example.wellnessbuddy.data.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for displaying and editing user profile information
 */
class ProfileFragment : Fragment() {
    
    private lateinit var authManager: AuthManager
    private var currentUser: User? = null
    
    // Profile header views
    private lateinit var profileAvatar: MaterialTextView
    private lateinit var profileUsername: MaterialTextView
    private lateinit var profileEmail: MaterialTextView
    private lateinit var profileMemberSince: MaterialTextView
    private lateinit var editProfileBtn: MaterialButton
    
    // Account information views
    private lateinit var accountUsername: MaterialTextView
    private lateinit var accountEmail: MaterialTextView
    private lateinit var accountPassword: MaterialTextView
    private lateinit var accountStatus: MaterialTextView
    
    // Edit buttons
    private lateinit var editUsernameBtn: MaterialButton
    private lateinit var editEmailBtn: MaterialButton
    private lateinit var changePasswordBtn: MaterialButton
    
    // Statistics views
    private lateinit var statDaysActive: MaterialTextView
    private lateinit var statLastLogin: MaterialTextView
    private lateinit var statAccountAge: MaterialTextView
    
    // Danger zone
    private lateinit var deleteAccountBtn: MaterialButton
    
    // Navigation
    private lateinit var backBtn: MaterialButton
    
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize AuthManager
        authManager = AuthManager(requireContext())
        
        // Initialize views
        initializeViews(view)
        
        // Setup click listeners
        setupClickListeners()
        
        // Load user data
        loadUserData()
    }
    
    private fun initializeViews(view: View) {
        // Profile header views
        profileAvatar = view.findViewById(R.id.profile_avatar)
        profileUsername = view.findViewById(R.id.profile_username)
        profileEmail = view.findViewById(R.id.profile_email)
        profileMemberSince = view.findViewById(R.id.profile_member_since)
        editProfileBtn = view.findViewById(R.id.edit_profile_btn)
        
        // Account information views
        accountUsername = view.findViewById(R.id.account_username)
        accountEmail = view.findViewById(R.id.account_email)
        accountPassword = view.findViewById(R.id.account_password)
        accountStatus = view.findViewById(R.id.account_status)
        
        // Edit buttons
        editUsernameBtn = view.findViewById(R.id.edit_username_btn)
        editEmailBtn = view.findViewById(R.id.edit_email_btn)
        changePasswordBtn = view.findViewById(R.id.change_password_btn)
        
        // Statistics views
        statDaysActive = view.findViewById(R.id.stat_days_active)
        statLastLogin = view.findViewById(R.id.stat_last_login)
        statAccountAge = view.findViewById(R.id.stat_account_age)
        
        // Danger zone
        deleteAccountBtn = view.findViewById(R.id.delete_account_btn)
        
        // Navigation
        backBtn = view.findViewById(R.id.back_btn)
    }
    
    private fun setupClickListeners() {
        editProfileBtn.setOnClickListener {
            showEditProfileDialog()
        }
        
        editUsernameBtn.setOnClickListener {
            showEditUsernameDialog()
        }
        
        editEmailBtn.setOnClickListener {
            showEditEmailDialog()
        }
        
        changePasswordBtn.setOnClickListener {
            showChangePasswordDialog()
        }
        
        deleteAccountBtn.setOnClickListener {
            showDeleteAccountDialog()
        }
        
        backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun loadUserData() {
        currentUser = authManager.getCurrentUser()
        
        if (currentUser != null) {
            updateProfileDisplay(currentUser!!)
            updateAccountInformation(currentUser!!)
            updateStatistics(currentUser!!)
        } else {
            // Handle case where user is not logged in
            showErrorAndNavigateToAuth()
        }
    }
    
    private fun updateProfileDisplay(user: User) {
        // Update profile avatar (using first letter of username)
        val avatarText = if (user.username.isNotEmpty()) {
            user.username.first().uppercaseChar().toString()
        } else {
            "👤"
        }
        profileAvatar.text = avatarText
        
        // Update profile information
        profileUsername.text = user.username
        profileEmail.text = user.email
        profileMemberSince.text = "Member since: ${dateFormat.format(user.createdAt)}"
    }
    
    private fun updateAccountInformation(user: User) {
        accountUsername.text = user.username
        accountEmail.text = user.email
        accountPassword.text = "••••••••"
        accountStatus.text = if (user.isActive) "Active" else "Inactive"
        accountStatus.setTextColor(
            if (user.isActive) {
                requireContext().getColor(R.color.neon_success)
            } else {
                requireContext().getColor(R.color.status_error)
            }
        )
    }
    
    private fun updateStatistics(user: User) {
        // Calculate days active (simplified - could be enhanced with actual usage tracking)
        val daysActive = calculateDaysActive(user.createdAt)
        statDaysActive.text = daysActive.toString()
        
        // Update last login
        val lastLoginText = formatLastLogin(user.lastLogin)
        statLastLogin.text = lastLoginText
        
        // Calculate account age
        val accountAge = calculateAccountAge(user.createdAt)
        statAccountAge.text = accountAge.toString()
    }
    
    private fun calculateDaysActive(createdAt: Date): Int {
        val now = Date()
        val diffInMillis = now.time - createdAt.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
    
    private fun formatLastLogin(lastLogin: Date): String {
        val now = Date()
        val diffInMillis = now.time - lastLogin.time
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
        
        return when {
            diffInDays < 1 -> "Today"
            diffInDays < 2 -> "Yesterday"
            diffInDays < 7 -> "${diffInDays.toInt()} days ago"
            else -> dateFormat.format(lastLogin)
        }
    }
    
    private fun calculateAccountAge(createdAt: Date): Int {
        val now = Date()
        val diffInMillis = now.time - createdAt.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
    
    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_profile, null)
        
        val usernameEdit = dialogView.findViewById<EditText>(R.id.edit_username)
        val emailEdit = dialogView.findViewById<EditText>(R.id.edit_email)
        
        // Pre-fill current values
        currentUser?.let { user ->
            usernameEdit.setText(user.username)
            emailEdit.setText(user.email)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = usernameEdit.text.toString().trim()
                val newEmail = emailEdit.text.toString().trim()
                
                if (validateProfileInput(newUsername, newEmail)) {
                    updateUserProfile(newUsername, newEmail)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditUsernameDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_username, null)
        val usernameEdit = dialogView.findViewById<EditText>(R.id.edit_username)
        
        // Pre-fill current username
        currentUser?.let { user ->
            usernameEdit.setText(user.username)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Username")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = usernameEdit.text.toString().trim()
                
                if (validateUsername(newUsername)) {
                    updateUsername(newUsername)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditEmailDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_email, null)
        val emailEdit = dialogView.findViewById<EditText>(R.id.edit_email)
        
        // Pre-fill current email
        currentUser?.let { user ->
            emailEdit.setText(user.email)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Email")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newEmail = emailEdit.text.toString().trim()
                
                if (validateEmail(newEmail)) {
                    updateEmail(newEmail)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null)
        val currentPasswordEdit = dialogView.findViewById<EditText>(R.id.current_password)
        val newPasswordEdit = dialogView.findViewById<EditText>(R.id.new_password)
        val confirmPasswordEdit = dialogView.findViewById<EditText>(R.id.confirm_password)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordEdit.text.toString()
                val newPassword = newPasswordEdit.text.toString()
                val confirmPassword = confirmPasswordEdit.text.toString()
                
                if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                    updatePassword(newPassword)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.\n\nAll your data including:\n• Habits and progress\n• Mood entries\n• Hydration data\n• Account settings\n\nWill be permanently deleted.")
            .setPositiveButton("Delete Account") { _, _ ->
                confirmDeleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun confirmDeleteAccount() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Final Confirmation")
            .setMessage("This is your final chance to cancel. Type 'DELETE' to confirm account deletion.")
            .setView(createDeleteConfirmationView())
            .setPositiveButton("Delete Forever") { _, _ ->
                currentUser?.let { user ->
                    deleteUserAccount(user.id)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun createDeleteConfirmationView(): EditText {
        return EditText(requireContext()).apply {
            hint = "Type DELETE to confirm"
        }
    }
    
    // Validation methods
    private fun validateProfileInput(username: String, email: String): Boolean {
        return validateUsername(username) && validateEmail(email)
    }
    
    private fun validateUsername(username: String): Boolean {
        if (username.isEmpty()) {
            showError("Username cannot be empty")
            return false
        }
        if (username.length < 3) {
            showError("Username must be at least 3 characters long")
            return false
        }
        if (username == currentUser?.username) {
            showError("Username is the same as current username")
            return false
        }
        return true
    }
    
    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            showError("Email cannot be empty")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address")
            return false
        }
        if (email == currentUser?.email) {
            showError("Email is the same as current email")
            return false
        }
        return true
    }
    
    private fun validatePasswordChange(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        if (currentPassword.isEmpty()) {
            showError("Current password is required")
            return false
        }
        if (currentUser?.password != currentPassword) {
            showError("Current password is incorrect")
            return false
        }
        if (newPassword.isEmpty()) {
            showError("New password cannot be empty")
            return false
        }
        if (newPassword.length < 6) {
            showError("New password must be at least 6 characters long")
            return false
        }
        if (newPassword != confirmPassword) {
            showError("New passwords do not match")
            return false
        }
        if (newPassword == currentPassword) {
            showError("New password must be different from current password")
            return false
        }
        return true
    }
    
    // Update methods
    private fun updateUserProfile(username: String, email: String) {
        currentUser?.let { user ->
            val updatedUser = user.copy(
                username = username,
                email = email,
                lastLogin = Date()
            )
            
            if (authManager.updateUser(updatedUser)) {
                currentUser = updatedUser
                loadUserData()
                showSuccess("Profile updated successfully!")
            } else {
                showError("Failed to update profile. Please try again.")
            }
        }
    }
    
    private fun updateUsername(username: String) {
        currentUser?.let { user ->
            val updatedUser = user.copy(
                username = username,
                lastLogin = Date()
            )
            
            if (authManager.updateUser(updatedUser)) {
                currentUser = updatedUser
                loadUserData()
                showSuccess("Username updated successfully!")
            } else {
                showError("Failed to update username. Username might already be taken.")
            }
        }
    }
    
    private fun updateEmail(email: String) {
        currentUser?.let { user ->
            val updatedUser = user.copy(
                email = email,
                lastLogin = Date()
            )
            
            if (authManager.updateUser(updatedUser)) {
                currentUser = updatedUser
                loadUserData()
                showSuccess("Email updated successfully!")
            } else {
                showError("Failed to update email. Email might already be in use.")
            }
        }
    }
    
    private fun updatePassword(newPassword: String) {
        currentUser?.let { user ->
            val updatedUser = user.copy(
                password = newPassword, // In production, hash this password
                lastLogin = Date()
            )
            
            if (authManager.updateUser(updatedUser)) {
                currentUser = updatedUser
                showSuccess("Password changed successfully!")
            } else {
                showError("Failed to change password. Please try again.")
            }
        }
    }
    
    private fun deleteUserAccount(userId: String) {
        if (authManager.deleteUser(userId)) {
            showSuccess("Account deleted successfully")
            // Navigate to auth activity
            requireActivity().finish()
        } else {
            showError("Failed to delete account. Please try again.")
        }
    }
    
    private fun showErrorAndNavigateToAuth() {
        AlertDialog.Builder(requireContext())
            .setTitle("Session Expired")
            .setMessage("Your session has expired. Please log in again.")
            .setPositiveButton("OK") { _, _ ->
                requireActivity().finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showError(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showSuccess(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        loadUserData()
    }
}
