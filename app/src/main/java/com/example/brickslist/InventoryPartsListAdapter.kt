package com.example.brickslist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.brickslist.database.DatabaseHelper
import com.example.brickslist.model.InventoryPart

class InventoryPartsListAdapter(private var list: ArrayList<InventoryPart>, private val context: Context) :
    BaseAdapter(), ListAdapter {

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(pos: Int): Any {
        return list[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }


    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        view = if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.item_row, parent, false)
        } else {
            convertView
        }

        val listItemText = view.findViewById(R.id.listPartTitle) as TextView
        val subtitle = view.findViewById(R.id.listPartSubtitle) as TextView
        val deleteBtn = view.findViewById(R.id.removePartInInventoryButton) as Button
        val addBtn = view.findViewById(R.id.addPartToInventoryButton) as Button
        val image = view.findViewById(R.id.partImage) as ImageView

        listItemText.text = list[position].title
        subtitle.text = "${list[position].quantityInStore}/${list[position].quantityInSet}"
        image.setImageBitmap(list[position].image)

        deleteBtn.setOnClickListener {
            Log.d("_PARTS", "Trying remove part")
            if (list[position].quantityInStore > 0) {
                val db = DatabaseHelper(this.context)
                val suc = db.changeQuantityInStore(list[position].id, list[position].quantityInStore - 1)
                if (-1 != suc.toInt()) {
                    list[position].quantityInStore -= 1
                }
            }
            parent.getChildAt(position).setBackgroundColor(Color.WHITE)
            notifyDataSetChanged()
        }
        addBtn.setOnClickListener {
            Log.d("_PARTS", "Trying add part")
            if (list[position].quantityInStore < list[position].quantityInSet) {
                val db = DatabaseHelper(this.context)
                val suc = db.changeQuantityInStore(list[position].id, list[position].quantityInStore + 1)
                if (-1 != suc.toInt()) {
                    list[position].quantityInStore += 1
                }
            }
            if (list[position].quantityInStore == list[position].quantityInSet) {
                parent.getChildAt(position).setBackgroundColor(Color.GREEN)
            }
                notifyDataSetChanged()
        }

        return view
    }
}