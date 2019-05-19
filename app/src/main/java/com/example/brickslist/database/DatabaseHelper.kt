package com.example.brickslist.database

import android.annotation.SuppressLint
import com.example.brickslist.model.Inventory
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {
    companion object {
        private val DB_NAME = "BrickList.db"
    }

    private var db: SQLiteDatabase? = null

    fun openDatabase(): SQLiteDatabase {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            try {
                val checkDB = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE,null)
                checkDB?.close()
                copyDatabase(dbFile)
            } catch (e: IOException) {
                throw RuntimeException("Error creating source database", e)
            }
        }
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
    }

    @SuppressLint("WrongConstant")
    private fun copyDatabase(dbFile: File) {
        val `is` = context.assets.open(DB_NAME)
        val os = FileOutputStream(dbFile)

        val buffer = ByteArray(1024)
        while (`is`.read(buffer) > 0) {
            os.write(buffer)
            Log.d("#DB", "writing>>")
        }

        os.flush()
        os.close()
        `is`.close()
        Log.d("#DB", "completed..")
    }

    fun getInventoryList(): ArrayList<Inventory> {

        val inventoryList = ArrayList<Inventory>()

        try {
            this.openDatabase()

            var cursor = this.readableDatabase.query("Inventories" , arrayOf("id, Name, Active, LastAccessed"), null, null, null, null, "LastAccessed DESC")

            if (cursor.moveToFirst()) {
                do {
                    var id = cursor.getInt(cursor.getColumnIndex("id"))
                    var name = cursor.getString(cursor.getColumnIndex("Name"))
                    var active = cursor.getInt(cursor.getColumnIndex("Active"))
                    var lastAccessed = cursor.getInt(cursor.getColumnIndex("LastAccessed"))

                    inventoryList.add(Inventory(id, name, active, lastAccessed))
                } while (cursor.moveToNext())
            }
            cursor.close()
            this.close()
        } catch (e: SQLiteException) {

        }
        return inventoryList
    }

    fun createProject(projectCode: String): Int {
        this.openDatabase()

        val values = ContentValues()
        values.put("Name", "Projekt $projectCode")
        values.put("Active", 1)
        values.put("LastAccessed", Calendar.getInstance().timeInMillis.toInt())

        val id = this.writableDatabase.insert("Inventories", null, values)
        this.close()
        return id.toInt()
    }

    override fun onCreate(db: SQLiteDatabase?) {    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {    }
}