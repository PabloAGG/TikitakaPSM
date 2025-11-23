package com.example.tikitaka.models

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("profile_image")
    val profileImage: String?,
    
    @SerializedName("likes_count")
    val likesCount: Int,
    
    @SerializedName("is_liked")
    val isLiked: Boolean
)

data class CommentRequest(
    @SerializedName("content")
    val content: String
)

data class CommentsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("comments")
    val comments: List<Comment>,
    
    @SerializedName("pagination")
    val pagination: Pagination? = null
)

data class CommentLikeResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("is_liked")
    val isLiked: Boolean,
    
    @SerializedName("likes_count")
    val likesCount: Int
)
