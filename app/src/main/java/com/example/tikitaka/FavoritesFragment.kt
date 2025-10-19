package com.example.tikitaka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tikitaka.models.Post

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_view_favorites)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Mock data for favorites
        val mockFavorites = listOf(
            Post(
                id = 1,
                content = getString(R.string.lorem_ipsum_content),
                teamId = 1,
                userId = 1,
                username = "Usuario1",
                fullName = "Usuario Uno",
                teamName = "Argentina",
                isFavorited = true,
                createdAt = "2025-10-18T00:00:00Z"
            ),
            Post(
                id = 4,
                content = "Esta es una publicación favorita.",
                teamId = 2,
                userId = 4,
                username = "Usuario4",
                fullName = "Usuario Cuatro",
                teamName = "Brasil",
                isFavorited = true,
                createdAt = "2025-10-15T00:00:00Z"
            )
        )
        
        val adapter = PostsAdapter(mockFavorites.toMutableList(), { post, position ->
            // Handle like click
        }, { post, position ->
            // Handle favorite click
        })
        recyclerView.adapter = adapter
    }
}