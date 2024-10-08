package com.example.teammatefinder.ui

import android.content.Intent
import android.database.Cursor
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

data class Player(val username: String, val tag: String, val division: String, val server: String, val winrate: String)


class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private var riotApiKey = "enter api here"
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
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        riotApiUrl = getRiotURL(getServer(username, "League of Legends"))
        Log.d("MainActivity", "Riot API URL: $riotApiUrl")
        riotRegionURL = getRiotRegion(getServer(username, "League of Legends"))
        Log.d("MainActivity", "Riot Region URL: $riotRegionURL")
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
                showPlayers(selectedOption, username, recyclerView)
                true
            }

            // Show the popup menu
            popupMenu.show()
        }
        showPlayers(gameSelection.text.toString(), username, recyclerView)

        gameSelection.setOnClickListener {
            showPopupMenu(gameSelection)
        }
        navigationMenu(username, gameSelection)
    }

    private fun navigationMenu(username: String?, gameSelection: TextView) {
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
                        val intent = Intent(this, Settings::class.java)
                        intent.putExtra("username", username)
                        intent.putExtra("selectedOption", gameSelection.text.toString())
                        startActivity(intent)
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
        val gamerTag = databaseHelper.retrieveDataUser(username, "LolPlayers")?.getString(1)
        Log.d("MainActivity", "GamerTag: $gamerTag")
        val (gameName, gameTag) = getGameTag(gamerTag.toString())
        Log.d("MainActivity", "GameName: $gameName, GameTag: $gameTag")

        riotApiServiceRegion.getAccountByRiotId(gameName, gameTag, riotApiKey).enqueue(object :
            Callback<RiotAccount> {
            override fun onResponse(call: Call<RiotAccount>, response: Response<RiotAccount>) {
                if (!response.isSuccessful) {
                    Log.e("RiotApiService", "Error fetching Riot account: ${response.code()}")
                    return
                } else
                    Log.d("RiotApiService", "Riot Account Response successful!")
                val puuid = response.body()?.puuid ?: return
                getLeagueData(puuid, gamerTag.toString(), username)
            }

            override fun onFailure(call: Call<RiotAccount>, t: Throwable) {
                Log.e("RiotApiService", "Error fetching Riot account", t)
            }
        })
    }

    private fun getLeagueData(puuid: String, gamerTag: String, username: String) {
        val riotApiServiceAPI = Retrofit.Builder()
            .baseUrl(riotApiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RiotApiService::class.java)
        riotApiServiceAPI.getSummonerByPUUID(puuid, riotApiKey)
            .enqueue(object : Callback<Summoner> {
                override fun onResponse(call: Call<Summoner>, response: Response<Summoner>) {
                    if (!response.isSuccessful) {
                        Log.e("RiotApiService", "Error fetching summoner: ${response.code()}")
                        return
                    } else
                        Log.d("RiotApiService", "League Data Response successful!")
                    val summoner = response.body() ?: return
                    val encryptedSummonerId = summoner.id
                    riotApiServiceAPI.getLeagueEntriesBySummonerId(encryptedSummonerId, riotApiKey)
                        .enqueue(object : Callback<List<LeagueEntry>> {
                            override fun onResponse(
                                call: Call<List<LeagueEntry>>,
                                response: Response<List<LeagueEntry>>
                            ) {
                                val leagueEntries = response.body() ?: return
                                for (leagueEntry in leagueEntries) {
                                    if (leagueEntry.queueType == "RANKED_SOLO_5x5") {
                                        val serverCursor =
                                            databaseHelper.retrieveDataUser(username, "LolPlayers")
                                        val server = serverCursor?.use {
                                            it.getString(it.getColumnIndexOrThrow("Server"))
                                        } ?: ""
                                        val winrate =
                                            (leagueEntry.wins / (leagueEntry.wins + leagueEntry.losses) * 100).toDouble().toString()
                                        databaseHelper.replaceDataGame(
                                            username,
                                            "League of Legends",
                                            gamerTag,
                                            leagueEntry.tier + " " + leagueEntry.rank,
                                            server,
                                            winrate
                                        )
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

    private fun getServer(username: String, game: String): String {
        val cursor: Cursor
        when (game) {
            "League of Legends" -> {
                cursor = databaseHelper.retrieveDataUser(username, "LolPlayers")!!
                cursor.use {
                    if (it.moveToFirst()) {
                        return it.getString(3).toString()
                    }
                }
            }

            "Valorant" -> {
                cursor = databaseHelper.retrieveDataUser(username, "ValorantPlayers")!!
                cursor.use {
                    if (it.moveToFirst()) {
                        return it.getString(3).toString()
                    }
                }
            }

            "TFT" -> {
                cursor = databaseHelper.retrieveDataUser(username, "TFTPlayers")!!
                cursor.use {
                    if (it.moveToFirst()) {
                        return it.getString(3).toString()
                    }
                }
            }

            else -> throw IllegalArgumentException("Unsupported game type")
        }
        return "N/A"
    }
    private fun getRiotURL(region: String): String {
        when(region){
            "BR" -> return "https://br1.api.riotgames.com"
            "EUNE" -> return "https://eun1.api.riotgames.com"
            "EUW" -> return "https://euw1.api.riotgames.com"
            "JP" -> return "https://jp1.api.riotgames.com"
            "KR" -> return "https://kr.api.riotgames.com"
            "LAN" -> return "https://la1.api.riotgames.com"
            "LAS" -> return "https://la2.api.riotgames.com"
            "NA" -> return "https://na1.api.riotgames.com"
            "OCE" -> return "https://oc1.api.riotgames.com"
            "RU" -> return "https://ru.api.riotgames.com"
            "TR" -> return "https://tr1.api.riotgames.com"
            "PH" -> return "https://ph2.api.riotgames.com"
            "SG" -> return "https://sg2.api.riotgames.com"
            "TH" -> return "https://th2.api.riotgames.com"
            "TW" -> return "https://tw2.api.riotgames.com"
            "VN" -> return "https://vn2.api.riotgames.com"
            else -> return "https://eun1.api.riotgames.com"
        }
    }
    private fun getRiotRegion(region: String): String {
        when(region){
            "BR" -> return "https://americas.api.riotgames.com"
            "EUNE" -> return "https://europe.api.riotgames.com"
            "EUW" -> return "https://europe.api.riotgames.com"
            "JP" -> return "https://asia.api.riotgames.com"
            "KR" -> return "https://asia.api.riotgames.com"
            "LAN" -> return "https://americas.api.riotgames.com"
            "LAS" -> return "https://americas.api.riotgames.com"
            "NA" -> return "https://americas.api.riotgames.com"
            "OCE" -> return "https://asia.api.riotgames.com"
            "RU" -> return "https://europe.api.riotgames.com"
            "TR" -> return "https://europe.api.riotgames.com"
            "PH" -> return "https://sea.api.riotgames.com"
            "SG" -> return "https://sea.api.riotgames.com"
            "TH" -> return "https://sea.api.riotgames.com"
            "TW" -> return "https://sea.api.riotgames.com"
            "VN" -> return "https://sea.api.riotgames.com"
            else -> return "https://europe.api.riotgames.com"
        }
    }

    private fun getGameTag(tag: String): Pair<String, String> {
        val gameName = tag.substringBefore("#")
        val gameTag = tag.substringAfter("#")
        return Pair(gameName, gameTag)
    }
    private fun showPlayers(selectedOption: String, username: String, recyclerView: RecyclerView){
        when (selectedOption) {
            "League of Legends" -> {
                val playerList = databaseHelper.getAllPlayers("LolPlayers")

                recyclerView.layoutManager = LinearLayoutManager(this)

                val adapter = DataAdapter(
                    playerList,
                    username, onItemClicked = { clickedPlayer ->
                        run {
                            Log.d("ClickedPlayer", clickedPlayer.toString())

                            val intent = Intent(this, Profile::class.java)
                            intent.putExtra("username", clickedPlayer.username)
                            intent.putExtra("selectedOption", selectedOption)
                            startActivity(intent)
                        }
                    }, "League of Legends"
                )

                recyclerView.adapter = adapter
            }

            "Valorant" -> {
                val playerList = databaseHelper.getAllPlayers("ValorantPlayers")


                recyclerView.layoutManager = LinearLayoutManager(this)

                val adapter = DataAdapter(
                    playerList,
                    username, onItemClicked = {
                            clickedPlayer ->
                        run {

                            Log.d("ClickedPlayer", clickedPlayer.toString())

                            val intent = Intent(this, Profile::class.java)
                            intent.putExtra("username", clickedPlayer.username)
                            intent.putExtra("selectedOption", selectedOption)
                            startActivity(intent)
                        }

                    }, "Valorant"
                )
                recyclerView.adapter = adapter
            }

            "TFT" -> {
                val playerList = databaseHelper.getAllPlayers("TFTPlayers")


                recyclerView.layoutManager = LinearLayoutManager(this)

                val adapter = DataAdapter(
                    playerList,
                    username, onItemClicked = { clickedPlayer ->
                        run {

                            Log.d("ClickedPlayer", clickedPlayer.toString())

                            val intent = Intent(this, Profile::class.java)
                            intent.putExtra("username", clickedPlayer.username)
                            intent.putExtra("selectedOption", selectedOption)
                            startActivity(intent)
                        }
                    }, "TFT"
                )
                recyclerView.adapter = adapter
            }
        }
    }

}