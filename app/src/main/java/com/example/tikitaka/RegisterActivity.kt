package com.example.tikitaka

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.models.RegisterRequest
import com.example.tikitaka.models.Team
import com.example.tikitaka.utils.PreferencesManager
import com.example.tikitaka.utils.Utils
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var lastnameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var teamSpinner: Spinner
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView
    private lateinit var progressBar: ProgressBar
    
    private lateinit var preferencesManager: PreferencesManager
    private var teams: List<Team> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        preferencesManager = PreferencesManager(this)
        
        initViews()
        setupListeners()
        loadTeams()
    }

    private fun initViews() {
        nameInput = findViewById(R.id.name_input)
        lastnameInput = findViewById(R.id.lastname_input)
        emailInput = findViewById(R.id.email_input)
        phoneInput = findViewById(R.id.phone_input)
        teamSpinner = findViewById(R.id.team_spinner)
        passwordInput = findViewById(R.id.password_input)
        confirmPasswordInput = findViewById(R.id.confirm_password_input)
        registerButton = findViewById(R.id.register_button)
        loginLink = findViewById(R.id.login_link)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupListeners() {
        registerButton.setOnClickListener {
            performRegister()
        }

        loginLink.setOnClickListener {
            finish() // Go back to login
        }
    }

    private fun loadTeams() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTeams()
                if (response.isSuccessful && response.body()?.success == true) {
                    teams = response.body()?.teams ?: emptyList()
                    // El spinner ya debería estar configurado con el array de strings.xml
                    // Los equipos se usarán para obtener el ID cuando se registre
                }
            } catch (e: Exception) {
                // Si no se pueden cargar los equipos, usar los valores por defecto del spinner
                Utils.showToast(this@RegisterActivity, "Error cargando equipos", false)
            }
        }
    }

    private fun performRegister() {
        val name = nameInput.text.toString().trim()
        val lastname = lastnameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val favoriteTeam = teamSpinner.selectedItem.toString()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        // Validación
        if (!validateInput(name, lastname, email, phone, password, confirmPassword)) {
            return
        }

        // Obtener ID del equipo seleccionado
        val teamId = getTeamIdFromSpinner()
        if (teamId == -1) {
            Utils.showToast(this, "Error: No se pudo obtener el equipo seleccionado")
            return
        }

        // Mostrar loading
        setLoading(true)

        // Llamada a la API
        lifecycleScope.launch {
            try {
                val fullName = "$name $lastname"
                val username = generateUsername(name, lastname)
                
                val registerRequest = RegisterRequest(
                    email = email,
                    password = password,
                    username = username,
                    fullName = fullName,
                    teamId = teamId
                )
                
                val response = ApiClient.apiService.register(registerRequest)

                if (response.isSuccessful && response.body()?.success == true) {
                    val registerResponse = response.body()!!
                    
                    // Guardar token y datos del usuario
                    registerResponse.token?.let { token ->
                        ApiClient.setAuthToken(token)
                        preferencesManager.saveAuthToken(token)
                    }
                    
                    registerResponse.user?.let { user ->
                        preferencesManager.saveUserData(
                            userId = user.id,
                            username = user.username,
                            email = user.email,
                            fullName = user.fullName,
                            profileImage = user.profileImage,
                            teamName = user.teamName,
                            teamLogo = user.teamLogo
                        )
                    }
                    
                    Utils.showToast(this@RegisterActivity, "¡Registro exitoso! Bienvenido a TikiTaka")
                    navigateToMain()
                    
                } else {
                    val errorMessage = response.body()?.message ?: "Error en el registro"
                    Utils.showToast(this@RegisterActivity, errorMessage, true)
                }
            } catch (e: Exception) {
                Utils.showToast(this@RegisterActivity, "Error de conexión. Verifica tu internet.", true)
            } finally {
                setLoading(false)
            }
        }
    }

    private fun validateInput(name: String, lastname: String, email: String, 
                            phone: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            nameInput.error = "El nombre es requerido"
            isValid = false
        }

        if (lastname.isEmpty()) {
            lastnameInput.error = "El apellido es requerido"
            isValid = false
        }

        if (email.isEmpty()) {
            emailInput.error = "El correo es requerido"
            isValid = false
        } else if (!Utils.isValidEmail(email)) {
            emailInput.error = "Correo inválido"
            isValid = false
        }

        if (phone.isEmpty()) {
            phoneInput.error = "El teléfono es requerido"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordInput.error = "La contraseña es requerida"
            isValid = false
        } else if (!Utils.isValidPassword(password)) {
            passwordInput.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        if (password != confirmPassword) {
            confirmPasswordInput.error = "Las contraseñas no coinciden"
            isValid = false
        }

        return isValid
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
        
        // Fallback: mapear según el índice del spinner (basado en strings.xml)
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

    private fun generateUsername(name: String, lastname: String): String {
        val cleanName = name.lowercase().replace(" ", "")
        val cleanLastname = lastname.lowercase().replace(" ", "")
        return "${cleanName}_${cleanLastname}_${(1000..9999).random()}"
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            progressBar.visibility = View.VISIBLE
            registerButton.isEnabled = false
            registerButton.text = "Registrando..."
        } else {
            progressBar.visibility = View.GONE
            registerButton.isEnabled = true
            registerButton.text = "Registrarse"
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}