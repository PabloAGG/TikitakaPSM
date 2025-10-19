package com.example.tikitaka.models

import com.google.gson.annotations.SerializedName

// Requests para autenticación
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

data class RegisterRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("team_id")
    val teamId: Int
)

// Requests para posts
data class CreatePostRequest(
    @SerializedName("content")
    val content: String,
    
    @SerializedName("team_id")
    val teamId: Int,
    
    @SerializedName("is_draft")
    val isDraft: Boolean = false
)

data class UpdatePostRequest(
    @SerializedName("content")
    val content: String,
    
    @SerializedName("team_id")
    val teamId: Int,
    
    @SerializedName("is_draft")
    val isDraft: Boolean = false
)

// Requests para usuarios
data class UpdateProfileRequest(
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("full_name")
    val fullName: String? = null,
    
    @SerializedName("team_id")
    val teamId: Int? = null,
    
    @SerializedName("bio")
    val bio: String? = null
)

data class ChangePasswordRequest(
    @SerializedName("current_password")
    val currentPassword: String,
    
    @SerializedName("new_password")
    val newPassword: String
)