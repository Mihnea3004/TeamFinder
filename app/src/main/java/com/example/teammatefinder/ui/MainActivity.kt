package com.example.teammatefinder.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.teammatefinder.R
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val drawerLayout: androidx.drawerlayout.widget.DrawerLayout = findViewById(R.id.main)
        val navOpenButton = findViewById<ImageButton>(R.id.open_nav)
        val dynamicLayout = findViewById<LinearLayout>(R.id.dynamicLayout)
        val options = listOf("League of Legends", "Valorant", "TFT")
        var selectedItem: String? = null
        fun inflateLayout(layoutResId: Int) {
            // Clear the dynamic content layout first
            dynamicLayout.removeAllViews()

            // Inflate the new layout
            val inflater = LayoutInflater.from(this)
            val newView = inflater.inflate(layoutResId, dynamicLayout, false)

            // Add the inflated view to the dynamic content layout
            dynamicLayout.addView(newView)
        }
        val gameSelection = findViewById<TextView>(R.id.game_selection)
        fun showPopupMenu(view: View) {
            val popupMenu = PopupMenu(this, view)
            selectedItem = gameSelection.text.toString()
            val filteredOptions = options.filter { it != selectedItem }

            filteredOptions.forEachIndexed { index, option ->
                popupMenu.menu.add(0, index, 0, option)
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                val selectedOption = filteredOptions[menuItem.itemId]
                selectedItem = selectedOption

                gameSelection.text = selectedOption

                // Change activity based on the selected option
                when (selectedOption) {
                    "League of Legends" ->{
                        Toast.makeText(this, "League of Legends clicked", Toast.LENGTH_SHORT).show()
                    }
                    "Valorant" ->{

                    }
                    "TFT" ->{

                    }
                }

                true
            }

            // Show the popup menu
            popupMenu.show()
        }

        gameSelection.setOnClickListener{
            showPopupMenu(gameSelection)
        }
        navOpenButton.setOnClickListener {
            drawerLayout.openDrawer(navView)
            val navCloseButton = findViewById<ImageButton>(R.id.back_nav)
            navCloseButton.setOnClickListener {
                drawerLayout.closeDrawer(navView)
            }
            navView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.home_nav -> {
                        Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.profile_nav -> {
                        Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.settings_nav -> {
                        Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.friends_nav -> {
                        Toast.makeText(this, "Friends clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.messages_nav -> {
                        Toast.makeText(this, "Messages clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }

        }

    }

}