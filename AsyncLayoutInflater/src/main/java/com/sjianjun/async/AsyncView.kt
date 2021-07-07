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
    private val widthParam: Int? = LayoutParams.MATCH_PARENT,
    private val heightParam: Int? = LayoutParams.MATCH_PARENT,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0,
    protected var callback: (View) -> Unit = {}
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private var inflate: AsyncInflater.Disposable? = null

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
            callback = { view->
                post {
                    if (view.parent == null) {
                        addView(view)
                    }
                    onCallback(view)
                }
            }
        )
    }

    open fun onCallback(view: View) {
        callback(view)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        inflate?.dispose()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        if (widthParam != null) {
            params?.width = widthParam
        }
        if (heightParam != null) {
            params?.height = heightParam
        }
        super.setLayoutParams(params)
    }
}