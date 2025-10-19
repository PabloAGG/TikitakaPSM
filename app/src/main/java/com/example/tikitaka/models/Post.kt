package com.example.tikitaka.models

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("team_id")
    val teamId: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("profile_image")
    val profileImage: String? = null,
    
    @SerializedName("team_name")
    val teamName: String,
    
    @SerializedName("team_logo")
    val teamLogo: String? = null,
    
    @SerializedName("likes_count")
    val likesCount: Int = 0,
    
    @SerializedName("favorites_count")
    val favoritesCount: Int = 0,
    
    @SerializedName("is_liked")
    val isLiked: Boolean = false,
    
    @SerializedName("is_favorited")
    val isFavorited: Boolean = false,
    
    @SerializedName("is_draft")
    val isDraft: Boolean = false,
    
    @SerializedName("created_at")
    val createdAt: String
)