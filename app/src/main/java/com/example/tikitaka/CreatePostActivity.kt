package com.example.tikitaka

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.models.CreatePostRequest
import com.example.tikitaka.models.UpdatePostRequest
import com.example.tikitaka.models.Team
import com.example.tikitaka.utils.Utils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CreatePostActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var descriptionEditText: EditText
    private lateinit var teamSpinner: Spinner
    private lateinit var postImageView: ImageView
    private lateinit var saveButton: Button
    private lateinit var saveDraftButton: Button
    private lateinit var progressBar: ProgressBar
    
    private var selectedImageUri: Uri? = null
    private var teams: List<Team> = emptyList()
    private var isEditMode = false
    private var editingPostId: Int? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(postImageView)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        
        initViews()
        setupListeners()
        loadTeams()
        
        // Verificar si estamos editando un post existente
        checkEditMode()
    }

    private fun initViews() {
        backButton = findViewById(R.id.btn_back)
        descriptionEditText = findViewById(R.id.edit_description)
        teamSpinner = findViewById(R.id.team_spinner)
        postImageView = findViewById(R.id.post_image)
        saveButton = findViewById(R.id.btn_save)
        saveDraftButton = findViewById(R.id.btn_save_draft)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            publishPost()
        }

        saveDraftButton.setOnClickListener {
            saveDraft()
        }

        postImageView.setOnClickListener {
            openImagePicker()
        }
    }

    private fun loadTeams() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTeams()
                if (response.isSuccessful && response.body()?.success == true) {
                    teams = response.body()?.teams ?: emptyList()
                }
            } catch (e: Exception) {
                // Error loading teams, use default spinner values
            }
        }
    }

    private fun checkEditMode() {
        editingPostId = intent.getIntExtra("post_id", -1)
        if (editingPostId != -1) {
            isEditMode = true
            saveButton.text = "Actualizar Post"
            saveDraftButton.visibility = View.GONE
            
            // Cargar datos del post a editar
            loadPostData()
        }
    }

    private fun loadPostData() {
        // TODO: Implementar carga de datos del post a editar
        // Por ahora solo cambiar el título
        title = "Editar Post"
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun publishPost() {
        createPost(isDraft = false)
    }

    private fun saveDraft() {
        createPost(isDraft = true)
    }

    private fun createPost(isDraft: Boolean) {
        val description = descriptionEditText.text.toString().trim()
        val selectedTeam = teamSpinner.selectedItem.toString()

        // Validación
        if (!validateInput(description)) {
            return
        }

        val teamId = getTeamIdFromSpinner()
        if (teamId == -1) {
            Utils.showToast(this, "Error: No se pudo obtener el equipo seleccionado")
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                var imageUrl: String? = null
                
                // Subir imagen si se seleccionó una
                selectedImageUri?.let { uri ->
                    imageUrl = uploadImage(uri)
                }

                // Crear o actualizar post
                val request = CreatePostRequest(
                    content = description,
                    teamId = teamId,
                    isDraft = isDraft
                )

                val response = if (isEditMode) {
                    val updateRequest = UpdatePostRequest(
                        content = description,
                        teamId = teamId,
                        isDraft = isDraft
                    )
                    ApiClient.apiService.updatePost(editingPostId!!, updateRequest)
                } else {
                    ApiClient.apiService.createPost(request)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val message = when {
                        isEditMode -> "Post actualizado exitosamente"
                        isDraft -> "Borrador guardado exitosamente"
                        else -> "Post publicado exitosamente"
                    }
                    
                    Utils.showToast(this@CreatePostActivity, message)
                    setResult(Activity.RESULT_OK)
                    finish()
                    
                } else {
                    val errorMessage = response.body()?.message ?: "Error al procesar el post"
                    Utils.showToast(this@CreatePostActivity, errorMessage, true)
                }
                
            } catch (e: Exception) {
                Utils.showToast(this@CreatePostActivity, "Error de conexión", true)
            } finally {
                setLoading(false)
            }
        }
    }

    private suspend fun uploadImage(imageUri: Uri): String? {
        return try {
            val file = createFileFromUri(imageUri)
            if (file != null) {
                val requestFile = file.asRequestBody("image/*".toMediaType())
                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                
                val response = ApiClient.apiService.uploadPostImage(body)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun createFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun validateInput(description: String): Boolean {
        if (description.isEmpty()) {
            descriptionEditText.error = "El contenido es requerido"
            return false
        }

        if (description.length < 10) {
            descriptionEditText.error = "El contenido debe tener al menos 10 caracteres"
            return false
        }

        return true
    }

    private fun getTeamIdFromSpinner(): Int {
        val selectedTeamName = teamSpinner.selectedItem.toString()
        
        // Si tenemos la lista de equipos de la API, buscar por nombre
        if (teams.isNotEmpty()) {
            val team = teams.find { it.name == selectedTeamName }
            if (team != null) {
                return team.id
            }
        }
        
        // Fallback: mapear según el índice del spinner
        return when (teamSpinner.selectedItemPosition) {
            0 -> 1  // "Todas las selecciones" -> usar Argentina como default
            1 -> 1  // Argentina
            2 -> 2  // Brasil  
            3 -> 20 // México
            4 -> 11 // España
            5 -> 12 // Francia
            6 -> 13 // Alemania
            7 -> 14 // Italia
            8 -> 15 // Inglaterra
            9 -> 16 // Portugal
            10 -> 17 // Países Bajos
            else -> 1 // Default a Argentina
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            progressBar.visibility = View.VISIBLE
            saveButton.isEnabled = false
            saveDraftButton.isEnabled = false
            saveButton.text = if (isEditMode) "Actualizando..." else "Publicando..."
        } else {
            progressBar.visibility = View.GONE
            saveButton.isEnabled = true
            saveDraftButton.isEnabled = true
            saveButton.text = if (isEditMode) "Actualizar Post" else "Publicar"
        }
    }
}