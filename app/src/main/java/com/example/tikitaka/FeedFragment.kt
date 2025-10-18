package com.example.tikitaka

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FeedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabCreatePost: FloatingActionButton
    private lateinit var searchButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_view_posts)
        fabCreatePost = view.findViewById(R.id.fab_create_post)
        searchButton = view.findViewById(R.id.btn_search)
        
        setupRecyclerView()
        setupFab()
        setupSearchButton()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Mock data for testing
        val mockPosts = listOf(
            Post(1, "Usuario1", "18/10/2025", getString(R.string.lorem_ipsum_content)),
            Post(2, "Usuario2", "17/10/2025", "Esta es otra publicación de ejemplo para probar la interfaz."),
            Post(3, "Usuario3", "16/10/2025", "¡Hola mundo! Esta es mi primera publicación en Tikitaka.")
        )
        
        val adapter = PostsAdapter(mockPosts)
        recyclerView.adapter = adapter
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
}