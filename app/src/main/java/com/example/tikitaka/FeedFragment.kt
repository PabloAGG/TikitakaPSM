package com.example.tikitaka

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.models.Post
import com.example.tikitaka.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class FeedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabCreatePost: FloatingActionButton
    private lateinit var searchButton: ImageButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    
    private lateinit var postsAdapter: PostsAdapter
    private var currentPage = 1
    private var isLoading = false
    private var hasMorePosts = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
        setupSearchButton()
        
        loadPosts(refresh = true)
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_posts)
        fabCreatePost = view.findViewById(R.id.fab_create_post)
        searchButton = view.findViewById(R.id.btn_search)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyStateText = view.findViewById(R.id.empty_state_text)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        postsAdapter = PostsAdapter(
            posts = mutableListOf(),
            onLikeClick = { post, position -> handleLikeClick(post, position) },
            onFavoriteClick = { post, position -> handleFavoriteClick(post, position) },
            onUserClick = { userId -> navigateToUserProfile(userId) }
        )
        
        recyclerView.adapter = postsAdapter
        
        // Scroll infinito
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                
                if (!isLoading && hasMorePosts && lastVisibleItem >= totalItemCount - 5) {
                    loadPosts(refresh = false)
                }
            }
        })
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            loadPosts(refresh = true)
        }
        
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light
        )
    }

    private fun setupFab() {
        fabCreatePost.setOnClickListener {
            val intent = Intent(context, CreatePostActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearchButton() {
        searchButton.setOnClickListener {
            findNavController().navigate(R.id.nav_search)
        }
    }

    private fun loadPosts(refresh: Boolean) {
        if (isLoading) return
        
        isLoading = true
        
        if (refresh) {
            currentPage = 1
            hasMorePosts = true
            if (!swipeRefreshLayout.isRefreshing) {
                progressBar.visibility = View.VISIBLE
            }
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getPosts(
                    page = currentPage,
                    limit = 10
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val postsResponse = response.body()!!
                    val newPosts = postsResponse.posts
                    
                    if (refresh) {
                        postsAdapter.updatePosts(newPosts)
                        recyclerView.scrollToPosition(0)
                    } else {
                        postsAdapter.addPosts(newPosts)
                    }
                    
                    // Verificar si hay más posts
                    hasMorePosts = postsResponse.pagination?.hasMore ?: false
                    if (hasMorePosts) {
                        currentPage++
                    }
                    
                    // Mostrar estado vacío si no hay posts
                    if (refresh && newPosts.isEmpty()) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                    }
                    
                } else {
                    context?.let { 
                        Utils.showToast(it, "Error cargando posts", true) 
                    }
                    if (refresh && postsAdapter.itemCount == 0) {
                        showEmptyState()
                    }
                }
                
            } catch (e: Exception) {
                context?.let { 
                    Utils.showToast(it, "Error de conexión", true) 
                }
                if (refresh && postsAdapter.itemCount == 0) {
                    showEmptyState()
                }
            } finally {
                isLoading = false
                swipeRefreshLayout.isRefreshing = false
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun handleLikeClick(post: Post, position: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.toggleLike(post.id)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val likeResponse = response.body()!!.data
                    
                    // Crear post actualizado
                    val updatedPost = post.copy(
                        isLiked = likeResponse?.isLiked ?: !post.isLiked,
                        likesCount = likeResponse?.likesCount ?: post.likesCount
                    )
                    
                    postsAdapter.updatePost(position, updatedPost)
                } else {
                    context?.let { 
                        Utils.showToast(it, "Error al procesar like") 
                    }
                }
            } catch (e: Exception) {
                context?.let { 
                    Utils.showToast(it, "Error de conexión") 
                }
            }
        }
    }

    private fun handleFavoriteClick(post: Post, position: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.toggleFavorite(post.id)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val favoriteResponse = response.body()!!.data
                    
                    // Crear post actualizado
                    val updatedPost = post.copy(
                        isFavorited = favoriteResponse?.isFavorited ?: !post.isFavorited
                    )
                    
                    postsAdapter.updatePost(position, updatedPost)
                    
                    context?.let { 
                        val message = if (updatedPost.isFavorited) "Post guardado" else "Post removido de favoritos"
                        Utils.showToast(it, message)
                    }
                } else {
                    context?.let { 
                        Utils.showToast(it, "Error al procesar favorito") 
                    }
                }
            } catch (e: Exception) {
                context?.let { 
                    Utils.showToast(it, "Error de conexión") 
                }
            }
        }
    }

    private fun navigateToUserProfile(userId: Int) {
        // TODO: Implementar navegación al perfil del usuario
        context?.let { 
            Utils.showToast(it, "Ver perfil de usuario #$userId") 
        }
    }

    private fun showEmptyState() {
        emptyStateText.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateText.text = "No hay posts aún.\n¡Sé el primero en publicar algo!"
    }

    private fun hideEmptyState() {
        emptyStateText.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Recargar posts cuando el usuario regrese (por ejemplo, después de crear un post)
        loadPosts(refresh = true)
    }
}