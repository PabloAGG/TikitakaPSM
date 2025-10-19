package com.example.tikitaka

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.tikitaka.api.ApiClient
import com.example.tikitaka.utils.PreferencesManager
import com.example.tikitaka.utils.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        preferencesManager = PreferencesManager(this)
        
        // Verificar autenticación
        if (!preferencesManager.isLoggedIn()) {
            navigateToLogin()
            return
        }
        
        // Configurar token para las llamadas API
        preferencesManager.getAuthToken()?.let { token ->
            ApiClient.setAuthToken(token)
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupNavigation()
        verifyTokenValidityAsync()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)
    }
    
    private fun verifyTokenValidityAsync() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.verifyToken()
                if (!response.isSuccessful || response.body()?.success != true) {
                    // Token inválido o expirado
                    handleInvalidToken()
                } else {
                    // Token válido, actualizar datos del usuario si es necesario
                    response.body()?.data?.let { user ->
                        preferencesManager.updateUserData(
                            username = user.username,
                            fullName = user.fullName,
                            profileImage = user.profileImage,
                            teamName = user.teamName,
                            teamLogo = user.teamLogo
                        )
                    }
                }
            } catch (e: Exception) {
                // Error de red, pero permitir continuar
                // El usuario puede seguir usando la app offline
            }
        }
    }
    
    private fun handleInvalidToken() {
        // Limpiar datos de sesión
        preferencesManager.clearUserData()
        ApiClient.clearAuthToken()
        
        Utils.showToast(this, "Sesión expirada. Por favor inicia sesión nuevamente.")
        navigateToLogin()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    fun logout() {
        preferencesManager.clearUserData()
        ApiClient.clearAuthToken()
        navigateToLogin()
    }
}