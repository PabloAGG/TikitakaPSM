package com.example.tikitaka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
            Post(1, "Usuario1", "18/10/2025", getString(R.string.lorem_ipsum_content), isFavorite = true),
            Post(4, "Usuario4", "15/10/2025", "Esta es una publicación favorita.", isFavorite = true)
        )
        
        val adapter = PostsAdapter(mockFavorites)
        recyclerView.adapter = adapter
    }
}