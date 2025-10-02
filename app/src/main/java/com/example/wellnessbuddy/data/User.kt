package com.example.wellnessbuddy.data

import java.util.Date

/**
 * Data class representing a user account
 */
data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "", // In production, this should be hashed
    val createdAt: Date = Date(),
    val lastLogin: Date = Date(),
    val isActive: Boolean = true
)

/**
 * Data class for authentication result
 */
data class AuthResult(
    val success: Boolean,
    val user: User? = null,
    val errorMessage: String? = null
)
