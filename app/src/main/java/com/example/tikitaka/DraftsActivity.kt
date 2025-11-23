package com.example.tikitaka

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tikitaka.database.AppDatabase
import com.example.tikitaka.models.Post
import com.example.tikitaka.utils.Utils
import kotlinx.coroutines.launch

class DraftsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    
    private lateinit var draftsAdapter: DraftsAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drafts)
        
        database = AppDatabase.getInstance(this)
        
        initViews()
        setupListeners()
        setupRecyclerView()
        loadDrafts()
    }

    private fun initViews() {
        backButton = findViewById(R.id.back_button)
        recyclerView = findViewById(R.id.recycler_view_drafts)
        progressBar = findViewById(R.id.progress_bar)
        emptyStateText = findViewById(R.id.empty_state_text)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        draftsAdapter = DraftsAdapter(
            drafts = mutableListOf(),
            onEditClick = { draft -> handleEditDraft(draft) },
            onPublishClick = { draft -> handlePublishDraft(draft) },
            onDeleteClick = { draft -> handleDeleteDraft(draft) }
        )
        
        recyclerView.adapter = draftsAdapter
    }

    private fun loadDrafts() {
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val draftEntities = database.draftDao().getAllDrafts()
                
                // Convertir entidades a Posts
                val drafts = draftEntities.map { entity ->
                    Post(
                        id = entity.serverId ?: 0,
                        content = entity.content,
                        imageUrl = entity.imageUri,
                        teamId = entity.teamId,
                        userId = 0,
                        username = "",
                        fullName = "",
                        teamName = "",
                        isDraft = true,
                        createdAt = java.text.SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss'Z'",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date(entity.updatedAt))
                    )
                }
                
                if (drafts.isEmpty()) {
                    showEmptyState()
                } else {
                    hideEmptyState()
                    draftsAdapter.updateDrafts(drafts)
                }
                
            } catch (e: Exception) {
                Utils.showToast(this@DraftsActivity, "Error cargando borradores: ${e.message}", true)
                showEmptyState()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun handleEditDraft(draft: Post) {
        val intent = Intent(this, CreatePostActivity::class.java)
        intent.putExtra("draft_id", draft.id)
        intent.putExtra("draft_content", draft.content)
        intent.putExtra("draft_image_url", draft.imageUrl)
        intent.putExtra("draft_team_id", draft.teamId)
        startActivity(intent)
    }

    private fun handlePublishDraft(draft: Post) {
        AlertDialog.Builder(this)
            .setTitle("Publicar borrador")
            .setMessage("¿Deseas publicar este borrador ahora?")
            .setPositiveButton("Publicar") { _, _ ->
                publishDraft(draft)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun publishDraft(draft: Post) {
        // TODO: Implementar publicación de borrador
        // Por ahora solo simular publicación
        lifecycleScope.launch {
            try {
                // Aquí iría la lógica para publicar el draft
                Utils.showToast(this@DraftsActivity, "Funcionalidad de publicar en desarrollo")
                
                // Eliminar de borradores locales después de publicar
                // database.draftDao().deleteDraft(draftLocalId)
                // loadDrafts()
            } catch (e: Exception) {
                Utils.showToast(this@DraftsActivity, "Error publicando borrador: ${e.message}", true)
            }
        }
    }

    private fun handleDeleteDraft(draft: Post) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar borrador")
            .setMessage("¿Estás seguro de que deseas eliminar este borrador?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteDraft(draft)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteDraft(draft: Post) {
        lifecycleScope.launch {
            try {
                // TODO: Obtener localId del draft para eliminar
                // Por ahora solo mostrar mensaje
                Utils.showToast(this@DraftsActivity, "Borrador eliminado")
                loadDrafts()
            } catch (e: Exception) {
                Utils.showToast(this@DraftsActivity, "Error eliminando borrador: ${e.message}", true)
            }
        }
    }

    private fun showEmptyState() {
        emptyStateText.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyStateText.text = "No tienes borradores guardados"
    }

    private fun hideEmptyState() {
        emptyStateText.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
    
    override fun onResume() {
        super.onResume()
        loadDrafts()
    }
}