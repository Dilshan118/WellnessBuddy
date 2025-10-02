package com.example.wellnessbuddy.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.Date

/**
 * Manager class for handling user authentication with SharedPreferences
 */
class AuthManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val CURRENT_USER_KEY = "current_user"
        private const val IS_LOGGED_IN_KEY = "is_logged_in"
        private const val USERS_KEY = "users_list"
    }
    
    /**
     * Register a new user
     */
    fun registerUser(email: String, username: String, password: String): AuthResult {
        // Validate input
        if (email.isBlank() || username.isBlank() || password.isBlank()) {
            return AuthResult(false, errorMessage = "All fields are required")
        }
        
        if (!isValidEmail(email)) {
            return AuthResult(false, errorMessage = "Please enter a valid email address")
        }
        
        if (password.length < 6) {
            return AuthResult(false, errorMessage = "Password must be at least 6 characters")
        }
        
        // Check if user already exists
        val users = loadUsers()
        if (users.any { it.email == email }) {
            return AuthResult(false, errorMessage = "Email already registered")
        }
        
        if (users.any { it.username == username }) {
            return AuthResult(false, errorMessage = "Username already taken")
        }
        
        // Create new user
        val newUser = User(
            id = generateUserId(),
            email = email,
            username = username,
            password = password, // In production, hash this password
            createdAt = Date()
        )
        
        // Save user
        val updatedUsers = users.toMutableList()
        updatedUsers.add(newUser)
        saveUsers(updatedUsers)
        
        return AuthResult(true, newUser)
    }
    
    /**
     * Login user
     */
    fun loginUser(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult(false, errorMessage = "Email and password are required")
        }
        
        val users = loadUsers()
        val user = users.find { it.email == email && it.password == password }
        
        if (user != null) {
            // Update last login
            val updatedUser = user.copy(lastLogin = Date())
            val updatedUsers = users.toMutableList()
            val index = updatedUsers.indexOfFirst { it.id == user.id }
            if (index != -1) {
                updatedUsers[index] = updatedUser
                saveUsers(updatedUsers)
            }
            
            // Set current user
            setCurrentUser(updatedUser)
            setLoggedIn(true)
            
            return AuthResult(true, updatedUser)
        } else {
            return AuthResult(false, errorMessage = "Invalid email or password")
        }
    }
    
    /**
     * Logout current user
     */
    fun logout() {
        setCurrentUser(null)
        setLoggedIn(false)
    }
    
    /**
     * Get current logged in user
     */
    fun getCurrentUser(): User? {
        val userJson = prefs.getString(CURRENT_USER_KEY, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN_KEY, false)
    }
    
    /**
     * Update user profile
     */
    fun updateUser(updatedUser: User): Boolean {
        val users = loadUsers()
        val index = users.indexOfFirst { it.id == updatedUser.id }
        
        if (index != -1) {
            val updatedUsers = users.toMutableList()
            updatedUsers[index] = updatedUser
            saveUsers(updatedUsers)
            
            // Update current user if it's the same user
            val currentUser = getCurrentUser()
            if (currentUser?.id == updatedUser.id) {
                setCurrentUser(updatedUser)
            }
            
            return true
        }
        
        return false
    }
    
    /**
     * Delete user account
     */
    fun deleteUser(userId: String): Boolean {
        val users = loadUsers()
        val updatedUsers = users.filter { it.id != userId }
        
        if (updatedUsers.size < users.size) {
            saveUsers(updatedUsers)
            
            // Logout if deleting current user
            val currentUser = getCurrentUser()
            if (currentUser?.id == userId) {
                logout()
            }
            
            return true
        }
        
        return false
    }
    
    // Private helper methods
    private fun loadUsers(): List<User> {
        val usersJson = prefs.getString(USERS_KEY, null)
        return if (usersJson != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<User>>() {}.type
                gson.fromJson(usersJson, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    private fun saveUsers(users: List<User>) {
        val usersJson = gson.toJson(users)
        prefs.edit().putString(USERS_KEY, usersJson).apply()
    }
    
    private fun setCurrentUser(user: User?) {
        if (user != null) {
            val userJson = gson.toJson(user)
            prefs.edit().putString(CURRENT_USER_KEY, userJson).apply()
        } else {
            prefs.edit().remove(CURRENT_USER_KEY).apply()
        }
    }
    
    private fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(IS_LOGGED_IN_KEY, isLoggedIn).apply()
    }
    
    private fun generateUserId(): String {
        return "user_${System.currentTimeMillis()}_${(0..999).random()}"
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
