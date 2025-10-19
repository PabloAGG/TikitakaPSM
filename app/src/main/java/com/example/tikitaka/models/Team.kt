package com.example.tikitaka.models

import com.google.gson.annotations.SerializedName

data class Team(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("logo_url")
    val logoUrl: String? = null,
    
    @SerializedName("confederation")
    val confederation: String,
    
    @SerializedName("fans_count")
    val fansCount: Int = 0,
    
    @SerializedName("posts_count")
    val postsCount: Int = 0,
    
    @SerializedName("recent_posts")
    val recentPosts: List<Post>? = null,
    
    @SerializedName("top_fans")
    val topFans: List<User>? = null
)