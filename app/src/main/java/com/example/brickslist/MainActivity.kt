package com.example.brickslist

import android.os.Build.ID
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.example.brickslist.database.DatabaseHelper

class MainActivity : AppCompatActivity() {
    lateinit var db_ : DatabaseHelper
    lateinit var listView: ListView
    var Url = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db_ = DatabaseHelper(this)
        val db = db_.openDatabase()

        val selectQuery = "SELECT  * FROM Colors"
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor != null) {
            cursor.moveToFirst()
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex("Name"))
                Log.d("#DB", id.toString())
            }
        }
        cursor.close()

//        var inventoryLists = db.getInventoryList()
//        Log.d("DB", inventoryLists.toString())
    }
}
