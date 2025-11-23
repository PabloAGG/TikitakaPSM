package com.example.tikitaka

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.models.Post
import com.example.tikitaka.utils.PreferencesManager
import com.example.tikitaka.utils.Utils
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView
    private lateinit var editButton: ImageButton
    private lateinit var draftsOption: TextView
    private lateinit var modifyDataOption: TextView
    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var logoutButton: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var postsAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager(requireContext())
        
        initViews(view)
        setupViews()
        setupRecyclerView()
        loadUserProfile()
        loadUserPosts()
    }

    private fun initViews(view: View) {
        profileImage = view.findViewById(R.id.profile_image)
        usernameText = view.findViewById(R.id.username)
        emailText = view.findViewById(R.id.profile_email)
        phoneText = view.findViewById(R.id.profile_phone)
        editButton = view.findViewById(R.id.edit_profile_button)
        draftsOption = view.findViewById(R.id.drafts_option)
        modifyDataOption = view.findViewById(R.id.modify_data_option)
        recyclerViewPosts = view.findViewById(R.id.recycler_view_user_posts)
        logoutButton = view.findViewById(R.id.logout_button)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupViews() {
        editButton.setOnClickListener {
            val intent = Intent(context, EditProfileActivity::class.java)
            startActivity(intent)
        }
        
        modifyDataOption.setOnClickListener {
            val intent = Intent(context, EditProfileActivity::class.java)
            startActivity(intent)
        }

        draftsOption.setOnClickListener {
            val intent = Intent(context, DraftsActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun setupRecyclerView() {
        recyclerViewPosts.layoutManager = LinearLayoutManager(context)
        
        postsAdapter = PostsAdapter(
            posts = mutableListOf(),
            onLikeClick = { _, _ -> /* no-op for own posts */ },
            onFavoriteClick = { _, _ -> /* no-op for own posts */ },
            onCommentClick = { post -> openComments(post) },
            onUserClick = { _ -> /* no-op for own posts */ }
        )
        
        recyclerViewPosts.adapter = postsAdapter
    }

    private fun loadUserProfile() {
        // Cargar datos del usuario desde PreferencesManager
        val fullName = preferencesManager.getUserFullName() ?: ""
        val username = preferencesManager.getUsername() ?: ""
        val email = preferencesManager.getUserEmail()
        
        usernameText.text = if (fullName.isNotEmpty()) fullName else if (username.isNotEmpty()) username else "Usuario"
        emailText.text = email ?: "correo@ejemplo.com"
        phoneText.text = "No especificado" // Phone no está guardado en PreferencesManager
    }

    private fun loadUserPosts() {
        val userId = preferencesManager.getUserId()
        if (userId == -1) return
        
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getUserPosts(userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val posts = response.body()!!.posts
                    postsAdapter.updatePosts(posts)
                } else {
                    context?.let { 
                        Utils.showToast(it, "Error cargando publicaciones", true)
                    }
                }
            } catch (e: Exception) {
                context?.let { 
                    Utils.showToast(it, "Error de conexión", true)
                }
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun performLogout() {
        // Limpiar sesión del usuario
        preferencesManager.clearUserData()
        
        // Redirigir a LoginActivity
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        
        context?.let { 
            Utils.showToast(it, "Sesión cerrada")
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadUserProfile()
        loadUserPosts()
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
