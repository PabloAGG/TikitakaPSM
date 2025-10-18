package com.example.tikitaka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostsAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.user_name)
        val postDate: TextView = view.findViewById(R.id.post_date)
        val postContent: TextView = view.findViewById(R.id.post_content)
        val likeButton: Button = view.findViewById(R.id.btn_like)
        val favoriteButton: Button = view.findViewById(R.id.btn_favorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.userName.text = post.username
        holder.postDate.text = post.date
        holder.postContent.text = post.content
        
        holder.likeButton.setOnClickListener {
            // TODO: Handle like action
        }
        
        holder.favoriteButton.setOnClickListener {
            // TODO: Handle favorite action
        }
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}