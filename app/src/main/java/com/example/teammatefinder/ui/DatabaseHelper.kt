package com.example.teammatefinder.ui

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME_USERS = "Users"
        private const val TABLE_NAME_LOL = "LolPlayers"
        private const val TABLE_NAME_VALORANT = "ValorantPlayers"
        private const val TABLE_NAME_TFT = "TFTPlayers"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_SERVER = "Server"
        private const val COLUMN_TAG = "Tag"
        private const val COLUMN_DIVISION = "Division"
        private const val COLUMN_WINRATE = "Winrate"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTableQuery =
            ("CREATE TABLE $TABLE_NAME_USERS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USERNAME TEXT, $COLUMN_PASSWORD TEXT)")
        val createLolTableQuery =
            ("CREATE TABLE $TABLE_NAME_LOL ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USERNAME TEXT, $COLUMN_TAG TEXT, $COLUMN_DIVISION TEXT, $COLUMN_SERVER TEXT, $COLUMN_WINRATE DOUBLE)")
        val createValorantTableQuery =
            ("CREATE TABLE $TABLE_NAME_VALORANT ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USERNAME TEXT, $COLUMN_TAG TEXT, $COLUMN_DIVISION TEXT, $COLUMN_SERVER TEXT, $COLUMN_WINRATE DOUBLE)")
        val createTFTTableQuery =
            ("CREATE TABLE $TABLE_NAME_TFT ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USERNAME TEXT, $COLUMN_TAG TEXT, $COLUMN_DIVISION TEXT, $COLUMN_SERVER TEXT, $COLUMN_WINRATE DOUBLE)")
        db?.execSQL(createUsersTableQuery)
        db?.execSQL(createLolTableQuery)
        db?.execSQL(createValorantTableQuery)
        db?.execSQL(createTFTTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_LOL")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_VALORANT")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_TFT")
        onCreate(db)
    }

    fun insertDataUsers(
        username: String,
        password: String,
    ): Long {
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        return writableDatabase.insert(TABLE_NAME_USERS, null, values)
    }

    fun replaceDataGame(
        username: String,
        game: String,
        tag: String,
        division: String,
        server: String,
        winrate: String
    ): Int {
        val values = ContentValues().apply {
            put(COLUMN_TAG, tag)
            put(COLUMN_DIVISION, division)
            put(COLUMN_SERVER, server)
            put(COLUMN_WINRATE, winrate)
        }

        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)

        return when (game) {
            "League of Legends" -> writableDatabase.update(TABLE_NAME_LOL, values, selection, selectionArgs)
            "Valorant" -> writableDatabase.update(TABLE_NAME_VALORANT, values, selection, selectionArgs)
            "TFT" -> writableDatabase.update(TABLE_NAME_TFT, values, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unsupported game type")
        }
    }
    fun insertDataGame(
        username: String,
        game: String,
        tag: String,
        division: String,
        server: String,
        winrate: String
    ): Long {
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_TAG, tag)
            put(COLUMN_DIVISION, division)
            put(COLUMN_SERVER, server)
            put(COLUMN_WINRATE, winrate)
        }
        return when (game) {
            "League of Legends" -> writableDatabase.insert(TABLE_NAME_LOL,null, values)
            "Valorant" -> writableDatabase.insert(TABLE_NAME_VALORANT, null, values)
            "TFT" -> writableDatabase.insert(TABLE_NAME_TFT, null, values)
            else -> throw IllegalArgumentException("Unsupported game type")
        }
    }

    fun retrieveDataUser(username: String, tableName: String): Cursor? {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)

        // Define the projection (columns) based on the tableName
        val projection: Array<String> = when (tableName) {
            TABLE_NAME_LOL, TABLE_NAME_VALORANT, TABLE_NAME_TFT -> {
                arrayOf(COLUMN_USERNAME, COLUMN_TAG, COLUMN_DIVISION, COLUMN_SERVER, COLUMN_WINRATE)
            }
            else -> {
                arrayOf(COLUMN_USERNAME)
            }
        }

        // Use a try-finally block to ensure proper resource handling
        var cursor: Cursor? = null
        return try {
            cursor = db.query(tableName, projection, selection, selectionArgs, null, null, null)

            if (cursor.moveToFirst()) {
                cursor // Return the cursor if data is found
            } else {
                Log.e("Database", "No data found for user: $username in table: $tableName")
                cursor?.close() // Close the cursor if no data is found
                null
            }
        } catch (e: Exception) {
            Log.e("Database", "Error retrieving data for user: $username in table: $tableName", e)
            cursor?.close() // Ensure the cursor is closed in case of an error
            null
        }
    }




    fun getAllPlayers(tableName: String): List<Player> {
        val playerList = mutableListOf<Player>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT tag, division, winrate, server FROM $tableName", null)

        if (cursor.moveToFirst()) {
            do {
                val tag = cursor.getString(cursor.getColumnIndexOrThrow("tag"))
                val division = cursor.getString(cursor.getColumnIndexOrThrow("division"))
                val winrate = cursor.getString(cursor.getColumnIndexOrThrow("winrate"))
                val server = cursor.getDouble(cursor.getColumnIndexOrThrow("server"))
                val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                playerList.add(Player(username, tag, division, winrate, server))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return playerList
    }

    fun readUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(username, password)
        val cursor = db.query(TABLE_NAME_USERS, null, selection, selectionArgs, null, null, null)
        val userExists = cursor.count > 0
        cursor.close()
        return userExists
    }

    fun isFirstTimeEntry(username: String): Boolean {
        return isFirstTimeEntryTable(username, "League of Legends") &&
                isFirstTimeEntryTable(username, "Valorant") &&
                isFirstTimeEntryTable(username, "TFT")
    }

    fun isFirstTimeEntryTable(username: String, game: String): Boolean {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)

        val table = when (game) {
            "League of Legends" -> TABLE_NAME_LOL
            "Valorant" -> TABLE_NAME_VALORANT
            "TFT" -> TABLE_NAME_TFT
            else -> null
        }

        table?.let {
            val cursor = db.query(it, null, selection, selectionArgs, null, null, null)
            val userExists = cursor.count > 0
            cursor.close()
            return !userExists
        }
        return false
    }


}
