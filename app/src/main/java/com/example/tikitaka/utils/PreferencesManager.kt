package com.example.tikitaka.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("tikitaka_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_FULL_NAME = "user_full_name"
        private const val KEY_USER_PROFILE_IMAGE = "user_profile_image"
        private const val KEY_USER_TEAM_NAME = "user_team_name"
        private const val KEY_USER_TEAM_LOGO = "user_team_logo"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    // Auth Token
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    fun clearAuthToken() {
        sharedPreferences.edit().remove(KEY_AUTH_TOKEN).apply()
    }
    
    // User Data
    fun saveUserData(
        userId: Int,
        username: String,
        email: String,
        fullName: String,
        profileImage: String? = null,
        teamName: String,
        teamLogo: String? = null
    ) {
        sharedPreferences.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_FULL_NAME, fullName)
            putString(KEY_USER_PROFILE_IMAGE, profileImage)
            putString(KEY_USER_TEAM_NAME, teamName)
            putString(KEY_USER_TEAM_LOGO, teamLogo)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }
    
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }
    
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }
    
    fun getUserFullName(): String? {
        return sharedPreferences.getString(KEY_USER_FULL_NAME, null)
    }
    
    fun getUserProfileImage(): String? {
        return sharedPreferences.getString(KEY_USER_PROFILE_IMAGE, null)
    }
    
    fun getUserTeamName(): String? {
        return sharedPreferences.getString(KEY_USER_TEAM_NAME, null)
    }
    
    fun getUserTeamLogo(): String? {
        return sharedPreferences.getString(KEY_USER_TEAM_LOGO, null)
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    // Clear all user data
    fun clearUserData() {
        sharedPreferences.edit().apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_FULL_NAME)
            remove(KEY_USER_PROFILE_IMAGE)
            remove(KEY_USER_TEAM_NAME)
            remove(KEY_USER_TEAM_LOGO)
            remove(KEY_IS_LOGGED_IN)
            apply()
        }
    }
    
    // Update specific user data
    fun updateUserData(
        username: String? = null,
        fullName: String? = null,
        profileImage: String? = null,
        teamName: String? = null,
        teamLogo: String? = null
    ) {
        sharedPreferences.edit().apply {
            username?.let { putString(KEY_USERNAME, it) }
            fullName?.let { putString(KEY_USER_FULL_NAME, it) }
            profileImage?.let { putString(KEY_USER_PROFILE_IMAGE, it) }
            teamName?.let { putString(KEY_USER_TEAM_NAME, it) }
            teamLogo?.let { putString(KEY_USER_TEAM_LOGO, it) }
            apply()
        }
    }
}