package com.sjianjun.asyncinflateview.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        async.setOnClickListener {
            startActivity(Intent(this,AsyncActivity::class.java))
        }
        sync.setOnClickListener {
            startActivity(Intent(this,SyncActivity::class.java))
        }
    }
}