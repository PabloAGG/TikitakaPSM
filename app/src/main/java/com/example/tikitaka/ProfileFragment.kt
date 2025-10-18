package com.example.tikitaka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var editButton: ImageButton
    private lateinit var recyclerViewPosts: RecyclerView

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
        editButton = view.findViewById(R.id.edit_profile_button)
        recyclerViewPosts = view.findViewById(R.id.recycler_view_user_posts)
        
        setupViews()
        setupRecyclerView()
    }

    private fun setupViews() {
        usernameText.text = getString(R.string.username_placeholder)
        editButton.setOnClickListener {
            // TODO: Implement edit profile functionality
        }
    }

    private fun setupRecyclerView() {
        recyclerViewPosts.layoutManager = LinearLayoutManager(context)
        
        // Mock data for user posts
        val mockUserPosts = listOf(
            Post(1, getString(R.string.username_placeholder), "18/10/2025", "Mi primera publicación"),
            Post(2, getString(R.string.username_placeholder), "17/10/2025", "Compartiendo momentos especiales")
        )
        
        val adapter = PostsAdapter(mockUserPosts)
        recyclerViewPosts.adapter = adapter
    }
}