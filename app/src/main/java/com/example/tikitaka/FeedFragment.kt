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
import com.example.tikitaka.repository.PostRepository
import com.example.tikitaka.utils.NetworkUtils
import com.example.tikitaka.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FeedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabCreatePost: FloatingActionButton
    private lateinit var searchButton: ImageButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var postRepository: PostRepository
    
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
        
        try {
            postRepository = PostRepository.getInstance(requireContext())
            
            initViews(view)
            setupRecyclerView()
            setupSwipeRefresh()
            setupFab()
            setupSearchButton()
            
            loadPosts(refresh = true)
            showOfflineIndicatorIfNeeded()
        } catch (e: Exception) {
            android.util.Log.e("FeedFragment", "Error en onViewCreated", e)
            context?.let { 
                Utils.showToast(it, "Error al inicializar: ${e.message}", true)
            }
        }
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
            onCommentClick = { post -> openComments(post) },
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
                // Usar el repositorio con estrategia cache-first
                val result = postRepository.getPosts(
                    page = currentPage,
                    limit = 10,
                    forceRefresh = refresh
                )
                
                result.onSuccess { newPosts ->
                    if (refresh) {
                        postsAdapter.updatePosts(newPosts)
                        recyclerView.scrollToPosition(0)
                    } else {
                        postsAdapter.addPosts(newPosts)
                    }
                    
                    // Si tenemos menos de 10 posts, probablemente no hay más
                    hasMorePosts = newPosts.size >= 10
                    if (hasMorePosts) {
                        currentPage++
                    }
                    
                    // Mostrar estado vacío si no hay posts
                    if (refresh && newPosts.isEmpty()) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                    }
                    
                    // Mostrar indicador si estamos offline
                    showOfflineIndicatorIfNeeded()
                    
                }.onFailure { exception ->
                    android.util.Log.e("FeedFragment", "Error cargando posts", exception)
                    context?.let { 
                        if (!NetworkUtils.isNetworkAvailable(it)) {
                            Utils.showToast(it, "Sin conexión - mostrando caché", true)
                        } else {
                            Utils.showToast(it, "Error: ${exception.message}", true)
                        }
                    }
                    if (refresh && postsAdapter.itemCount == 0) {
                        showEmptyState()
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("FeedFragment", "Excepción cargando posts", e)
                context?.let { 
                    Utils.showToast(it, "Error: ${e.message}", true) 
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
                // Usar repositorio con actualización optimista
                val result = postRepository.toggleLike(post.id, post.isLiked)
                
                result.onSuccess { newLikeState ->
                    val updatedPost = post.copy(
                        isLiked = newLikeState,
                        likesCount = if (newLikeState) post.likesCount + 1 else post.likesCount - 1
                    )
                    postsAdapter.updatePost(position, updatedPost)
                    
                    // Mostrar mensaje si estamos offline
                    if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                        Utils.showToast(requireContext(), "Like guardado localmente")
                    }
                }.onFailure { exception ->
                    android.util.Log.e("FeedFragment", "Error en like", exception)
                    Utils.showToast(requireContext(), "Error: ${exception.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("FeedFragment", "Excepción en handleLikeClick", e)
                Utils.showToast(requireContext(), "Error al dar like")
            }
        }
    }

    private fun handleFavoriteClick(post: Post, position: Int) {
        lifecycleScope.launch {
            try {
                // Usar repositorio con actualización optimista
                val result = postRepository.toggleFavorite(post.id, post.isFavorited)
                
                result.onSuccess { newFavoriteState ->
                    val updatedPost = post.copy(
                        isFavorited = newFavoriteState
                    )
                    postsAdapter.updatePost(position, updatedPost)
                    
                    val message = if (newFavoriteState) "Post guardado" else "Post removido de favoritos"
                    Utils.showToast(requireContext(), message)
                    
                    // Mostrar mensaje si estamos offline
                    if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                        Utils.showToast(requireContext(), "Cambio guardado localmente")
                    }
                }.onFailure { exception ->
                    android.util.Log.e("FeedFragment", "Error en favorito", exception)
                    Utils.showToast(requireContext(), "Error: ${exception.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("FeedFragment", "Excepción en handleFavoriteClick", e)
                Utils.showToast(requireContext(), "Error al guardar favorito")
            }
        }
    }

    private fun navigateToUserProfile(userId: Int) {
        // Navegar al fragmento de perfil con el userId
        val bundle = Bundle().apply {
            putInt("user_id", userId)
        }
        
        try {
            findNavController().navigate(R.id.nav_profile, bundle)
        } catch (e: Exception) {
            // Si falla la navegación, mostrar mensaje
            context?.let { 
                Utils.showToast(it, "Abriendo perfil...") 
            }
        }
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

    private fun showEmptyState() {
        emptyStateText.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateText.text = "No hay posts aún.\n¡Sé el primero en publicar algo!"
    }

    private fun hideEmptyState() {
        emptyStateText.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun showOfflineIndicatorIfNeeded() {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            view?.let { rootView ->
                Snackbar.make(rootView, "📱 Modo offline - mostrando contenido guardado", Snackbar.LENGTH_LONG)
                    .setAction("OK") { }
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar posts cuando el usuario regrese (por ejemplo, después de crear un post)
        loadPosts(refresh = true)
    }
}