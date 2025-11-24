package com.example.tikitaka.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM cached_posts ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getCachedPosts(limit: Int, offset: Int): List<CachedPostEntity>
    
    @Query("SELECT * FROM cached_posts ORDER BY createdAt DESC")
    fun getCachedPostsFlow(): Flow<List<CachedPostEntity>>
    
    @Query("SELECT * FROM cached_posts WHERE id = :postId")
    suspend fun getPostById(postId: Int): CachedPostEntity?
    
    @Query("SELECT * FROM cached_posts WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getPostsByUser(userId: Int): List<CachedPostEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CachedPostEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CachedPostEntity)
    
    @Update
    suspend fun updatePost(post: CachedPostEntity)
    
    @Query("UPDATE cached_posts SET isLiked = :isLiked, likesCount = :likesCount WHERE id = :postId")
    suspend fun updateLikeStatus(postId: Int, isLiked: Boolean, likesCount: Int)
    
    @Query("UPDATE cached_posts SET isFavorited = :isFavorited WHERE id = :postId")
    suspend fun updateFavoriteStatus(postId: Int, isFavorited: Boolean)
    
    @Query("DELETE FROM cached_posts WHERE cachedAt < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)
    
    @Query("DELETE FROM cached_posts")
    suspend fun clearAllCache()
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM cached_teams ORDER BY name ASC")
    suspend fun getAllTeams(): List<CachedTeamEntity>
    
    @Query("SELECT * FROM cached_teams WHERE id = :teamId")
    suspend fun getTeamById(teamId: Int): CachedTeamEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<CachedTeamEntity>)
    
    @Query("DELETE FROM cached_teams")
    suspend fun clearAllTeams()
}

@Dao
interface DraftDao {
    @Query("SELECT * FROM draft_posts ORDER BY updatedAt DESC")
    suspend fun getAllDrafts(): List<DraftPostEntity>
    
    @Query("SELECT * FROM draft_posts WHERE localId = :localId")
    suspend fun getDraftById(localId: Int): DraftPostEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: DraftPostEntity): Long
    
    @Update
    suspend fun updateDraft(draft: DraftPostEntity)
    
    @Query("UPDATE draft_posts SET isSynced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markAsSynced(localId: Int, serverId: Int)
    
    @Query("DELETE FROM draft_posts WHERE localId = :localId")
    suspend fun deleteDraft(localId: Int)
    
    @Query("DELETE FROM draft_posts")
    suspend fun clearAllDrafts()
}