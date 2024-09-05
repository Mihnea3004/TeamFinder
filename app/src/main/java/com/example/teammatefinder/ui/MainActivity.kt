package com.example.teammatefinder.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.teammatefinder.R

class MainActivity : AppCompatActivity() {
    lateinit var userInput : EditText
    lateinit var passwordInput : EditText
    lateinit var loginButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        userInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        loginButton.setOnClickListener {
            val username = userInput.text.toString()
            val password = passwordInput.text.toString()
            Log.i("Test Credits","Username: $username, Password: $password")
    }
}
}