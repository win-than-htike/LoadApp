package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    companion object {
        const val FILE_NAME = "FILE_NAME"
        const val STATUS = "STATUS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        setSupportActionBar(toolbar)
        val fileName = intent.getStringExtra(FILE_NAME)
        val status = intent.getStringExtra(STATUS)
        tvFileName.text = fileName
        tvStatus.text = status

        btnOk.setOnClickListener {
            onBackPressed()
        }

    }

}
