package com.example.teammatefinder.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teammatefinder.R
import com.example.teammatefinder.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var backArrow : ImageButton
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var registervalidation : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        backArrow = findViewById(R.id.reigster_back)

        // Initialize Database Helper
        databaseHelper = DatabaseHelper(this)
        // Back button functionality
        backArrow.setOnClickListener {
            finish()  // Use finish() to return to the previous activity
        }

        registervalidation = findViewById(R.id.register_validation)
        registervalidation.setOnClickListener {
            val signUpUsername = binding.email.text.toString()
            val signUpPassword = binding.password.text.toString()
            registerUser(signUpUsername, signUpPassword)
        }
    }

    private fun registerUser(username: String, password: String) {
        val passwordMatch = password == binding.passwordVerification.text.toString()
        val emailMatch = username == binding.emailVerification.text.toString()
        val playsLOL = binding.lolCheckbox.isChecked
        val playsValorant = binding.valorantCheckbox.isChecked
        val playsTFT = binding.tftCheckbox.isChecked
        var valid = true

        if (passwordMatch && emailMatch) {
            // Check if user exists only by username, not both username and password
            val userExists = databaseHelper.readUser(username, password)
            if (!playsLOL && !playsValorant && !playsTFT)
                valid = false
            if (valid) {
                if (!userExists) {
                    val insertId = databaseHelper.insertDataUsers(
                        username,
                        password,
                        playsLOL,
                        playsValorant,
                        playsTFT
                    )
                    if (insertId != -1L) {
                        // Registration successful
                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Login::class.java))
                        finish()
                    } else {
                        // Registration failed
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // User already exists
                    Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show()
                }
            } else{
                Toast.makeText(this, "Please select at least one game", Toast.LENGTH_SHORT).show()
            }
        }else {
            Toast.makeText(this, "Emails or passwords do not match", Toast.LENGTH_SHORT).show()
        }
        }
    }
