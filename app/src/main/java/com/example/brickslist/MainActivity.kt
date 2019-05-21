package com.example.brickslist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.brickslist.database.DatabaseHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var getUrl: String
    lateinit var db: DatabaseHelper
    var Url = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)
        createInventoryListView()
        newInventoryAction()
    }

    private fun newInventoryAction() {
        addInventoryButton.setOnClickListener {
            getUrl = Url + 70403 + ".xml"
            createNewInventory()
        }
    }

    private fun createNewInventory() {
        var str = HandlerXML().execute().get()
        Log.d("ADD_", str)

    }

    private fun createInventoryListView() {
        val inventoryLists = db.getInventoryList()

        val stringInventoryList: MutableList<String> = arrayListOf()
        for (inv in inventoryLists) {
            stringInventoryList.add(inv.name)
        }
        val listView = findViewById<ListView>(R.id.inventoryListView)
        val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, stringInventoryList)
        listView.adapter = adapter

        inventoryListView.setOnItemClickListener { _, _, position, _ ->
            val inventory = inventoryLists[position]
            val intent = Intent(this, InventoryParts::class.java)
            intent.putExtra("inventoryId", inventory.id)
            intent.putExtra("inventoryName", inventory.name)
            startActivity(intent)
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class HandlerXML : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String?): String {
            return try {
                URL(getUrl).readText()
            } catch (e: Exception) {
                e.message.toString()
            }
        }

    }
}