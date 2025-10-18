package com.example.tikitaka

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class CreatePostActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var saveButton: ImageButton
    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var publishButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        
        initViews()
        setupListeners()
    }

    private fun initViews() {
        backButton = findViewById(R.id.btn_back)
        saveButton = findViewById(R.id.btn_save)
        imageView = findViewById(R.id.post_image)
        uploadButton = findViewById(R.id.btn_upload_image)
        titleEditText = findViewById(R.id.edit_title)
        descriptionEditText = findViewById(R.id.edit_description)
        publishButton = findViewById(R.id.btn_publish)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            // TODO: Implement save functionality
            finish()
        }

        uploadButton.setOnClickListener {
            // TODO: Implement image upload
        }

        publishButton.setOnClickListener {
            // TODO: Implement publish functionality
            finish()
        }
    }
}