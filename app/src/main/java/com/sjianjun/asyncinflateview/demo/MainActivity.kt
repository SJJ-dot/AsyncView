package com.sjianjun.asyncinflateview.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sjianjun.async.AsyncInflateConfig
import com.sjianjun.async.OnInflateFinishedAndResumeCallback
import com.sjianjun.async.utils.AsyncInflateUtil

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AsyncInflateConfig .getDef().fadeTime = 5000
        val result = AsyncInflateUtil().inflate(
            this,
            R.layout.activity_main,
            OnInflateFinishedAndResumeCallback(lifecycle) { view: View, layoutRes: Int, parent: ViewGroup ->
                Toast.makeText(this, "async inflate complete", Toast.LENGTH_SHORT).show()
            })
        setContentView(result.first)

    }
}