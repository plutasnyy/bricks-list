package com.example.brickslist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import com.example.brickslist.database.DatabaseHelper
import com.example.brickslist.model.XMLInventory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var getUrl: String
    lateinit var db: DatabaseHelper
    var Url = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"
    lateinit var projectCode : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)
        createInventoryListView()
        newInventoryAction()
    }

    private fun createInventoryListView() {
        val inventoryLists = db.getInventoryList()

        val stringInventoryList: MutableList<String> = arrayListOf()
        for (inv in inventoryLists) {
            stringInventoryList.add(inv.name)
        }
        val listView = findViewById<ListView>(R.id.inventoryListView)
        val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, stringInventoryList)
        listView.adapter = adapter as ListAdapter?

        inventoryListView.setOnItemClickListener { _, _, position, _ ->
            val inventory = inventoryLists[position]
            val intent = Intent(this, InventoryParts::class.java)
            intent.putExtra("inventoryName", inventory.name)
            intent.putExtra("inventoryId", inventory.id.toString())
            startActivity(intent)
        }
    }


    private fun newInventoryAction() {
        addInventoryButton.setOnClickListener {
            projectCode = "70403"
            getUrl = "$Url$projectCode.xml"
            XMLHandler().execute().get()
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
                val id = db.createProject(projectCode)
                db.addPartsInventory(inventory.items, id)
                finish()
                startActivity(intent)

            } catch (e: Exception) {
                Log.d("ADD_", e.message.toString())
                toast("Project wasn't downloaded from URL")
            }
            return ""
        }
    }
}