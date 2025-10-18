package com.example.tikitaka

data class Post(
    val id: Int,
    val username: String,
    val date: String,
    val content: String,
    val imageUrl: String? = null,
    val likes: Int = 0,
    val isFavorite: Boolean = false
)