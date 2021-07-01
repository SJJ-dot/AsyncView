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

object AsyncInflater {
    private val inflaterExecutor = Executors.newSingleThreadScheduledExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    fun inflate(
        inflater: LayoutInflater,
        layoutRes: Int,
        parent: ViewGroup? = null,
        attachToRoot: Boolean = parent != null,
        inflateDelayMillis: Long = 300,
        callbackDelayMillis: Long = 300,
        mainInflate: Boolean = false,
        callback: (View) -> Unit
    ): Disposable {
        val ref = RefDisposable()
        if (mainInflate) {
            val infDisposable = HandlerDisposable(mainHandler) {
                val view = inflater.inflate(layoutRes, parent, false)
                val callbackDisposable = HandlerDisposable(mainHandler) {
                    if (attachToRoot) {
                        parent!!.addView(view)
                        callback(parent)
                    } else {
                        callback(view)
                    }
                }
                ref.setDispose(callbackDisposable)
                mainHandler.postDelayed(callbackDisposable, callbackDelayMillis)
            }
            ref.setDispose(infDisposable)
            mainHandler.postDelayed(infDisposable, inflateDelayMillis)
        } else {
            val schedule = inflaterExecutor.schedule({
                try {
                    val view = inflater.inflate(layoutRes, parent, false)
                    val callbackDisposable = HandlerDisposable(mainHandler) {
                        if (attachToRoot) {
                            parent!!.addView(view)
                            callback(parent)
                        } else {
                            callback(view)
                        }
                    }
                    ref.setDispose(callbackDisposable)
                    mainHandler.postDelayed(callbackDisposable, callbackDelayMillis)
                } catch (e: Exception) {
                    Log.e("AsyncInflater","inflate error",e)
                }
            }, inflateDelayMillis, TimeUnit.MILLISECONDS)
            ref.setDispose(FutureDisposable(schedule))
        }

        return ref
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
                Log.e("AsyncInflater","HandlerDisposable dispose")
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