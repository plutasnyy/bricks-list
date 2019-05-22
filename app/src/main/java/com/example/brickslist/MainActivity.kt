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
import com.example.brickslist.model.InventoryPart
import com.example.brickslist.model.XMLInventory
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.android.synthetic.main.activity_main.*
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
        XMLHandler().execute().get()
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
    inner class XMLHandler : AsyncTask<String, String, String>() {

        private val kotlinXmlMapper = XmlMapper(JacksonXmlModule().apply {
            setDefaultUseWrapper(false)
        }).registerKotlinModule()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)


        private inline fun <reified T : Any> parseAs(doc: String): T {
            return kotlinXmlMapper.readValue(doc)
        }

        override fun doInBackground(vararg params: String?): String? {
            try {
                val document = URL(getUrl).readText()
                val inventory = parseAs<XMLInventory>(document)
                for (item in inventory.items){
                    Log.d("ADD_", item.itemID)
                }
            } catch (e: Exception) {
                Log.d("ADD_", e.message.toString())
            }
            return ""
        }
    }
}