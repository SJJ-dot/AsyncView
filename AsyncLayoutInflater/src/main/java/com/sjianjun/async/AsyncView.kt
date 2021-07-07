package com.sjianjun.async

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

@SuppressLint("ViewConstructor")
open class AsyncView constructor(
    context: Context,
    layoutRes: Int,
    inflateDelayInUIMillis: Long = 300,
    inflateInUI: Boolean = false,
    callbackDelayMillis: Long = 300,
    private val widthParam: Int = LayoutParams.MATCH_PARENT,
    private val heightParam: Int = LayoutParams.MATCH_PARENT,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0,
    protected var callback: (View) -> Unit = {}
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private var inflate: AsyncInflater.Disposable? = null
    private var view: View? = null

    init {
        inflate = AsyncInflater.inflate(
            LayoutInflater.from(context),
            layoutRes,
            parent = this,
            attachToRoot = true,
            inflateDelayMillis = if (inflateInUI) inflateDelayInUIMillis
            else 0,
            callbackDelayMillis = callbackDelayMillis,
            inflateInUI = inflateInUI,
            callbackInUI = true,
            callback = this::inflateCallback
        )
    }

    open fun inflateCallback(view: View) {
        if (hasWindowFocus()) {
            callCallback(view)
        } else {
            this.view = view
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        val locView = view ?: return
        if (hasWindowFocus) {
            view = null
            callCallback(locView)
        }
    }

    private fun callCallback(locView: View) {
        post {
            if (locView.parent == null) {
                addView(locView)
            }
            callback(locView)
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