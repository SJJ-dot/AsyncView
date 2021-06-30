package com.sjianjun.async

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.concurrent.*

object AsyncInflater {
    private val inflaterExecutor = Executors.newSingleThreadScheduledExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    fun inflate(
        inflater: LayoutInflater,
        res: Int,
        parent: ViewGroup? = null,
        attachToRoot: Boolean = parent != null,
        inflateDelayMillis: Long = 300,
        callbackDelayMillis: Long = 300
    ): Future<*> {
        return if (inflateDelayMillis > 0) {
            inflaterExecutor.schedule({
                val inflate = inflater.inflate(res, parent, attachToRoot)

            }, inflateDelayMillis, TimeUnit.MILLISECONDS)
        } else {
            inflaterExecutor.submit {
                inflater.inflate(res, parent, attachToRoot)
            }
        }

    }

    private fun post(delay: Long, view: View) {
        mainHandler.postDelayed({

        },delay)
    }
}