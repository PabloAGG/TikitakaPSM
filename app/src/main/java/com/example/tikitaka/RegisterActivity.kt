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
    private var teamAdapter: android.widget.ArrayAdapter<String>? = null

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
                    
                    if (teams.isNotEmpty()) {
                        // Actualizar el spinner dinámicamente con los equipos de la API
                        updateTeamSpinner()
                    } else {
                        android.util.Log.w("RegisterActivity", "Lista de equipos vacía")
                    }
                } else {
                    // Usar equipos por defecto del spinner si falla la carga
                    android.util.Log.w("RegisterActivity", "No se pudieron cargar equipos desde API")
                }
            } catch (e: Exception) {
                // Si no se pueden cargar los equipos, usar los valores por defecto del spinner
                android.util.Log.e("RegisterActivity", "Error cargando equipos: ${e.message}")
            }
        }
    }
    
    private fun updateTeamSpinner() {
        // Crear lista de nombres de equipos
        val teamNames = teams.map { it.name }
        
        // Crear y configurar el adapter
        teamAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            teamNames
        )
        teamAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        // Asignar el adapter al spinner
        teamSpinner.adapter = teamAdapter
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
                    
                    // Validar que tenemos token y usuario
                    if (registerResponse.token != null && registerResponse.user != null) {
                        // Guardar token primero
                        ApiClient.setAuthToken(registerResponse.token)
                        preferencesManager.saveAuthToken(registerResponse.token)
                        
                        // Guardar datos del usuario
                        val user = registerResponse.user
                        preferencesManager.saveUserData(
                            userId = user.id,
                            username = user.username,
                            email = user.email,
                            fullName = user.fullName,
                            profileImage = user.profileImage,
                            teamName = user.teamName,
                            teamLogo = user.teamLogo
                        )
                        
                        Utils.showToast(this@RegisterActivity, "¡Bienvenido a TikiTaka, ${user.fullName}!")
                        navigateToMain()
                    } else {
                        Utils.showToast(this@RegisterActivity, "Error: datos incompletos del servidor", true)
                        setLoading(false)
                    }
                    
                } else {
                    // Manejar diferentes códigos de error
                    val errorMessage = when {
                        response.body()?.message != null -> response.body()!!.message
                        response.code() == 409 -> "El email o nombre de usuario ya están en uso"
                        response.code() == 400 -> "Datos inválidos. Verifica los campos."
                        response.code() >= 500 -> "Error del servidor. Intenta más tarde."
                        else -> "Error en el registro. Código: ${response.code()}"
                    }
                    Utils.showToast(this@RegisterActivity, errorMessage, true)
                    setLoading(false)
                }
            } catch (e: java.net.UnknownHostException) {
                Utils.showToast(this@RegisterActivity, "No se puede conectar al servidor. Verifica tu conexión.", true)
                setLoading(false)
            } catch (e: java.net.SocketTimeoutException) {
                Utils.showToast(this@RegisterActivity, "Tiempo de espera agotado. Intenta de nuevo.", true)
                setLoading(false)
            } catch (e: Exception) {
                Utils.showToast(this@RegisterActivity, "Error: ${e.message ?: "Desconocido"}", true)
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

        // El teléfono es opcional, pero si se proporciona debe ser válido
        if (phone.isNotEmpty() && phone.length < 8) {
            phoneInput.error = "Teléfono debe tener al menos 8 dígitos"
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
        // Si tenemos la lista de equipos de la API, usar el índice seleccionado
        if (teams.isNotEmpty()) {
            val selectedPosition = teamSpinner.selectedItemPosition
            if (selectedPosition >= 0 && selectedPosition < teams.size) {
                return teams[selectedPosition].id
            }
        }
        
        // Fallback: Si no hay equipos de la API, mapear según strings.xml
        val selectedTeamName = teamSpinner.selectedItem?.toString() ?: ""
        
        return when {
            selectedTeamName.contains("Argentina", ignoreCase = true) -> 1
            selectedTeamName.contains("Brasil", ignoreCase = true) -> 2
            selectedTeamName.contains("Uruguay", ignoreCase = true) -> 3
            selectedTeamName.contains("Colombia", ignoreCase = true) -> 4
            selectedTeamName.contains("Chile", ignoreCase = true) -> 5
            selectedTeamName.contains("Perú", ignoreCase = true) -> 6
            selectedTeamName.contains("Ecuador", ignoreCase = true) -> 7
            selectedTeamName.contains("Venezuela", ignoreCase = true) -> 8
            selectedTeamName.contains("Bolivia", ignoreCase = true) -> 9
            selectedTeamName.contains("Paraguay", ignoreCase = true) -> 10
            selectedTeamName.contains("España", ignoreCase = true) -> 11
            selectedTeamName.contains("Francia", ignoreCase = true) -> 12
            selectedTeamName.contains("Alemania", ignoreCase = true) -> 13
            selectedTeamName.contains("Italia", ignoreCase = true) -> 14
            selectedTeamName.contains("Inglaterra", ignoreCase = true) -> 15
            selectedTeamName.contains("Portugal", ignoreCase = true) -> 16
            selectedTeamName.contains("Países Bajos", ignoreCase = true) || 
            selectedTeamName.contains("Holanda", ignoreCase = true) -> 17
            selectedTeamName.contains("Bélgica", ignoreCase = true) -> 18
            selectedTeamName.contains("Croacia", ignoreCase = true) -> 19
            selectedTeamName.contains("México", ignoreCase = true) -> 20
            else -> 1 // Default a Argentina si no se encuentra
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