package com.example.brickslist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.brickslist.database.DatabaseHelper

class InventoryParts : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_parts)
        val inventoryPartsTitle = findViewById<TextView>(R.id.inventoryPartsTitle)
        inventoryPartsTitle.text = intent.getStringExtra("inventoryName")

        db = DatabaseHelper(this)
        val projectId = intent.getStringExtra("inventoryId").toInt()
        Log.d("PARTS", projectId.toString())
        val inventoryPartsList = db.getPartList(projectId)

        val stringInventoryPartsList: MutableList<String> = arrayListOf()
        for (part in inventoryPartsList) {
            stringInventoryPartsList.add(part.title)
        }
        val listView = findViewById<ListView>(R.id.inventoryPartsListView)
        val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, stringInventoryPartsList)
        listView.adapter = adapter
    }
}
