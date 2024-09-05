package com.example.teammatefinder.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.teammatefinder.R

class MainActivity : AppCompatActivity() {
    private lateinit var userInput : EditText
    private lateinit var passwordInput : EditText
    private lateinit var loginButton : Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        userInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        val databaseHelper = DatabaseHelper(this)
        fun loginDatabase(username: String, password: String){
            val userExists = databaseHelper.readUser(username, password)
            if (userExists) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Login failed, check your data or create an account", Toast.LENGTH_SHORT).show()
            }
        }
        loginButton.setOnClickListener {
            val username = userInput.text.toString()
            val password = passwordInput.text.toString()
            loginDatabase(username, password)
        }
        registerButton = findViewById(R.id.register_button)
        registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
    }
}
}