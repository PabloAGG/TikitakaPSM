package com.example.tikitaka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var orderSpinner: Spinner
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        searchEditText = view.findViewById(R.id.search_edit_text)
        orderSpinner = view.findViewById(R.id.order_spinner)
        recyclerView = view.findViewById(R.id.recycler_view_search_results)
        
        setupViews()
        setupRecyclerView()
    }

    private fun setupViews() {
        // TODO: Set up search functionality and spinner
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Mock data for search results (initially empty)
        val mockSearchResults = listOf<Post>()
        
        val adapter = PostsAdapter(mockSearchResults)
        recyclerView.adapter = adapter
    }
}