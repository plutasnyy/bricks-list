package com.example.brickslist

import android.os.Build.ID
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.example.brickslist.database.DatabaseHelper

class MainActivity : AppCompatActivity() {
    lateinit var db : DatabaseHelper
    lateinit var listView: ListView
    var Url = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)
        db.createProject(22.toString())

        val inventoryLists = db.getInventoryList()
        for(inv in inventoryLists){
            Log.d("#DB", inv.name)
        }
        Log.d("DB", inventoryLists.toString())
    }
}
