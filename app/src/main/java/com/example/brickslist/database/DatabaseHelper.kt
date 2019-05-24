package com.example.brickslist.database

import android.annotation.SuppressLint
import com.example.brickslist.model.Inventory
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.brickslist.model.InventoryPart
import com.example.brickslist.model.InventoryPartXML
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

//TODO Refactor this classs
class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {
    companion object {
        private val DB_NAME = "BrickList.db"
    }

    private fun openDatabase(): SQLiteDatabase {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            try {
                val checkDB = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null)
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

            val cursor = this.readableDatabase.query(
                "Inventories",
                arrayOf("id, Name, Active, LastAccessed"),
                null,
                null,
                null,
                null,
                "LastAccessed DESC"
            )

            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex("id"))
                    val name = cursor.getString(cursor.getColumnIndex("Name"))
                    val active = cursor.getInt(cursor.getColumnIndex("Active"))
                    val lastAccessed = cursor.getInt(cursor.getColumnIndex("LastAccessed"))

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

    private fun getImage(itemId: Int, colorId: Int): Bitmap? {

        this.openDatabase()

        val idCursor =
            this.readableDatabase.query("Parts", arrayOf("id"), "Code = $itemId", null, null, null, null)
        var id = 0
        if (idCursor.moveToFirst()) {
            id = idCursor.getInt(idCursor.getColumnIndex("id"))
        }

        val colorCursor =
            this.readableDatabase.query("Colors", arrayOf("id"), "Code = $colorId", null, null, null, null)
        var color = 0
        if (colorCursor.moveToFirst()) {
            color = colorCursor.getInt(colorCursor.getColumnIndex("id"))
        }

        val cursor = this.readableDatabase.query(
            "Codes",
            arrayOf("Image"),
            "ItemID = $id and ColorID = $color",
            null,
            null,
            null,
            null
        )

        if (cursor.count > 0) {
            cursor.moveToFirst()
            if (cursor.getBlob(cursor.getColumnIndex("Image")) != null) {
                val image = cursor.getBlob(cursor.getColumnIndex("Image"))
                val bmp = BitmapFactory.decodeByteArray(image, 0, image.size)
                return Bitmap.createScaledBitmap(bmp, 250, 250, false)
            }
        }
        this.close()
        cursor.close()
        idCursor.close()
        colorCursor.close()
        return null
    }

    fun getPartList(code: Int): ArrayList<InventoryPart> {
        val partList = ArrayList<InventoryPart>()

        try {
            this.openDatabase()

            val cursor = this.readableDatabase.query(
                "InventoriesParts",
                arrayOf("id, InventoryID, TypeID, ItemID, QuantityInSet, QuantityInStore, ColorID, Extra"),
                "InventoryID = $code",
                null,
                null,
                null,
                "QuantityInStore"
            )

            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex("id"))
                    val inventoryId = cursor.getInt(cursor.getColumnIndex("InventoryID"))
                    val typeId = cursor.getInt(cursor.getColumnIndex("TypeID"))
                    val itemId = cursor.getInt(cursor.getColumnIndex("ItemID"))
                    val quantityInSet = cursor.getInt(cursor.getColumnIndex("QuantityInSet"))
                    val quantityInStore = cursor.getInt(cursor.getColumnIndex("QuantityInStore"))
                    val colorId = cursor.getInt(cursor.getColumnIndex("ColorID"))
                    val extra = cursor.getInt(cursor.getColumnIndex("Extra"))

                    val inventoryPart = InventoryPart(
                        id,
                        inventoryId,
                        typeId,
                        itemId,
                        quantityInSet,
                        quantityInStore,
                        colorId,
                        extra,
                        this.getTitle(itemId, colorId),
                        this.getImage(itemId, colorId)
                    )
                    partList.add(inventoryPart)
                } while (cursor.moveToNext())
            }
            cursor.close()
            this.close()
        } catch (e: SQLiteException) {
            Log.i("SQLERR", e.toString())
        }
        return partList
    }

    fun addPartsInventory(items: List<InventoryPartXML>, projectId: Int) {
        this.openDatabase()
        for (itemsXML in items) {
            if (itemsXML.alternate == "N") {
                val values = ContentValues()
                values.put("InventoryID", projectId)
                values.put("TypeID", itemsXML.typeID)
                values.put("ItemID", itemsXML.itemID)
                values.put("QuantityInSet", itemsXML.quantityInSet.toInt())
                values.put("QuantityInStore", 0)
                values.put("ColorID", itemsXML.colorID)
                values.put("Extra", itemsXML.extra)
                this.writableDatabase.insert("InventoriesParts", null, values)
            }
        }
        this.close()
    }

    fun changeQuantityInStore(itemId: Int, newQuantity: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("QuantityInStore", newQuantity)
        val success = db.update("InventoriesParts", values, "ID=?", arrayOf(itemId.toString())).toLong()
        db.close()
        return success
    }

    private fun getTitle(itemId: Int, colorId: Int): String {
        var cursor: Cursor? = null
        var name = ""
        try {
            this.openDatabase()
            cursor = this.readableDatabase.query("Parts", arrayOf("Name"), "Code = $itemId", null, null, null, null)
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex("Name"))
            }
        } catch (e: SQLiteException) {

        } finally {
            cursor?.close()
            this.close()
        }
        return name
    }

    override fun onCreate(db: SQLiteDatabase?) {}

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
}