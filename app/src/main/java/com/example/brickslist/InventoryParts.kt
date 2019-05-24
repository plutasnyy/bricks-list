package com.example.brickslist

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import android.widget.TextView
import com.example.brickslist.database.DatabaseHelper
import com.example.brickslist.model.InventoryPart
import kotlinx.android.synthetic.main.activity_inventory_parts.*
import org.w3c.dom.Document
import java.io.File
import java.util.ArrayList
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class InventoryParts : AppCompatActivity() {

    private var projectId: Int = 0
    private lateinit var partsList: ArrayList<InventoryPart>
    private lateinit var db: DatabaseHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_parts)
        val inventoryPartsTitle = findViewById<TextView>(R.id.inventoryPartsTitle)
        inventoryPartsTitle.text = intent.getStringExtra("inventoryName")

        db = DatabaseHelper(this)
        projectId = intent.getStringExtra("inventoryId").toInt()
        Log.d("PARTS", projectId.toString())

        partsList = db.getPartList(projectId)
        val adapter = InventoryPartsListAdapter(partsList, this)
        inventoryPartsListView.adapter = adapter

        csvButton.setOnClickListener {
            writeXml()
        }

        archiveButton.setOnClickListener {
            val suc: Int = db.changeActiveInventory(projectId, 0).toInt()
            if (suc != -1) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun writeXml() {
        val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = docBuilder.newDocument()

        val rootElement = doc.createElement("INVENTORY")

        for (item in partsList) {
            val difference = item.quantityInSet - item.quantityInStore
            if (difference > 0) {
                val outputItemXML = doc.createElement("ITEM")

                val itemType = doc.createElement("ITEMTYPE")
                val itemID = doc.createElement("ITEMID")
                val color = doc.createElement("COLOR")
                val QTYFILLED = doc.createElement("QTYFILLED")

                itemType.appendChild(doc.createTextNode(item.typeID.toString()))
                itemID.appendChild(doc.createTextNode(item.itemID.toString()))
                color.appendChild(doc.createTextNode(item.colorID.toString()))
                QTYFILLED.appendChild(doc.createTextNode((difference).toString()))

                outputItemXML.appendChild(itemType)
                outputItemXML.appendChild(itemID)
                outputItemXML.appendChild(color)
                outputItemXML.appendChild(QTYFILLED)

                rootElement.appendChild(outputItemXML)
            }
        }

        doc.appendChild(rootElement)

        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        val outDir = File("data/data/com.example.brickslist")
        Log.d("PATH_", outDir.path)
        outDir.createNewFile()
        val file = File(outDir, "text.xml")
        transformer.transform(DOMSource(doc), StreamResult(file))
    }
}
