package com.example.tikitaka

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tikitaka.models.Post

class DraftsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drafts)
        
        initViews()
        setupListeners()
        setupRecyclerView()
    }

    private fun initViews() {
        backButton = findViewById(R.id.back_button)
        recyclerView = findViewById(R.id.recycler_view_drafts)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Mock draft posts
        val mockDrafts = listOf(
            Post(
                id = 1,
                content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud ...",
                teamId = 1,
                userId = 1,
                username = "Usuario",
                fullName = "Usuario Completo",
                teamName = "Argentina",
                isDraft = true,
                createdAt = "2026-11-11T00:00:00Z"
            ),
            Post(
                id = 2,
                content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud ...",
                teamId = 2,
                userId = 1,
                username = "Usuario",
                fullName = "Usuario Completo",
                teamName = "Brasil",
                isDraft = true,
                createdAt = "2026-11-11T00:00:00Z"
            )
        )
        
        val adapter = DraftsAdapter(mockDrafts.toMutableList()) { post ->
            // Handle edit draft
            val intent = Intent(this, CreatePostActivity::class.java)
            intent.putExtra("draft_id", post.id)
            startActivity(intent)
        }
        
        recyclerView.adapter = adapter
    }
}