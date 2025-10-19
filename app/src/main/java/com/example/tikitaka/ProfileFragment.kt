package com.example.tikitaka

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tikitaka.models.Post

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        profileImage = view.findViewById(R.id.profile_image)
        usernameText = view.findViewById(R.id.username)
        emailText = view.findViewById(R.id.profile_email)
        phoneText = view.findViewById(R.id.profile_phone)
        editButton = view.findViewById(R.id.edit_profile_button)
        draftsOption = view.findViewById(R.id.drafts_option)
        modifyDataOption = view.findViewById(R.id.modify_data_option)
        recyclerViewPosts = view.findViewById(R.id.recycler_view_user_posts)
        logoutButton = view.findViewById(R.id.logout_button)
        
        setupViews()
        setupRecyclerView()
    }

    private fun setupViews() {
        usernameText.text = getString(R.string.username_placeholder)
        emailText.text = "correo@ejemplo.com"
        phoneText.text = "8199191919"
        
        editButton.setOnClickListener {
            val intent = Intent(context, EditProfileActivity::class.java)
            startActivity(intent)
        }

        draftsOption.setOnClickListener {
            val intent = Intent(context, DraftsActivity::class.java)
            startActivity(intent)
        }

        modifyDataOption.setOnClickListener {
            val intent = Intent(context, EditProfileActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun setupRecyclerView() {
        recyclerViewPosts.layoutManager = LinearLayoutManager(context)
        
        // Mock data for user posts (including drafts)
        val mockUserPosts = listOf(
            Post(
                id = 1,
                content = "Mi primera publicación",
                teamId = 1,
                userId = 1,
                username = getString(R.string.username_placeholder),
                fullName = "Usuario Completo",
                teamName = "Argentina",
                createdAt = "2025-10-18T00:00:00Z"
            ),
            Post(
                id = 2,
                content = "Compartiendo momentos especiales",
                teamId = 2,
                userId = 1,
                username = getString(R.string.username_placeholder),
                fullName = "Usuario Completo",
                teamName = "México",
                isDraft = true,
                createdAt = "2025-10-17T00:00:00Z"
            )
        )
        
        val adapter = PostsAdapter(mockUserPosts.toMutableList(), { post, position ->
            // Handle like click
        }, { post, position ->
            // Handle favorite click
        })
        recyclerViewPosts.adapter = adapter
    }

    private fun performLogout() {
        // TODO: Clear user session/preferences
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}