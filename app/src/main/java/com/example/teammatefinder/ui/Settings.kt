package com.example.teammatefinder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.teammatefinder.R

class Settings : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        databaseHelper = DatabaseHelper(this)

        val saveButton: Button = findViewById(R.id.settings_button)
        val tagText: EditText = findViewById(R.id.settings_tag)
        val serverText: EditText = findViewById(R.id.settings_server)

        val username = intent.getStringExtra("username") ?: ""
        Log.e("Settings", "Username: $username")
        populateFieldsIfNeeded(username, tagText, serverText)

        saveButton.setOnClickListener {
            handleSaveButtonClick(username, tagText, serverText)
        }
    }

    private fun populateFieldsIfNeeded(username: String, tagText: EditText, serverText: EditText) {
        val userData = databaseHelper.retrieveDataUser(username, "LolPlayers")

        // Ensure the cursor is not null and contains data
        userData?.use { cursor ->
            if (!databaseHelper.isFirstTimeEntry(username) && cursor.moveToFirst()) {
                val savedTag = cursor.getString(2) // Tag is in the second column
                val savedServer = cursor.getString(4) // Server is in the fourth column

                tagText.setText(savedTag)
                serverText.setText(savedServer)
            }
        }
    }

    private fun handleSaveButtonClick(username: String, tagText: EditText, serverText: EditText) {
        val tagFull = tagText.text.toString()
        if (isValidTagFormat(tagFull)) {
            val server = serverText.text.toString()

            if (!databaseHelper.isFirstTimeEntry(username)) {
                databaseHelper.replaceDataGame(
                    username,
                    "League of Legends",
                    tagFull,
                    "",
                    server,
                    ""
                )
            } else {
                databaseHelper.insertDataGame(
                    username,
                    "League of Legends",
                    tagFull,
                    "",
                    server,
                    ""
                )
            }

            // Navigate to MainActivity after saving the data
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("username", username)
                Log.e("Settings", "Username: $username")
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Invalid format", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidTagFormat(tag: String): Boolean {
        val pattern = "^.+#[^.]{1,5}$".toRegex()
        return tag.matches(pattern)
    }
}
