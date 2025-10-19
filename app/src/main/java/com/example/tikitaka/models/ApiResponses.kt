package com.example.tikitaka.models

import com.google.gson.annotations.SerializedName

// Respuesta genérica de la API
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("errors")
    val errors: List<ApiError>? = null
)

data class ApiError(
    @SerializedName("field")
    val field: String? = null,
    
    @SerializedName("message")
    val message: String
)

// Respuestas específicas para autenticación
data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("token")
    val token: String? = null,
    
    @SerializedName("user")
    val user: User? = null,
    
    @SerializedName("errors")
    val errors: List<ApiError>? = null
)

data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("token")
    val token: String? = null,
    
    @SerializedName("user")
    val user: User? = null,
    
    @SerializedName("errors")
    val errors: List<ApiError>? = null
)

// Respuesta para posts
data class PostsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("posts")
    val posts: List<Post>,
    
    @SerializedName("pagination")
    val pagination: Pagination? = null
)

// Respuesta para equipos
data class TeamsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("teams")
    val teams: List<Team>,
    
    @SerializedName("teams_by_confederation")
    val teamsByConfederation: Map<String, List<Team>>? = null
)

// Respuesta para usuarios
data class UsersResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("users")
    val users: List<User>,
    
    @SerializedName("pagination")
    val pagination: Pagination? = null
)

// Paginación
data class Pagination(
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("limit")
    val limit: Int,
    
    @SerializedName("hasMore")
    val hasMore: Boolean
)