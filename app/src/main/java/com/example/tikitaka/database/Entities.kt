package com.example.tikitaka.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tikitaka.models.Post

@Entity(tableName = "cached_posts")
data class CachedPostEntity(
    @PrimaryKey
    val id: Int,
    val content: String,
    val imageUrl: String?,
    val teamId: Int,
    val userId: Int,
    val username: String,
    val fullName: String,
    val profileImage: String?,
    val teamName: String,
    val teamLogo: String?,
    val likesCount: Int,
    val favoritesCount: Int,
    val isLiked: Boolean,
    val isFavorited: Boolean,
    val isDraft: Boolean,
    val createdAt: String,
    val cachedAt: Long // Timestamp de cuándo se guardó en caché
) {
    fun toPost(): Post {
        return Post(
            id = id,
            content = content,
            imageUrl = imageUrl,
            teamId = teamId,
            userId = userId,
            username = username,
            fullName = fullName,
            profileImage = profileImage,
            teamName = teamName,
            teamLogo = teamLogo,
            likesCount = likesCount,
            favoritesCount = favoritesCount,
            isLiked = isLiked,
            isFavorited = isFavorited,
            isDraft = isDraft,
            createdAt = createdAt
        )
    }
    
    companion object {
        fun fromPost(post: Post): CachedPostEntity {
            return CachedPostEntity(
                id = post.id,
                content = post.content,
                imageUrl = post.imageUrl,
                teamId = post.teamId,
                userId = post.userId,
                username = post.username,
                fullName = post.fullName,
                profileImage = post.profileImage,
                teamName = post.teamName,
                teamLogo = post.teamLogo,
                likesCount = post.likesCount,
                favoritesCount = post.favoritesCount,
                isLiked = post.isLiked,
                isFavorited = post.isFavorited,
                isDraft = post.isDraft,
                createdAt = post.createdAt,
                cachedAt = System.currentTimeMillis()
            )
        }
    }
}

@Entity(tableName = "cached_teams")
data class CachedTeamEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val logoUrl: String?,
    val confederation: String,
    val fansCount: Int,
    val postsCount: Int,
    val cachedAt: Long
)

@Entity(tableName = "draft_posts")
data class DraftPostEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val serverId: Int?, // ID del servidor si ya fue sincronizado
    val content: String,
    val imageUri: String?, // URI local de la imagen
    val teamId: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = false
)