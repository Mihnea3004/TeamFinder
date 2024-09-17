package com.example.teammatefinder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teammatefinder.R
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class Player(val username: String, val tag: String, val division: String, val server: String, val winrate: Double)


class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private var riotApiKey = "RGAPI-9427d3eb-44da-48d6-92c6-eb59884ddce5"
    private var riotApiUrl = "https://eun1.api.riotgames.com/"
    private var riotRegionURL = "https://europe.api.riotgames.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        databaseHelper = DatabaseHelper(this)
        val options = listOf("League of Legends", "Valorant", "TFT")
        var selectedItem: String?
        val username = intent.getStringExtra("username").toString()
        val gameSelection = findViewById<TextView>(R.id.game_selection)
        val recyclerView : RecyclerView = findViewById(R.id.recyclerView)
        fetchRiotAccount()

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

                when (selectedOption) {
                    "League of Legends" ->{
                        val playerList = databaseHelper.getAllPlayers("LolPlayers")

                        recyclerView.layoutManager = LinearLayoutManager(this)

                        val adapter = DataAdapter(playerList,
                            username, onItemClicked = { player -> {} },"League of Legends")
                        recyclerView.adapter = adapter
                    }
                    "Valorant" ->{
                        val playerList = databaseHelper.getAllPlayers("ValorantPlayers")


                        recyclerView.layoutManager = LinearLayoutManager(this)

                        val adapter = DataAdapter(playerList,
                            username, onItemClicked = { player ->  {} },"Valorant")
                        recyclerView.adapter = adapter
                    }
                    "TFT" ->{
                        val playerList = databaseHelper.getAllPlayers("TFTPlayers")


                        recyclerView.layoutManager = LinearLayoutManager(this)

                        val adapter = DataAdapter(playerList,
                            username, onItemClicked = { player -> {} },"TFT")
                        recyclerView.adapter = adapter
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
        navigationMenu(username,gameSelection)
    }

    private fun navigationMenu(username:String?, gameSelection: TextView){
        val navView: NavigationView = findViewById(R.id.nav_view)
        val drawerLayout: androidx.drawerlayout.widget.DrawerLayout = findViewById(R.id.main)
        val navOpenButton = findViewById<ImageButton>(R.id.open_nav)
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
                        val intent = Intent(this, Profile::class.java)
                        intent.putExtra("username", username)
                        intent.putExtra("selectedOption", gameSelection.text.toString())
                        startActivity(intent)
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

    private fun fetchRiotAccount() {
        val username = intent.getStringExtra("username").toString()
        Log.d("MainActivity", "Username: $username")
        val riotApiServiceRegion = Retrofit.Builder()
            .baseUrl(riotRegionURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RiotApiService::class.java)

        val riotApiServiceAPI = Retrofit.Builder()
            .baseUrl(riotApiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RiotApiService::class.java)
        val gamerTag = databaseHelper.retrieveDataUser(username,"LolPlayers")?.getString(1)
        Log.d("MainActivity", "GamerTag: $gamerTag")
        val (gameName, gameTag) = getGameTag(gamerTag.toString())
        Log.d("MainActivity", "GameName: $gameName, GameTag: $gameTag")

        riotApiServiceRegion.getAccountByRiotId(gameName, gameTag, riotApiKey).enqueue(object :
            Callback<RiotAccount> {
            override fun onResponse(call: Call<RiotAccount>, response: Response<RiotAccount>) {
                val puuid = response.body()?.puuid ?: return
                riotApiServiceAPI.getSummonerByPUUID(puuid, riotApiKey).enqueue(object : Callback<Summoner> {
                    override fun onResponse(call: Call<Summoner>, response: Response<Summoner>) {
                        val summoner = response.body() ?: return
                        val encryptedSummonerId = summoner.id
                        riotApiServiceAPI.getLeagueEntriesBySummonerId(encryptedSummonerId, riotApiKey)
                            .enqueue(object : Callback<List<LeagueEntry>> {
                                override fun onResponse(call: Call<List<LeagueEntry>>, response: Response<List<LeagueEntry>>) {
                                    val leagueEntries = response.body() ?: return
                                    for (leagueEntry in leagueEntries) {
                                        if (leagueEntry.queueType == "RANKED_SOLO_5x5") {
                                            val serverCursor = databaseHelper.retrieveDataUser(username, "LolPlayers")
                                            val server = serverCursor?.use {
                                                it.getString(it.getColumnIndexOrThrow("Server"))
                                            } ?: ""
                                            val winrate = (leagueEntry.wins.toDouble() / (leagueEntry.wins + leagueEntry.losses).toDouble() * 100).toString()
                                            databaseHelper.replaceDataGame(username, "League of Legends",
                                                gamerTag.toString(), leagueEntry.tier + " " + leagueEntry.rank, server, winrate)
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<List<LeagueEntry>>, t: Throwable) {
                                    Log.e("RiotApiService", "Error fetching league entries", t)
                                }
                            })
                    }

                    override fun onFailure(call: Call<Summoner>, t: Throwable) {
                        Log.e("RiotApiService", "Error fetching summoner", t)
                    }
                })
            }

            override fun onFailure(call: Call<RiotAccount>, t: Throwable) {
                Log.e("RiotApiService", "Error fetching Riot account", t)
            }
        })
    }

    private fun getGameTag(tag: String): Pair<String, String> {
        val gameName = tag.substringBefore("#")
        val gameTag = tag.substringAfter("#")
        return Pair(gameName, gameTag)
    }
}