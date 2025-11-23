package com.example.tikitaka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.models.Comment
import com.example.tikitaka.utils.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*

class CommentsAdapter(
    private val currentUserId: Int,
    private val onLikeClick: (Comment, Int) -> Unit,
    private val onDeleteClick: (Comment, Int) -> Unit
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val comments = mutableListOf<Comment>()

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: ImageView = itemView.findViewById(R.id.user_avatar)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val commentDate: TextView = itemView.findViewById(R.id.comment_date)
        val commentContent: TextView = itemView.findViewById(R.id.comment_content)
        val btnLike: ImageView = itemView.findViewById(R.id.btn_like)
        val likesCount: TextView = itemView.findViewById(R.id.likes_count)
        val likeContainer: View = itemView.findViewById(R.id.like_container)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)

        fun bind(comment: Comment, position: Int) {
            userName.text = comment.fullName
            commentContent.text = comment.content
            likesCount.text = comment.likesCount.toString()
            commentDate.text = formatDate(comment.createdAt)

            // Cargar avatar
            if (!comment.profileImage.isNullOrEmpty()) {
                val imageUrl = "${ApiClient.BASE_URL}${comment.profileImage}"
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(userAvatar)
            } else {
                userAvatar.setImageResource(R.drawable.ic_person)
            }

            // Estado del like
            if (comment.isLiked) {
                btnLike.setColorFilter(itemView.context.getColor(R.color.purple_500))
            } else {
                btnLike.setColorFilter(itemView.context.getColor(android.R.color.darker_gray))
            }

            // Mostrar botón eliminar solo para comentarios propios
            btnDelete.visibility = if (comment.userId == currentUserId) View.VISIBLE else View.GONE

            // Listeners
            likeContainer.setOnClickListener {
                onLikeClick(comment, position)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(comment, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position], position)
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    fun addComment(comment: Comment) {
        comments.add(0, comment)
        notifyItemInserted(0)
    }

    fun updateComment(position: Int, comment: Comment) {
        if (position in comments.indices) {
            comments[position] = comment
            notifyItemChanged(position)
        }
    }

    fun removeComment(position: Int) {
        if (position in comments.indices) {
            comments.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString) ?: return dateString

            val now = Date()
            val diffInMillis = now.time - date.time
            val diffInMinutes = diffInMillis / (1000 * 60)
            val diffInHours = diffInMillis / (1000 * 60 * 60)
            val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

            when {
                diffInMinutes < 1 -> "Ahora"
                diffInMinutes < 60 -> "${diffInMinutes}m"
                diffInHours < 24 -> "${diffInHours}h"
                diffInDays < 7 -> "${diffInDays}d"
                else -> {
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    outputFormat.format(date)
                }
            }
        } catch (e: Exception) {
            dateString
        }
    }
}
