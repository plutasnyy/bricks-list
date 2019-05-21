package com.example.brickslist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_inventory_parts.*

class InventoryParts : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_parts)
        val inventoryPartsTitle = findViewById<TextView>(R.id.inventoryPartsTitle)
        inventoryPartsTitle.text = intent.getStringExtra("inventoryName")
    }
}
