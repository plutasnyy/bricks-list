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
import com.example.brickslist.GlobalData
import com.example.brickslist.model.InventoryPart
import com.example.brickslist.model.InventoryPartXML
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.util.*
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URL


class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {
    companion object {
        private const val DB_NAME = "BrickList.db"
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
        values.put("Name", projectCode)
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
        idCursor.close()

        val colorCursor =
            this.readableDatabase.query("Colors", arrayOf("id"), "Code = $colorId", null, null, null, null)
        var color = 0
        if (colorCursor.moveToFirst()) {
            color = colorCursor.getInt(colorCursor.getColumnIndex("id"))
        }
        colorCursor.close()
        Log.d("DOWNLOAD", "$colorId $color")
        val cursor = this.readableDatabase.query(
            "Codes",
            arrayOf("id", "Code", "Image"),
            "ItemID = $id and ColorID = $color",
            null,
            null,
            null,
            null
        )

        var bmp: Bitmap? = null
        val isCountGreatherThanZero = cursor.count > 0
        var code = 0
        var codesID = 0
        if (isCountGreatherThanZero) {
            cursor.moveToFirst()
            if (cursor.getBlob(cursor.getColumnIndex("Image")) != null) {
                val image = cursor.getBlob(cursor.getColumnIndex("Image"))
                bmp = BitmapFactory.decodeByteArray(image, 0, image.size)
            } else {
                code = cursor.getInt(cursor.getColumnIndex("Code"))
                codesID = cursor.getInt(cursor.getColumnIndex("id"))
            }
        }
        cursor.close()
        this.close()

        var downloaded: Bitmap? = null
        runBlocking {
            if (bmp == null) {
                val job: Job
                if (!isCountGreatherThanZero) {
                    job = GlobalScope.launch {
                        Log.d("DOWNLOAD", "IF")
                        downloaded = downloadOldImage(itemId)
                        val local = downloaded
                        if (local != null) {
                            addNewBitmap(local, itemId, color)
                        }
                        Log.d("DOWNLOAD", "FINISHED")
                    }
                } else {
                    job = GlobalScope.launch {
                        Log.d("DOWNLOAD", "ELSE")
                        downloaded = downloadImage(code)
                        if (downloaded == null) {
                            downloaded = downloadAlternativeImage(itemId, color)
                        }
                        val local = downloaded
                        if (local != null) {
                            updateBitmap(codesID, local)
                        }
                        Log.d("DOWNLOAD", "FINISHED")
                    }
                }
                job.join()
                bmp = downloaded
            }
        }

        Log.d("DOWNLOAD", "OUTSIDE")

        var result: Bitmap? = null
        if (bmp != null) {
            result = Bitmap.createScaledBitmap(bmp, 250, 250, false)
        }
        return result
    }

    private fun updateBitmap(id: Int, result: Bitmap) {
        this.openDatabase()
        val image = bitmapToByteArray(result)
        val values = ContentValues()
        values.put("Image", image)
        this.writableDatabase.update("Codes", values, "id=?", arrayOf(id.toString())).toLong()
        this.close()
    }

    private fun addNewBitmap(result: Bitmap, itemId: Int, color: Int) {
        this.openDatabase()
        val image = bitmapToByteArray(result)
        val values = ContentValues()
        values.put("ItemID", itemId)
        values.put("ColorID", color)
        values.put("Image", image)
        this.writableDatabase.insert("Codes", null, values)
        this.close()
    }

    private fun bitmapToByteArray(image: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }

    private fun downloadOldImage(id: Int): Bitmap? {
        val url = GlobalData.oldImageUrl + id + ".jpg"
        Log.d("DOWNLOAD_OLD", url)
        return getImageFromUrl(url)
    }

    private fun downloadAlternativeImage(id: Int, color: Int): Bitmap? {
        val url = GlobalData.alternativeNewImageUrl + color + "/" + id + ".jpg"
        Log.d("DOWNLOAD_ALTERNATIVE", url)
        return getImageFromUrl(url)
    }


    private fun downloadImage(code: Int): Bitmap? {
        val url = GlobalData.imageUrl + code
        Log.d("DOWNLOAD_CLASSIC", url)
        return getImageFromUrl(url)

    }

    private fun getImageFromUrl(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.run {
                connection.doInput = true
                connect()
            }
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

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
        val success = db.update("InventoriesParts", values, "id=?", arrayOf(itemId.toString())).toLong()
        db.close()
        return success
    }

    fun changeActiveInventory(inventoryId: Int, newActive: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("Active", newActive)
        val success = db.update("Inventories", values, "ID=?", arrayOf(inventoryId.toString())).toLong()
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