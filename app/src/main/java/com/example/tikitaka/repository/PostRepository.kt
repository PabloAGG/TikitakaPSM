package com.example.tikitaka.repository

import android.content.Context
import android.util.Log
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.database.AppDatabase
import com.example.tikitaka.database.CachedPostEntity
import com.example.tikitaka.models.Post
import com.example.tikitaka.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PostRepository private constructor(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val postDao = database.postDao()
    private val appContext = context.applicationContext
    
    companion object {
        @Volatile
        private var INSTANCE: PostRepository? = null
        
        // Caché válido por 30 minutos
        private val CACHE_VALIDITY_DURATION = TimeUnit.MINUTES.toMillis(30)
        
        fun getInstance(context: Context): PostRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = PostRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * Estrategia cache-first: Intenta desde el caché, luego desde red
     */
    suspend fun getPosts(page: Int, limit: Int = 10, forceRefresh: Boolean = false): Result<List<Post>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("PostRepository", "getPosts llamado: page=$page, limit=$limit, forceRefresh=$forceRefresh")
                val offset = (page - 1) * limit
                
                // Si hay red y es refresh forzado, ir directo a red
                if (forceRefresh && NetworkUtils.isNetworkAvailable(appContext)) {
                    Log.d("PostRepository", "Refresh forzado con red disponible, obteniendo desde red")
                    return@withContext fetchFromNetworkAndCache(page, limit)
                }
                
                // Intentar cargar desde caché primero
                val cachedPosts = postDao.getCachedPosts(limit, offset)
                
                if (cachedPosts.isNotEmpty() && !forceRefresh) {
                    Log.d("PostRepository", "Devolviendo ${cachedPosts.size} posts desde caché")
                    return@withContext Result.success(cachedPosts.map { it.toPost() })
                }
                
                // Si no hay caché o es muy antiguo, intentar desde red
                if (NetworkUtils.isNetworkAvailable(appContext)) {
                    Log.d("PostRepository", "Caché vacío o antiguo, obteniendo desde red")
                    return@withContext fetchFromNetworkAndCache(page, limit)
                } else {
                    Log.d("PostRepository", "Sin conexión a red")
                    // Sin red y sin caché
                    if (cachedPosts.isEmpty()) {
                        return@withContext Result.failure(Exception("No hay conexión y no hay posts en caché"))
                    } else {
                        // Devolver caché antiguo aunque no sea ideal
                        return@withContext Result.success(cachedPosts.map { it.toPost() })
                    }
                }
                
            } catch (e: Exception) {
                Log.e("PostRepository", "Error obteniendo posts", e)
                Result.failure(e)
            }
        }
    }
    
    private suspend fun fetchFromNetworkAndCache(page: Int, limit: Int): Result<List<Post>> {
        return try {
            val response = ApiClient.apiService.getPosts(page, limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val posts = response.body()!!.posts
                // Guardar en caché
                val entities = posts.map { CachedPostEntity.fromPost(it) }
                
                // Si es página 1, limpiar caché antiguo
                if (page == 1) {
                    postDao.clearAllCache()
                }
                
                postDao.insertPosts(entities)
                Log.d("PostRepository", "Guardados ${entities.size} posts en caché desde red")
                Result.success(posts)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Error desde red", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar el estado de like localmente y sincronizar con servidor
     */
    suspend fun toggleLike(postId: Int, currentlyLiked: Boolean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Actualizar optimistamente en caché
                val cachedPost = postDao.getPostById(postId)
                if (cachedPost != null) {
                    val newLiked = !currentlyLiked
                    val newCount = if (newLiked) cachedPost.likesCount + 1 else cachedPost.likesCount - 1
                    postDao.updateLikeStatus(postId, newLiked, newCount)
                }
                
                // Intentar sincronizar con servidor
                if (NetworkUtils.isNetworkAvailable(appContext)) {
                    val response = ApiClient.apiService.toggleLike(postId)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        Result.success(!currentlyLiked)
                    } else {
                        // Revertir cambio optimista si falla
                        cachedPost?.let {
                            postDao.updateLikeStatus(postId, currentlyLiked, it.likesCount)
                        }
                        Result.failure(Exception(response.body()?.message ?: "Error"))
                    }
                } else {
                    // Sin conexión, mantener cambio local
                    Result.success(!currentlyLiked)
                }
            } catch (e: Exception) {
                Log.e("PostRepository", "Error toggling like", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Actualizar el estado de favorito localmente y sincronizar con servidor
     */
    suspend fun toggleFavorite(postId: Int, currentlyFavorited: Boolean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Actualizar optimistamente en caché
                val cachedPost = postDao.getPostById(postId)
                if (cachedPost != null) {
                    val newFavorited = !currentlyFavorited
                    postDao.updateFavoriteStatus(postId, newFavorited)
                }
                
                // Intentar sincronizar con servidor
                if (NetworkUtils.isNetworkAvailable(appContext)) {
                    val response = ApiClient.apiService.toggleFavorite(postId)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        Result.success(!currentlyFavorited)
                    } else {
                        // Revertir cambio optimista si falla
                        cachedPost?.let {
                            postDao.updateFavoriteStatus(postId, currentlyFavorited)
                        }
                        Result.failure(Exception(response.body()?.message ?: "Error"))
                    }
                } else {
                    // Sin conexión, mantener cambio local
                    Result.success(!currentlyFavorited)
                }
            } catch (e: Exception) {
                Log.e("PostRepository", "Error toggling favorite", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Limpiar caché antiguo (llamar periódicamente)
     */
    suspend fun clearOldCache() {
        withContext(Dispatchers.IO) {
            val threshold = System.currentTimeMillis() - CACHE_VALIDITY_DURATION
            postDao.deleteOldCache(threshold)
        }
    }
    
    /**
     * Obtener posts como Flow para observar cambios en tiempo real
     */
    fun getPostsFlow(): Flow<List<Post>> {
        return postDao.getCachedPostsFlow().map { entities ->
            entities.map { it.toPost() }
        }
    }
}