package com.example.teammatefinder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.teammatefinder.R

class Settings : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        databaseHelper = DatabaseHelper(this)
        val username = intent.getStringExtra("username") ?: ""
        Log.d("Settings", "Username: $username")

        val saveButton: Button = findViewById(R.id.settings_button)
        val tagText: EditText = findViewById(R.id.settings_tag)
        val serverSpinner: Spinner = findViewById(R.id.settings_server)
        val spinner: Spinner = findViewById(R.id.settings_spinner)

        var lolTag = ""
        var valoTag = ""
        var tftTag = ""
        var lolServer = ""
        var valoServer = ""
        var tftServer = ""
        var currentServer = ""

        ArrayAdapter.createFromResource(
            this,
            R.array.game_selection,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        val serverAdapter = ArrayAdapter.createFromResource(this, R.array.servers, android.R.layout.simple_spinner_item)
        serverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        serverSpinner.adapter = serverAdapter

        serverSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentServer =
                    serverSpinner.selectedItem.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
        fun populateFieldsIfNeeded(username: String, tagText: EditText, server: Spinner, table: String) {
            val userData = databaseHelper.retrieveDataUser(username, table)
            userData?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val savedTag = cursor.getString(1)
                    val savedServer = cursor.getString(3)
                    tagText.setText(savedTag)
                    server.setSelection(serverAdapter.getPosition(savedServer))
                }
            }
        }


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedGame = spinner.selectedItem.toString()
                when (selectedGame) {
                    "League of Legends" -> {
                        populateFieldsIfNeeded(username, tagText, serverSpinner, "LolPlayers")
                    }

                    "Valorant" -> {
                        populateFieldsIfNeeded(username, tagText, serverSpinner, "ValorantPlayers")
                    }

                    "TFT" -> {
                        populateFieldsIfNeeded(username, tagText, serverSpinner, "TFTPlayers")
                    }
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
                // Do nothing
            }
        }
        saveButton.setOnClickListener {
            val selectedGame = spinner.selectedItem.toString()
            val currentTagText = tagText.text.toString()
            val currentServer = serverSpinner.selectedItem.toString()

            when (selectedGame) {
                "League of Legends" -> {
                    lolTag = currentTagText
                    lolServer = currentServer
                }

                "Valorant" -> {
                    valoTag = currentTagText
                    valoServer = currentServer
                }

                "TFT" -> {
                    tftTag = currentTagText
                    tftServer = currentServer
                }
            }

            var isDataSaved = false

                if (lolTag.isNotEmpty() && lolServer.isNotEmpty()) {
                    handleSaveButtonClick(username, lolTag, lolServer, "League of Legends")
                    Log.e("Settings", "lolTag: $lolTag, lolServer: $lolServer")
                    isDataSaved = true
                }

                if (valoTag.isNotEmpty() && valoServer.isNotEmpty()) {
                    handleSaveButtonClick(username, valoTag, valoServer, "Valorant")
                    Log.e("Settings", "valoTag: $valoTag, valoServer: $valoServer")
                    isDataSaved = true
                }
                if (tftTag.isNotEmpty() && tftServer.isNotEmpty()) {
                    handleSaveButtonClick(username, tftTag, tftServer, "TFT")
                    Log.e("Settings", "tftTag: $tftTag, tftServer: $tftServer")
                    isDataSaved = true
                }

            if (isDataSaved) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("username", username)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please fill in at least one game tag.", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun handleSaveButtonClick(username: String, tagFull: String, server: String, game: String) {
        if (isValidTagFormat(tagFull)) {
            val isFirstTimeGame = databaseHelper.isFirstTimeEntryTable(username, game)

            val success = if (!isFirstTimeGame) {
                databaseHelper.replaceDataGame(username, game, tagFull, "", server, "") > 0
            } else {
                databaseHelper.insertDataGame(username, game, tagFull, "", server, "") != -1L
            }

            if (success) {
                Log.d("Settings", "$game data saved successfully for $username")
            } else {
                Log.e("Settings", "Failed to save $game data for $username")
                Toast.makeText(this, "Error saving $game data. Please try again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("Settings", "Invalid tag format for $game")
            Toast.makeText(this, "Invalid tag format for $game", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidTagFormat(tag: String): Boolean {
        val pattern = "^.+#[^.]{1,5}$".toRegex()
        return tag.matches(pattern)
    }
}
