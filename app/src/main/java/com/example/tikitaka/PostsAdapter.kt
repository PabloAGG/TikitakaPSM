package com.example.tikitaka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tikitaka.models.Post
import com.example.tikitaka.utils.Utils

class PostsAdapter(
    private var posts: MutableList<Post>,
    private val onLikeClick: (Post, Int) -> Unit,
    private val onFavoriteClick: (Post, Int) -> Unit,
    private val onUserClick: (Int) -> Unit = {}
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userProfileImage: ImageView = view.findViewById(R.id.user_profile_image)
        val userName: TextView = view.findViewById(R.id.user_name)
        val postDate: TextView = view.findViewById(R.id.post_date)
        val teamName: TextView = view.findViewById(R.id.team_name)
        val teamLogo: ImageView = view.findViewById(R.id.team_logo)
        val postContent: TextView = view.findViewById(R.id.post_content)
        val postImage: ImageView = view.findViewById(R.id.post_image)
        val likeButton: Button = view.findViewById(R.id.btn_like)
        val favoriteButton: Button = view.findViewById(R.id.btn_favorite)
        val likesCount: TextView = view.findViewById(R.id.likes_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        
        // Información del usuario
        holder.userName.text = post.fullName.ifEmpty { post.username }
        holder.postDate.text = Utils.formatRelativeDate(post.createdAt)
        
        // Imagen de perfil del usuario
        if (!post.profileImage.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(Utils.getFullImageUrl(post.profileImage))
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(holder.userProfileImage)
        } else {
            holder.userProfileImage.setImageResource(R.drawable.ic_person)
        }
        
        // Información del equipo
        holder.teamName.text = post.teamName
        if (!post.teamLogo.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(Utils.getFullImageUrl(post.teamLogo))
                .placeholder(R.drawable.ic_home)
                .error(R.drawable.ic_home)
                .into(holder.teamLogo)
        } else {
            holder.teamLogo.setImageResource(R.drawable.ic_home)
        }
        
        // Contenido del post
        holder.postContent.text = post.content
        
        // Imagen del post (si existe)
        if (!post.imageUrl.isNullOrEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(Utils.getFullImageUrl(post.imageUrl))
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }
        
        // Botones de interacción
        updateLikeButton(holder, post)
        updateFavoriteButton(holder, post)
        
        // Contador de likes
        holder.likesCount.text = Utils.formatNumber(post.likesCount)
        
        // Click listeners
        holder.likeButton.setOnClickListener {
            onLikeClick(post, position)
        }
        
        holder.favoriteButton.setOnClickListener {
            onFavoriteClick(post, position)
        }
        
        // Click en el usuario para ver su perfil
        val userClickListener = View.OnClickListener {
            onUserClick(post.userId)
        }
        holder.userProfileImage.setOnClickListener(userClickListener)
        holder.userName.setOnClickListener(userClickListener)
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }
    
    fun addPosts(newPosts: List<Post>) {
        val startPosition = posts.size
        posts.addAll(newPosts)
        notifyItemRangeInserted(startPosition, newPosts.size)
    }
    
    fun updatePost(position: Int, updatedPost: Post) {
        if (position in 0 until posts.size) {
            posts[position] = updatedPost
            notifyItemChanged(position)
        }
    }
    
    fun removePost(position: Int) {
        if (position in 0 until posts.size) {
            posts.removeAt(position)
            notifyItemRemoved(position)
        }
    }
    
    private fun updateLikeButton(holder: PostViewHolder, post: Post) {
        if (post.isLiked) {
            holder.likeButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_thumb_up, 0, 0, 0
            )
            holder.likeButton.text = "Liked"
        } else {
            holder.likeButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_thumb_up, 0, 0, 0
            )
            holder.likeButton.text = "Like"
        }
    }
    
    private fun updateFavoriteButton(holder: PostViewHolder, post: Post) {
        if (post.isFavorited) {
            holder.favoriteButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_favorite_border, 0, 0, 0
            )
            holder.favoriteButton.text = "Saved"
        } else {
            holder.favoriteButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_favorite_border, 0, 0, 0
            )
            holder.favoriteButton.text = "Save"
        }
    }
}