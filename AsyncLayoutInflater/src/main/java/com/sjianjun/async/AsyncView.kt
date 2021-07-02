package com.sjianjun.async

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

@SuppressLint("ViewConstructor")
open class AsyncView constructor(
    context: Context,
    private val layoutRes: Int,
    private val inflateDelayMillis: Long = 300,
    private val callbackDelayMillis: Long = 300,
    private val inflateInUI: Boolean = false,
    private val callbackInUI: Boolean = true,
    private val windowFocus:Boolean = true,
    private val widthParam:Int = LayoutParams.MATCH_PARENT,
    private val heightParam:Int = LayoutParams.MATCH_PARENT,
    private val callback: (View) -> Unit
) : FrameLayout(context) {
    private var inflate:AsyncInflater.Disposable?=null
    var inflated = false


    open fun inflate() {
        if (!inflated) {
            inflated = true
            inflate = AsyncInflater.inflate(
                LayoutInflater.from(context),
                layoutRes,
                this,
                true,
                inflateDelayMillis,
                callbackDelayMillis,
                inflateInUI,
                callbackInUI,
                callback
            )
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (this.windowFocus && hasWindowFocus) {
            inflate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!windowFocus || hasWindowFocus()) {
            inflate()
        }
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        inflate?.dispose()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        params?.width = widthParam
        params?.height = heightParam
        super.setLayoutParams(params)
    }
}