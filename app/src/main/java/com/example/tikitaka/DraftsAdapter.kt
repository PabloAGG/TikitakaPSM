package com.example.tikitaka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tikitaka.models.Post
import com.example.tikitaka.utils.Utils

class DraftsAdapter(
    private var drafts: MutableList<Post>,
    private val onEditClick: (Post) -> Unit,
    private val onPublishClick: (Post) -> Unit,
    private val onDeleteClick: (Post) -> Unit
) : RecyclerView.Adapter<DraftsAdapter.DraftViewHolder>() {

    class DraftViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userAvatar: ImageView = view.findViewById(R.id.user_avatar)
        val userName: TextView = view.findViewById(R.id.user_name)
        val postDate: TextView = view.findViewById(R.id.post_date)
        val postContent: TextView = view.findViewById(R.id.post_content)
        val postImage: ImageView = view.findViewById(R.id.post_image)
        val publishButton: Button = view.findViewById(R.id.btn_publish)
        val editButton: Button = view.findViewById(R.id.btn_edit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_draft_post, parent, false)
        return DraftViewHolder(view)
    }

    override fun onBindViewHolder(holder: DraftViewHolder, position: Int) {
        val draft = drafts[position]
        
        holder.userName.text = "Borrador"
        holder.postDate.text = Utils.formatRelativeDate(draft.createdAt)
        holder.postContent.text = draft.content
        
        holder.publishButton.setOnClickListener {
            onPublishClick(draft)
        }
        
        holder.editButton.setOnClickListener {
            onEditClick(draft)
        }
    }

    override fun getItemCount() = drafts.size

    fun updateDrafts(newDrafts: List<Post>) {
        drafts.clear()
        drafts.addAll(newDrafts)
        notifyDataSetChanged()
    }
}