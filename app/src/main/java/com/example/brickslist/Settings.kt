package com.example.brickslist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        partListUrlEditText.setText(GlobalData.partListUrl)
        confirmSettingsButton.setOnClickListener {
            GlobalData.partListUrl = partListUrlEditText.text.toString()
        }
    }
}
