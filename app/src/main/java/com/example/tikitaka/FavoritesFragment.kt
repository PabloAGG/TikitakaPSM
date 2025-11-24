package com.example.tikitaka

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.models.Post
import com.example.tikitaka.repository.PostRepository
import com.example.tikitaka.utils.Utils
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var postRepository: PostRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            postRepository = PostRepository.getInstance(requireContext())
            
            recyclerView = view.findViewById(R.id.recycler_view_favorites)
            swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
            progressBar = view.findViewById(R.id.progress_bar)
            emptyStateText = view.findViewById(R.id.empty_state_text)
            
            setupRecyclerView()
            setupSwipeRefresh()
            loadFavorites()
        } catch (e: Exception) {
            android.util.Log.e("FavoritesFragment", "Error en onViewCreated", e)
            context?.let { 
                Utils.showToast(it, "Error al inicializar favoritos: ${e.message}", true)
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        postsAdapter = PostsAdapter(
            posts = mutableListOf(),
            onLikeClick = { post, position -> handleLikeClick(post, position) },
            onFavoriteClick = { post, position -> handleFavoriteClick(post, position) },
            onCommentClick = { post -> openComments(post) },
            onUserClick = { userId -> /* Navegar a perfil */ }
        )
        
        recyclerView.adapter = postsAdapter
    }
    
    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            loadFavorites()
        }
        
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light
        )
    }
    
    private fun loadFavorites() {
        if (!swipeRefreshLayout.isRefreshing) {
            progressBar.visibility = View.VISIBLE
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getFavorites(page = 1, limit = 50)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val favorites = response.body()!!.posts
                    
                    if (favorites.isEmpty()) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                        postsAdapter.updatePosts(favorites)
                    }
                } else {
                    context?.let { 
                        Utils.showToast(it, "Error cargando favoritos", true)
                    }
                    if (postsAdapter.itemCount == 0) {
                        showEmptyState()
                    }
                }
            } catch (e: Exception) {
                context?.let { 
                    Utils.showToast(it, "Error de conexión", true)
                }
                if (postsAdapter.itemCount == 0) {
                    showEmptyState()
                }
            } finally {
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun handleLikeClick(post: Post, position: Int) {
        lifecycleScope.launch {
            try {
                val result = postRepository.toggleLike(post.id, post.isLiked)
                result.onSuccess { newLikeState ->
                    val updatedPost = post.copy(
                        isLiked = newLikeState,
                        likesCount = if (newLikeState) post.likesCount + 1 else post.likesCount - 1
                    )
                    postsAdapter.updatePost(position, updatedPost)
                }.onFailure { exception ->
                    android.util.Log.e("FavoritesFragment", "Error en like", exception)
                    context?.let { Utils.showToast(it, "Error al dar like") }
                }
            } catch (e: Exception) {
                android.util.Log.e("FavoritesFragment", "Excepción en handleLikeClick", e)
                context?.let { Utils.showToast(it, "Error al dar like") }
            }
        }
    }
    
    private fun handleFavoriteClick(post: Post, position: Int) {
        lifecycleScope.launch {
            try {
                val result = postRepository.toggleFavorite(post.id, post.isFavorited)
                result.onSuccess { newFavoriteState ->
                    if (!newFavoriteState) {
                        // Si se quitó de favoritos, remover de la lista
                        postsAdapter.removePost(position)
                        
                        if (postsAdapter.itemCount == 0) {
                            showEmptyState()
                        }
                        
                        context?.let { 
                            Utils.showToast(it, "Post removido de favoritos")
                        }
                    }
                }.onFailure { exception ->
                    android.util.Log.e("FavoritesFragment", "Error en favorito", exception)
                    context?.let { Utils.showToast(it, "Error al remover favorito") }
                }
            } catch (e: Exception) {
                android.util.Log.e("FavoritesFragment", "Excepción en handleFavoriteClick", e)
                context?.let { Utils.showToast(it, "Error al remover favorito") }
            }
        }
    }
    
    private fun showEmptyState() {
        emptyStateText.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateText.text = "No tienes posts favoritos aún\n¡Guarda tus publicaciones favoritas!"
    }
    
    private fun hideEmptyState() {
        emptyStateText.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
    
    override fun onResume() {
        super.onResume()
        loadFavorites()
    }
    
    private fun openComments(post: Post) {
        val intent = Intent(requireContext(), CommentsActivity::class.java).apply {
            putExtra("POST_ID", post.id)
            putExtra("POST_USER_NAME", post.fullName.ifEmpty { post.username })
            putExtra("POST_CONTENT", post.content)
            putExtra("POST_USER_IMAGE", post.profileImage)
        }
        startActivity(intent)
    }
}