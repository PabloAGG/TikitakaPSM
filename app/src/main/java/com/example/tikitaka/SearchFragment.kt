package com.example.tikitaka

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.models.Post
import com.example.tikitaka.repository.PostRepository
import com.example.tikitaka.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var orderSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var postRepository: PostRepository
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            postRepository = PostRepository.getInstance(requireContext())
            
            searchEditText = view.findViewById(R.id.search_edit_text)
            orderSpinner = view.findViewById(R.id.order_spinner)
            recyclerView = view.findViewById(R.id.recycler_view_search_results)
            progressBar = view.findViewById(R.id.progress_bar)
            emptyStateText = view.findViewById(R.id.empty_state_text)
            
            setupViews()
            setupRecyclerView()
        } catch (e: Exception) {
            android.util.Log.e("SearchFragment", "Error en onViewCreated", e)
            context?.let { 
                Utils.showToast(it, "Error al inicializar búsqueda: ${e.message}", true)
            }
        }
    }

    private fun setupViews() {
        // Búsqueda con debounce (esperar 500ms después de que el usuario deje de escribir)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(500) // Debounce
                    val query = s?.toString()?.trim()
                    if (!query.isNullOrEmpty()) {
                        searchPosts(query)
                    } else {
                        showEmptyState("Escribe algo para buscar")
                        postsAdapter.updatePosts(emptyList())
                    }
                }
            }
        })
        
        showEmptyState("Busca posts por contenido o usuario")
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
    
    private fun searchPosts(query: String) {
        progressBar.visibility = View.VISIBLE
        emptyStateText.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                // Buscar en caché local primero
                val result = postRepository.getPosts(page = 1, limit = 50)
                
                result.onSuccess { posts ->
                    // Filtrar posts que contengan el query en el contenido o username
                    val filteredPosts = posts.filter { post ->
                        post.content.contains(query, ignoreCase = true) ||
                        post.username.contains(query, ignoreCase = true) ||
                        post.fullName.contains(query, ignoreCase = true)
                    }
                    
                    if (filteredPosts.isEmpty()) {
                        showEmptyState("No se encontraron resultados para \"$query\"")
                    } else {
                        hideEmptyState()
                        postsAdapter.updatePosts(filteredPosts)
                    }
                }.onFailure {
                    showEmptyState("Error al buscar. Intenta de nuevo.")
                }
            } catch (e: Exception) {
                context?.let { 
                    Utils.showToast(it, "Error de búsqueda: ${e.message}", true)
                }
                showEmptyState("Error al buscar")
            } finally {
                progressBar.visibility = View.GONE
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
                    android.util.Log.e("SearchFragment", "Error en like", exception)
                    context?.let { Utils.showToast(it, "Error al dar like") }
                }
            } catch (e: Exception) {
                android.util.Log.e("SearchFragment", "Excepción en handleLikeClick", e)
                context?.let { Utils.showToast(it, "Error al dar like") }
            }
        }
    }
    
    private fun handleFavoriteClick(post: Post, position: Int) {
        lifecycleScope.launch {
            try {
                val result = postRepository.toggleFavorite(post.id, post.isFavorited)
                result.onSuccess { newFavoriteState ->
                    val updatedPost = post.copy(isFavorited = newFavoriteState)
                    postsAdapter.updatePost(position, updatedPost)
                }.onFailure { exception ->
                    android.util.Log.e("SearchFragment", "Error en favorito", exception)
                    context?.let { Utils.showToast(it, "Error al guardar favorito") }
                }
            } catch (e: Exception) {
                android.util.Log.e("SearchFragment", "Excepción en handleFavoriteClick", e)
                context?.let { Utils.showToast(it, "Error al guardar favorito") }
            }
        }
    }
    
    private fun showEmptyState(message: String) {
        emptyStateText.visibility = View.VISIBLE
        emptyStateText.text = message
        recyclerView.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        emptyStateText.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
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