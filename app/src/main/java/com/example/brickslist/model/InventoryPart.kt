package com.example.brickslist.model
import com.fasterxml.jackson.annotation.JsonRootName

import android.graphics.Bitmap
import com.fasterxml.jackson.annotation.JsonProperty

@JsonRootName("ITEM")
class InventoryPart(
    val id: Int,
    val inventoryID: Int,
    var typeID: Int,
    var itemID: Int,
    var quantityInSet: Int,
    var colorID: Int,
    var extra: Int,
    val quantityInStore: Int = 0,
    val title: String,
    val image: Bitmap?
)

