package com.example.brickslist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        partListUrlEditText.setText(GlobalData.partListUrl)
        imgDownloadUrlEdit.setText(GlobalData.imageUrl)
        altImgDownloadEdit.setText(GlobalData.alternativeNewImageUrl)
        oldImgDownloadUrlEdit.setText(GlobalData.oldImageUrl)

        confirmSettingsButton.setOnClickListener {
            GlobalData.oldImageUrl = oldImgDownloadUrlEdit.text.toString()
            GlobalData.alternativeNewImageUrl = altImgDownloadEdit.text.toString()
            GlobalData.imageUrl = imgDownloadUrlEdit.text.toString()
            GlobalData.partListUrl = partListUrlEditText.text.toString()
        }
    }
}
