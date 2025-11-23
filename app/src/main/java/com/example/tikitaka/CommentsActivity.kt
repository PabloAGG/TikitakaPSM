package com.example.tikitaka

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.models.Comment
import com.example.tikitaka.models.CommentRequest
import com.example.tikitaka.models.Post
import com.example.tikitaka.utils.NetworkUtils
import com.example.tikitaka.utils.PreferencesManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CommentsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var postCard: View
    private lateinit var postUserImage: ImageView
    private lateinit var postUserName: TextView
    private lateinit var postContent: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: View
    private lateinit var userAvatar: ImageView
    private lateinit var commentInput: TextInputEditText
    private lateinit var btnSendComment: ImageButton
    private lateinit var progressBar: ProgressBar

    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var preferencesManager: PreferencesManager
    private var postId: Int = -1
    private var currentPage = 1
    private var isLoading = false
    private var hasMore = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        preferencesManager = PreferencesManager(this)
        
        postId = intent.getIntExtra("POST_ID", -1)
        if (postId == -1) {
            Toast.makeText(this, "Error al cargar comentarios", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadPostInfo()
        loadComments()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        postCard = findViewById(R.id.post_card)
        postUserImage = findViewById(R.id.post_user_image)
        postUserName = findViewById(R.id.post_user_name)
        postContent = findViewById(R.id.post_content)
        recyclerView = findViewById(R.id.comments_recycler_view)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        emptyState = findViewById(R.id.empty_state)
        userAvatar = findViewById(R.id.user_avatar)
        commentInput = findViewById(R.id.comment_input)
        btnSendComment = findViewById(R.id.btn_send_comment)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        commentsAdapter = CommentsAdapter(
            currentUserId = preferencesManager.getUserId(),
            onLikeClick = { comment, position ->
                toggleCommentLike(comment, position)
            },
            onDeleteClick = { comment, position ->
                deleteComment(comment, position)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CommentsActivity)
            adapter = commentsAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    val totalItems = layoutManager.itemCount

                    if (!isLoading && hasMore && lastVisible >= totalItems - 2) {
                        loadMoreComments()
                    }
                }
            })
        }

        // Cargar avatar del usuario actual
        val profileImage = preferencesManager.getUserProfileImage()
        if (!profileImage.isNullOrEmpty()) {
            Glide.with(this)
                .load("${ApiClient.BASE_URL}${profileImage}")
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(userAvatar)
        }
    }

    private fun setupListeners() {
        swipeRefresh.setOnRefreshListener {
            currentPage = 1
            hasMore = true
            loadComments()
        }

        btnSendComment.setOnClickListener {
            val content = commentInput.text?.toString()?.trim()
            if (content.isNullOrEmpty()) {
                Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createComment(content)
        }
    }

    private fun loadPostInfo() {
        // Cargar información del post desde el intent
        val postUserName = intent.getStringExtra("POST_USER_NAME")
        val postContentText = intent.getStringExtra("POST_CONTENT")
        val postUserImageUrl = intent.getStringExtra("POST_USER_IMAGE")

        this.postUserName.text = postUserName ?: "Usuario"
        this.postContent.text = postContentText ?: ""

        if (!postUserImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load("${ApiClient.BASE_URL}${postUserImageUrl}")
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(postUserImage)
        }
    }

    private fun loadComments() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            swipeRefresh.isRefreshing = false
            Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        if (currentPage == 1) {
            progressBar.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getComments(postId, currentPage, 20)
                
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
                isLoading = false

                if (response.isSuccessful) {
                    val commentsResponse = response.body()
                    if (commentsResponse != null && commentsResponse.success) {
                        val comments = commentsResponse.comments

                        if (currentPage == 1) {
                            commentsAdapter.updateComments(comments)
                        } else {
                            // Agregar más comentarios
                            comments.forEach { commentsAdapter.addComment(it) }
                        }

                        // Verificar si hay más
                        hasMore = commentsResponse.pagination?.hasMore ?: false

                        // Mostrar/ocultar estado vacío
                        emptyState.visibility = if (comments.isEmpty() && currentPage == 1) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    } else {
                        Toast.makeText(
                            this@CommentsActivity,
                            commentsResponse?.message ?: "Error al cargar comentarios",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@CommentsActivity,
                        "Error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
                isLoading = false
                Toast.makeText(
                    this@CommentsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadMoreComments() {
        if (isLoading || !hasMore) return
        currentPage++
        loadComments()
    }

    private fun createComment(content: String) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }

        btnSendComment.isEnabled = false

        lifecycleScope.launch {
            try {
                val request = CommentRequest(content)
                val response = ApiClient.apiService.createComment(postId, request)

                btnSendComment.isEnabled = true

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                        commentInput.text?.clear()
                        commentsAdapter.addComment(apiResponse.data)
                        recyclerView.smoothScrollToPosition(0)
                        emptyState.visibility = View.GONE
                        Toast.makeText(this@CommentsActivity, "Comentario agregado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this@CommentsActivity,
                            apiResponse?.message ?: "Error al crear comentario",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@CommentsActivity,
                        "Error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                btnSendComment.isEnabled = true
                Toast.makeText(
                    this@CommentsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun toggleCommentLike(comment: Comment, position: Int) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.toggleCommentLike(comment.id)

                if (response.isSuccessful) {
                    val likeResponse = response.body()
                    if (likeResponse != null && likeResponse.success) {
                        val updatedComment = comment.copy(
                            isLiked = likeResponse.isLiked,
                            likesCount = likeResponse.likesCount
                        )
                        commentsAdapter.updateComment(position, updatedComment)
                    }
                } else {
                    Toast.makeText(
                        this@CommentsActivity,
                        "Error al dar like",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CommentsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteComment(comment: Comment, position: Int) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_SHORT).show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar comentario")
            .setMessage("¿Estás seguro de que quieres eliminar este comentario?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response = ApiClient.apiService.deleteComment(comment.id)

                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse != null && apiResponse.success) {
                                commentsAdapter.removeComment(position)
                                Toast.makeText(
                                    this@CommentsActivity,
                                    "Comentario eliminado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@CommentsActivity,
                                    apiResponse?.message ?: "Error al eliminar",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@CommentsActivity,
                                "Error: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@CommentsActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
