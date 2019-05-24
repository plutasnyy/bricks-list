package com.example.brickslist.model
import com.fasterxml.jackson.annotation.JsonRootName

import android.graphics.Bitmap

@JsonRootName("ITEM")
class InventoryPart(
    val id: Int,
    val inventoryID: Int,
    var typeID: Int,
    var itemID: Int,
    var quantityInSet: Int,
    var quantityInStore: Int = 0,
    var colorID: Int,
    var extra: Int,
    val title: String,
    val image: Bitmap?
)

