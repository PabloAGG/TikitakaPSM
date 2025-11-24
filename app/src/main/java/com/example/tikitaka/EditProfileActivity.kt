package com.example.tikitaka

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.models.Team
import com.example.tikitaka.utils.PreferencesManager
import com.example.tikitaka.utils.Utils
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var profileImage: ImageView
    private lateinit var changeImageButton: Button
    private lateinit var nameInput: TextInputEditText
    private lateinit var lastnameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var teamSpinner: Spinner
    private lateinit var changePasswordSwitch: MaterialSwitch
    private lateinit var saveButton: Button
    private var progressBar: ProgressBar? = null
    
    private lateinit var preferencesManager: PreferencesManager
    private var teams: List<Team> = emptyList()
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        
        preferencesManager = PreferencesManager(this)
        
        initViews()
        setupListeners()
        loadTeams()
        loadUserData()
    }

    private fun initViews() {
        backButton = findViewById(R.id.back_button)
        profileImage = findViewById(R.id.profile_image)
        changeImageButton = findViewById(R.id.change_image_button)
        nameInput = findViewById(R.id.name_input)
        lastnameInput = findViewById(R.id.lastname_input)
        emailInput = findViewById(R.id.email_input)
        phoneInput = findViewById(R.id.phone_input)
        teamSpinner = findViewById(R.id.team_spinner)
        changePasswordSwitch = findViewById(R.id.change_password_switch)
        saveButton = findViewById(R.id.save_button)
        progressBar = findViewById(R.id.progress_bar)
        
        // TODO: Agregar campos de contraseña al layout si es necesario
        // oldPasswordLayout = findViewById(R.id.old_password_layout)
        // newPasswordLayout = findViewById(R.id.new_password_layout)
        // oldPasswordInput = findViewById(R.id.old_password_input)
        // newPasswordInput = findViewById(R.id.new_password_input)
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(profileImage)
            }
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Utils.showToast(this, "Permiso denegado", true)
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        changeImageButton.setOnClickListener {
            checkPermissionAndPickImage()
        }

        saveButton.setOnClickListener {
            saveChanges()
        }

        changePasswordSwitch.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Implementar mostrar/ocultar campos de contraseña cuando se agreguen al layout
            if (isChecked) {
                Utils.showToast(this, "Funcionalidad de cambio de contraseña en desarrollo")
            }
        }
    }
    
    private fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun loadTeams() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTeams()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    teams = response.body()!!.teams
                    updateTeamSpinner()
                }
            } catch (e: Exception) {
                Utils.showToast(this@EditProfileActivity, "Error cargando equipos", true)
            }
        }
    }
    
    private fun updateTeamSpinner() {
        val teamNames = teams.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, teamNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        teamSpinner.adapter = adapter
        
        // Seleccionar equipo actual del usuario
        val currentTeamName = preferencesManager.getUserTeamName()
        val teamIndex = teams.indexOfFirst { it.name == currentTeamName }
        if (teamIndex != -1) {
            teamSpinner.setSelection(teamIndex)
        }
    }

    private fun loadUserData() {
        val fullName = preferencesManager.getUserFullName() ?: ""
        val email = preferencesManager.getUserEmail() ?: ""
        val profileImageUrl = preferencesManager.getUserProfileImage()
        
        // Separar nombre completo en nombre y apellido
        val nameParts = fullName.split(" ", limit = 2)
        if (nameParts.isNotEmpty()) {
            nameInput.setText(nameParts[0])
            if (nameParts.size > 1) {
                lastnameInput.setText(nameParts[1])
            }
        }
        
        emailInput.setText(email)
        phoneInput.setText("") // Teléfono no está en PreferencesManager
        
        // Cargar imagen de perfil
        if (!profileImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_person)
                .into(profileImage)
        }
    }

    private fun saveChanges() {
        val name = nameInput.text.toString().trim()
        val lastname = lastnameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val fullName = "$name $lastname".trim()
        
        // Validación básica
        if (name.isEmpty()) {
            nameInput.error = "El nombre es requerido"
            nameInput.requestFocus()
            return
        }

        if (lastname.isEmpty()) {
            lastnameInput.error = "El apellido es requerido"
            lastnameInput.requestFocus()
            return
        }

        if (email.isEmpty()) {
            emailInput.error = "El correo es requerido"
            emailInput.requestFocus()
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Correo inválido"
            emailInput.requestFocus()
            return
        }
        
        // TODO: Validar cambio de contraseña cuando se implemente
        // if (changePasswordSwitch.isChecked) {
        //     val oldPassword = oldPasswordInput.text.toString()
        //     val newPassword = newPasswordInput.text.toString()
        //     ...
        // }
        
        // Obtener equipo seleccionado
        val selectedTeamPosition = teamSpinner.selectedItemPosition
        val selectedTeam = if (teams.isNotEmpty() && selectedTeamPosition >= 0) {
            teams[selectedTeamPosition]
        } else null
        
        progressBar?.visibility = View.VISIBLE
        saveButton.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // 1. Subir imagen si se seleccionó una nueva
                var uploadedImageUrl: String? = null
                selectedImageUri?.let { uri ->
                    uploadedImageUrl = uploadProfileImage(uri)
                }
                
                // 2. Actualizar perfil
                // TODO: Implementar endpoint de actualización de perfil en API
                // Por ahora solo actualizar PreferencesManager
                
                preferencesManager.updateUserData(
                    username = preferencesManager.getUsername(),
                    fullName = fullName,
                    profileImage = uploadedImageUrl ?: preferencesManager.getUserProfileImage(),
                    teamName = selectedTeam?.name ?: preferencesManager.getUserTeamName(),
                    teamLogo = selectedTeam?.logoUrl ?: preferencesManager.getUserTeamLogo()
                )
                
                // 3. TODO: Cambiar contraseña si está activado cuando se implemente
                
                Utils.showToast(this@EditProfileActivity, "Perfil actualizado exitosamente")
                finish()
                
            } catch (e: Exception) {
                Utils.showToast(this@EditProfileActivity, "Error al actualizar: ${e.message}", true)
            } finally {
                progressBar?.visibility = View.GONE
                saveButton.isEnabled = true
            }
        }
    }
    
    private suspend fun uploadProfileImage(uri: Uri): String? {
        return try {
            val file = Utils.getFileFromUri(this, uri)
            if (file != null) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("avatar", file.name, requestFile)
                
                val response = ApiClient.apiService.uploadAvatar(body)
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
}