package com.example.tikitaka

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.models.LoginRequest
import com.example.tikitaka.utils.PreferencesManager
import com.example.tikitaka.utils.Utils
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var progressBar: ProgressBar
    
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        preferencesManager = PreferencesManager(this)
        
        // Si ya está logueado, ir directamente al MainActivity
        if (preferencesManager.isLoggedIn()) {
            navigateToMain()
            return
        }
        
        initViews()
        setupListeners()
    }

    private fun initViews() {
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerLink = findViewById(R.id.register_link)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            performLogin()
        }

        registerLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Validación
        if (!validateInput(email, password)) {
            return
        }

        // Mostrar loading
        setLoading(true)

        // Llamada a la API
        lifecycleScope.launch {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = ApiClient.apiService.login(loginRequest)

                if (response.isSuccessful && response.body()?.success == true) {
                    val loginResponse = response.body()!!
                    
                    // Validar que tenemos token y usuario
                    if (loginResponse.token != null && loginResponse.user != null) {
                        // Guardar token primero
                        ApiClient.setAuthToken(loginResponse.token)
                        preferencesManager.saveAuthToken(loginResponse.token)
                        
                        // Guardar datos del usuario
                        val user = loginResponse.user
                        preferencesManager.saveUserData(
                            userId = user.id,
                            username = user.username,
                            email = user.email,
                            fullName = user.fullName,
                            profileImage = user.profileImage,
                            teamName = user.teamName,
                            teamLogo = user.teamLogo
                        )
                        
                        Utils.showToast(this@LoginActivity, "¡Bienvenido ${user.fullName}!")
                        navigateToMain()
                    } else {
                        Utils.showToast(this@LoginActivity, "Error: datos incompletos del servidor", true)
                        setLoading(false)
                    }
                    
                } else {
                    val errorMessage = when {
                        response.body()?.message != null -> response.body()!!.message
                        response.code() == 401 -> "Credenciales incorrectas. Verifica tu email y contraseña."
                        response.code() == 404 -> "Usuario no encontrado. ¿Ya te registraste?"
                        response.code() >= 500 -> "Error del servidor. Intenta más tarde."
                        else -> "Error en el login. Código: ${response.code()}"
                    }
                    Utils.showToast(this@LoginActivity, errorMessage, true)
                    setLoading(false)
                }
            } catch (e: java.net.UnknownHostException) {
                Utils.showToast(this@LoginActivity, "No se puede conectar al servidor. Verifica tu conexión.", true)
                setLoading(false)
            } catch (e: java.net.SocketTimeoutException) {
                Utils.showToast(this@LoginActivity, "Tiempo de espera agotado. Intenta de nuevo.", true)
                setLoading(false)
            } catch (e: Exception) {
                Utils.showToast(this@LoginActivity, "Error: ${e.message ?: "Desconocido"}", true)
                setLoading(false)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            emailInput.error = "El correo es requerido"
            isValid = false
        } else if (!Utils.isValidEmail(email)) {
            emailInput.error = "Correo inválido"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordInput.error = "La contraseña es requerida"
            isValid = false
        } else if (!Utils.isValidPassword(password)) {
            passwordInput.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        return isValid
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            progressBar.visibility = View.VISIBLE
            loginButton.isEnabled = false
            loginButton.text = "Iniciando sesión..."
        } else {
            progressBar.visibility = View.GONE
            loginButton.isEnabled = true
            loginButton.text = "Iniciar Sesión"
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}