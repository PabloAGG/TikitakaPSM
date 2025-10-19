package com.example.tikitaka

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        
        initViews()
        setupListeners()
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
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        changeImageButton.setOnClickListener {
            // TODO: Implement image picker
        }

        saveButton.setOnClickListener {
            saveChanges()
        }

        changePasswordSwitch.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Show/hide password fields when toggled
        }
    }

    private fun loadUserData() {
        // TODO: Load user data from preferences or database
        // For now, populate with mock data
        nameInput.setText("Usuario")
        lastnameInput.setText("Ejemplo")
        emailInput.setText("usuario@ejemplo.com")
        phoneInput.setText("8199191919")
        
        // Set default team selection
        teamSpinner.setSelection(1) // Argentina by default
    }

    private fun saveChanges() {
        val name = nameInput.text.toString().trim()
        val lastname = lastnameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val favoriteTeam = teamSpinner.selectedItem.toString()

        // Basic validation
        if (name.isEmpty()) {
            nameInput.error = "El nombre es requerido"
            return
        }

        if (lastname.isEmpty()) {
            lastnameInput.error = "El apellido es requerido"
            return
        }

        if (email.isEmpty()) {
            emailInput.error = "El correo es requerido"
            return
        }

        if (phone.isEmpty()) {
            phoneInput.error = "El teléfono es requerido"
            return
        }

        // TODO: Save changes to preferences or database
        
        finish() // Return to previous screen
    }
}