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

    private var db: SQLiteDatabase? = null

    fun openDatabase(): SQLiteDatabase {
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

            var cursor = this.readableDatabase.query(
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
                    var id = cursor.getInt(cursor.getColumnIndex("id"))
                    var name = cursor.getString(cursor.getColumnIndex("Name"))
                    var active = cursor.getInt(cursor.getColumnIndex("Active"))
                    var lastAccessed = cursor.getInt(cursor.getColumnIndex("LastAccessed"))

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

    fun getImage(itemId: Int, colorId: Int): Bitmap? {

        this.openDatabase()

        var idCursor =
            this.readableDatabase.query("Parts", arrayOf("id"), "Code = " + itemId.toString(), null, null, null, null)
        var id: Int = 0
        if (idCursor.moveToFirst()) {
            id = idCursor.getInt(idCursor.getColumnIndex("id"))
        }

        var colorCursor =
            this.readableDatabase.query("Colors", arrayOf("id"), "Code = " + colorId.toString(), null, null, null, null)
        var color: Int = 0
        if (colorCursor.moveToFirst()) {
            color = colorCursor.getInt(colorCursor.getColumnIndex("id"))
        }

        var cursor = this.readableDatabase.query(
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
                var image = cursor.getBlob(cursor.getColumnIndex("Image"))
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

            var cursor = this.readableDatabase.query(
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
                    var id = cursor.getInt(cursor.getColumnIndex("id"))
                    var inventoryId = cursor.getInt(cursor.getColumnIndex("InventoryID"))
                    var typeId = cursor.getInt(cursor.getColumnIndex("TypeID"))
                    var itemId = cursor.getInt(cursor.getColumnIndex("ItemID"))
                    var quantityInSet = cursor.getInt(cursor.getColumnIndex("QuantityInSet"))
                    var quantityInStore = cursor.getInt(cursor.getColumnIndex("QuantityInStore"))
                    var colorId = cursor.getInt(cursor.getColumnIndex("ColorID"))
                    var extra = cursor.getInt(cursor.getColumnIndex("Extra"))

                    partList.add(
                        InventoryPart(
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
                    )
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
        for (items in items) {
            if (items.alternate == "N") {
                val values = ContentValues()
                values.put("InventoryID", projectId)
                values.put("TypeID", items.typeID)
                values.put("ItemID", items.itemID)
                values.put("QuantityInSet", items.quantityInSet.toInt())
                values.put("QuantityInStore", 0)
                values.put("ColorID", items.colorID)
                values.put("Extra", items.extra)
                this.writableDatabase.insert("InventoriesParts", null, values)
            }
        }
        this.close()
    }

    fun getTitle(itemId: Int, colorId: Int): String {
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