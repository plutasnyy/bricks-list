package com.example.brickslist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.brickslist.database.DatabaseHelper

class MainActivity : AppCompatActivity() {
    lateinit var db: DatabaseHelper
    var Url = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)
        createInventoryListView()
    }

    private fun createInventoryListView() {
        val inventoryLists = db.getInventoryList()

        val inventoryList: MutableList<String> = arrayListOf()
        for (inv in inventoryLists) {
            inventoryList.add(inv.name)
        }
        val listView = findViewById<ListView>(R.id.inventoryListView)
        val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, inventoryList)
        listView.adapter = adapter
    }
}
