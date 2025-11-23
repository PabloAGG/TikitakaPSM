package com.example.tikitaka.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    
    // Formatear fechas
    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)
            
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
    
    // Formatear fechas relativas (hace 2 horas, hace 1 día, etc.)
    fun formatRelativeDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)
            
            val now = Date()
            val diffInMillis = now.time - (date?.time ?: 0)
            val diffInMinutes = diffInMillis / (1000 * 60)
            val diffInHours = diffInMinutes / 60
            val diffInDays = diffInHours / 24
            
            when {
                diffInMinutes < 1 -> "Ahora"
                diffInMinutes < 60 -> "${diffInMinutes.toInt()} min"
                diffInHours < 24 -> "${diffInHours.toInt()} h"
                diffInDays < 7 -> "${diffInDays.toInt()} d"
                else -> {
                    val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                    outputFormat.format(date ?: Date())
                }
            }
        } catch (e: Exception) {
            "Ahora"
        }
    }
    
    // Mostrar toast
    fun showToast(context: Context, message: String, isLong: Boolean = false) {
        Toast.makeText(
            context, 
            message, 
            if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }
    
    // Validar email
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // Validar contraseña
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
    
    // Formatear números (1000 -> 1K, 1000000 -> 1M)
    fun formatNumber(number: Int): String {
        return when {
            number >= 1000000 -> "${number / 1000000}M"
            number >= 1000 -> "${number / 1000}K"
            else -> number.toString()
        }
    }
    
    // Obtener URL completa de imagen
    fun getFullImageUrl(imageUrl: String?): String? {
        return if (imageUrl != null && !imageUrl.startsWith("http")) {
            "http://10.0.2.2:3000$imageUrl" // Para emulador
            // Para dispositivo físico: "http://tu_ip_local:3000$imageUrl"
        } else {
            imageUrl
        }
    }
    
    // Capitalizar primera letra
    fun capitalize(text: String): String {
        return text.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }
    }
    
    // Truncar texto
    fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            "${text.substring(0, maxLength)}..."
        } else {
            text
        }
    }
    
    // Obtener iniciales del nombre
    fun getInitials(fullName: String): String {
        val names = fullName.trim().split(" ")
        return when {
            names.size >= 2 -> "${names[0].first()}${names[1].first()}".uppercase()
            names.size == 1 -> names[0].take(2).uppercase()
            else -> "TK"
        }
    }
    
    // Validar que no esté vacío
    fun isNotEmpty(text: String?): Boolean {
        return !text.isNullOrBlank()
    }
    
    // Limpiar y validar texto
    fun cleanText(text: String?): String {
        return text?.trim() ?: ""
    }
    
    // Obtener archivo desde URI
    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}