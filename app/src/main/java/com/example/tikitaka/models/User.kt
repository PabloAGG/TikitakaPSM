package com.example.tikitaka.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("username") 
    val username: String,
    
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("profile_image")
    val profileImage: String? = null,
    
    @SerializedName("bio")
    val bio: String? = null,
    
    @SerializedName("team_name")
    val teamName: String,
    
    @SerializedName("team_logo")
    val teamLogo: String? = null,
    
    @SerializedName("posts_count")
    val postsCount: Int = 0,
    
    @SerializedName("favorites_count")
    val favoritesCount: Int = 0,
    
    @SerializedName("created_at")
    val createdAt: String
)