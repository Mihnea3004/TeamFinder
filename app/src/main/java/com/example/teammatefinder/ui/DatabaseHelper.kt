package com.example.teammatefinder.ui

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


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
        private const val COLUMN_LOL_CHECK = "IsPlayingLol"
        private const val COLUMN_VALORANT_CHECK = "IsPlayingValorant"
        private const val COLUMN_TFT_CHECK = "IsPlayingTft"
        private const val COLUMN_SERVER = "Server"
        private const val COLUMN_TAG = "Tag"
        private const val COLUMN_DIVISION = "Division"
        private const val COLUMN_WINRATE = "Winrate"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTableQuery = ("CREATE TABLE $TABLE_NAME_USERS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USERNAME TEXT, $COLUMN_PASSWORD TEXT, $COLUMN_LOL_CHECK INTEGER, $COLUMN_VALORANT_CHECK INTEGER, $COLUMN_TFT_CHECK INTEGER)")
        val createLolTableQuery = ("CREATE TABLE $TABLE_NAME_LOL ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USERNAME TEXT, $COLUMN_TAG TEXT, $COLUMN_DIVISION TEXT, $COLUMN_SERVER TEXT, $COLUMN_WINRATE DOUBLE)")
        val createValorantTableQuery = ("CREATE TABLE $TABLE_NAME_VALORANT ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USERNAME TEXT, $COLUMN_TAG TEXT, $COLUMN_DIVISION TEXT, $COLUMN_SERVER TEXT, $COLUMN_WINRATE DOUBLE)")
        val createTFTTableQuery = ("CREATE TABLE $TABLE_NAME_TFT ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USERNAME TEXT, $COLUMN_TAG TEXT, $COLUMN_DIVISION TEXT, $COLUMN_SERVER TEXT, $COLUMN_WINRATE DOUBLE)")
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

    fun insertDataUsers(username: String, password: String, lol: Boolean, valorant: Boolean, tft: Boolean): Long {
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_LOL_CHECK, if (lol) 1 else 0)
            put(COLUMN_VALORANT_CHECK, if (valorant) 1 else 0)
            put(COLUMN_TFT_CHECK, if (tft) 1 else 0)
        }
        return writableDatabase.insert(TABLE_NAME_USERS, null, values)
    }

    fun insertDataGame(game: String, username: String, tag: String, division: String, server: String, winrate: Double): Long {
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_TAG, tag)
            put(COLUMN_DIVISION, division)
            put(COLUMN_SERVER, server)
            put(COLUMN_WINRATE, winrate)
        }
        return when (game) {
            "League of Legends" -> writableDatabase.insert(TABLE_NAME_LOL, null, values)
            "Valorant" -> writableDatabase.insert(TABLE_NAME_VALORANT, null, values)
            "TFT" -> writableDatabase.insert(TABLE_NAME_TFT, null, values)
            else -> throw IllegalArgumentException("Unsupported game type")
        }
    }

    fun retrieveDataUser(username: String, tableName: String): Cursor {
        val db = readableDatabase
        val projection = arrayOf(COLUMN_LOL_CHECK, COLUMN_VALORANT_CHECK, COLUMN_TFT_CHECK)
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        return db.query(tableName, projection, selection, selectionArgs, null, null, null)
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
}
