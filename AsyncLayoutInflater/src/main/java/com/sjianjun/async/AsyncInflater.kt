package com.sjianjun.async

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

object AsyncInflater {
    private val inflaterExecutor = Executors.newSingleThreadScheduledExecutor()
    private val uiHandler = Handler(Looper.getMainLooper())


    fun inflate(
        inflater: LayoutInflater,
        layoutRes: Int,
        parent: ViewGroup? = null,
        attachToRoot: Boolean = parent != null,
        inflateDelayMillis: Long = 0,
        callbackDelayMillis: Long = 300,
        inflateInUI: Boolean = false,
        callbackInUI: Boolean = true,
        callback: (View) -> Unit
    ): Disposable {
        val ref = RefDisposable()
        if (inflateInUI) {
            val infDisposable = HandlerDisposable(uiHandler) {
                val view = inflater.inflate(layoutRes, parent, false)
                callback(
                    callbackInUI,
                    attachToRoot,
                    parent,
                    view,
                    callback,
                    ref,
                    callbackDelayMillis
                )

            }
            ref.setDispose(infDisposable)
            uiHandler.postDelayed(infDisposable, inflateDelayMillis)
        } else {
            val schedule = inflaterExecutor.schedule({
                try {
                    val view = inflater.cloneInContext(inflater.context).inflate(layoutRes, parent, false)
                    callback(
                        callbackInUI,
                        attachToRoot,
                        parent,
                        view,
                        callback,
                        ref,
                        callbackDelayMillis
                    )
                } catch (e: Exception) {
                    Log.e("AsyncInflater", "inflate error", e)
                }
            }, inflateDelayMillis, TimeUnit.MILLISECONDS)
            ref.setDispose(FutureDisposable(schedule))
        }

        return ref
    }

    private fun callback(
        callbackInUI: Boolean,
        attachToRoot: Boolean,
        parent: ViewGroup?,
        view: View,
        callback: (View) -> Unit,
        ref: RefDisposable,
        callbackDelayMillis: Long
    ) {
        if (callbackInUI) {
            val callbackDisposable = HandlerDisposable(uiHandler) {
                if (attachToRoot) {
                    parent!!.addView(view)
                }
                callback(view)
            }
            ref.setDispose(callbackDisposable)
            uiHandler.postDelayed(callbackDisposable, callbackDelayMillis)
        } else {
            val schedule = inflaterExecutor.schedule({
                if (attachToRoot && parent?.isAttachedToWindow == false) {
                    parent.addView(view)
                }
                callback(view)
            }, callbackDelayMillis, TimeUnit.MILLISECONDS)
            ref.setDispose(FutureDisposable(schedule))
        }
    }


    interface Disposable {
        fun dispose()
        fun isDisposed(): Boolean
    }

    private class FutureDisposable(private val future: Future<*>) : AtomicBoolean(), Disposable {
        override fun dispose() {
            if (compareAndSet(false, true)) {
                future.cancel(true)
            }
        }

        override fun isDisposed(): Boolean {
            return get()
        }
    }

    private class HandlerDisposable(
        private val handler: Handler,
        private val callback: () -> Unit
    ) : AtomicBoolean(), Disposable, Runnable {
        override fun dispose() {
            if (compareAndSet(false, true)) {
                Log.e("AsyncInflater", "HandlerDisposable dispose")
                handler.removeCallbacks(this)
            }
        }

        override fun isDisposed(): Boolean {
            return get()
        }

        override fun run() {
            if (!isDisposed()) {
                callback()
                lazySet(true)
            }
        }
    }

    private class RefDisposable : AtomicBoolean(), Disposable {
        private val disposableRef = AtomicReference<Disposable?>()
        fun setDispose(disposable: Disposable) {
            val ref = disposableRef.get()
            if (ref == this) {
                disposable.dispose()
            } else if (!disposableRef.compareAndSet(ref, disposable)) {
                disposable.dispose()
            }

        }

        override fun dispose() {
            if (compareAndSet(false, true)) {
                disposableRef.getAndSet(this)?.dispose()
            }
        }

        override fun isDisposed(): Boolean {
            return get()
        }
    }
}