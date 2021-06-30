package com.sjianjun.asyncinflateview.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sjianjun.async.AsyncInflateConfig
import com.sjianjun.async.OnInflateFinishedAndResumeCallback
import com.sjianjun.async.utils.AsyncInflateUtil
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