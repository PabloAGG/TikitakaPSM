package com.example.tikitaka.api

import com.example.tikitaka.models.*
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ==================== AUTENTICACIÓN ====================
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/verify-token")
    suspend fun verifyToken(): Response<ApiResponse<User>>
    
    @POST("auth/refresh-token")
    suspend fun refreshToken(): Response<ApiResponse<String>>
    
    // ==================== USUARIOS ====================
    @GET("users/profile")
    suspend fun getUserProfile(): Response<ApiResponse<User>>
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<ApiResponse<User>>
    
    @PUT("users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<User>>
    
    @Multipart
    @POST("users/upload-avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): Response<ApiResponse<String>>
    
    @PUT("users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<String>>
    
    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<UsersResponse>
    
    // ==================== POSTS ====================
    @GET("posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("team_id") teamId: Int? = null
    ): Response<PostsResponse>
    
    @GET("posts/user/{userId}")
    suspend fun getUserPosts(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PostsResponse>
    
    @GET("posts/drafts")
    suspend fun getDrafts(): Response<ApiResponse<List<Post>>>
    
    @GET("posts/favorites")
    suspend fun getFavorites(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PostsResponse>
    
    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<ApiResponse<Post>>
    
    @PUT("posts/{id}")
    suspend fun updatePost(
        @Path("id") postId: Int,
        @Body request: UpdatePostRequest
    ): Response<ApiResponse<Post>>
    
    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") postId: Int): Response<ApiResponse<String>>
    
    @POST("posts/{id}/like")
    suspend fun toggleLike(@Path("id") postId: Int): Response<ApiResponse<LikeResponse>>
    
    @POST("posts/{id}/favorite")
    suspend fun toggleFavorite(@Path("id") postId: Int): Response<ApiResponse<FavoriteResponse>>
    
    @Multipart
    @POST("posts/upload")
    suspend fun uploadPostImage(@Part image: MultipartBody.Part): Response<ApiResponse<String>>
    
    // ==================== EQUIPOS ====================
    @GET("teams")
    suspend fun getTeams(): Response<TeamsResponse>
    
    @GET("teams/{id}")
    suspend fun getTeam(@Path("id") teamId: Int): Response<ApiResponse<Team>>
    
    @GET("teams/{id}/posts")
    suspend fun getTeamPosts(
        @Path("id") teamId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PostsResponse>
    
    @GET("teams/{id}/fans")
    suspend fun getTeamFans(
        @Path("id") teamId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<UsersResponse>
    
    @GET("teams/confederation/{confederation}")
    suspend fun getTeamsByConfederation(@Path("confederation") confederation: String): Response<TeamsResponse>
    
    @GET("teams/search")
    suspend fun searchTeams(@Query("q") query: String): Response<TeamsResponse>
    
    @GET("teams/stats/popular")
    suspend fun getPopularTeams(@Query("limit") limit: Int = 10): Response<TeamsResponse>
    
    @GET("teams/stats/active")
    suspend fun getActiveTeams(
        @Query("limit") limit: Int = 10,
        @Query("days") days: Int = 7
    ): Response<TeamsResponse>
    
    // ==================== COMENTARIOS ====================
    @GET("comments/post/{postId}")
    suspend fun getComments(
        @Path("postId") postId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<CommentsResponse>
    
    @POST("comments/post/{postId}")
    suspend fun createComment(
        @Path("postId") postId: Int,
        @Body request: CommentRequest
    ): Response<ApiResponse<Comment>>
    
    @PUT("comments/{id}")
    suspend fun updateComment(
        @Path("id") commentId: Int,
        @Body request: CommentRequest
    ): Response<ApiResponse<Comment>>
    
    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") commentId: Int): Response<ApiResponse<String>>
    
    @POST("comments/{id}/like")
    suspend fun toggleCommentLike(@Path("id") commentId: Int): Response<CommentLikeResponse>
}

// Respuestas adicionales para likes y favoritos
data class LikeResponse(
    @SerializedName("is_liked")
    val isLiked: Boolean,
    
    @SerializedName("likes_count")
    val likesCount: Int
)

data class FavoriteResponse(
    @SerializedName("is_favorited")
    val isFavorited: Boolean
)