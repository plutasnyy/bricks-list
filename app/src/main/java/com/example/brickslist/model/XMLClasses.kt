package com.example.brickslist.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("INVENTORY")
internal data class XMLInventory(

    @set :JsonProperty("ITEM")
    var items: List<InventoryPartXML> = ArrayList()
)

@JsonRootName("ITEM")
class InventoryPartXML(
    @set:JsonProperty("ITEMTYPE")
    var typeID: String,

    @set:JsonProperty("ITEMID")
    var itemID: String,

    @set:JsonProperty("QTY")
    var quantityInSet: String,

    @set:JsonProperty("COLOR")
    var colorID: String,

    @set:JsonProperty("EXTRA")
    var extra: String,

    @set:JsonProperty("ALTERNATE")
    var alternate: String
)