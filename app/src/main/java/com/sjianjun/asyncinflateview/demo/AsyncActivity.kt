package com.sjianjun.asyncinflateview.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sjianjun.async.AsyncView

class AsyncActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(AsyncView(this,R.layout.activity_async_test){

        })

    }
}