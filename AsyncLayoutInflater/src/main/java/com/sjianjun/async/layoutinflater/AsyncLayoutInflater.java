package com.sjianjun.async.layoutinflater;

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sjianjun.async.Disposable;
import com.sjianjun.async.Logger;
import com.sjianjun.async.OnInflateFinishedListener;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * <p>Helper class for inflating layouts asynchronously. To use, construct
 * an instance of {@link AsyncLayoutInflater} on the UI thread and call
 * {@link #inflate(int, ViewGroup, boolean, OnInflateFinishedListener)}. The
 * {@link OnInflateFinishedListener} will be invoked on the UI thread
 * when the inflate request has completed.
 *
 * <p>This is intended for parts of the UI that are created lazily or in
 * response to user interactions. This allows the UI thread to continue
 * to be responsive & animate while the relatively heavy inflate
 * is being performed.
 *
 * <p>For a layout to be inflated asynchronously it needs to have a parent
 * whose {@link ViewGroup#generateLayoutParams(AttributeSet)} is thread-safe
 * and all the Views being constructed as part of inflation must not create
 * any {@link Handler}s or otherwise call {@link Looper#myLooper()}. If the
 * layout that is trying to be inflated cannot be constructed
 * asynchronously for whatever reason, {@link AsyncLayoutInflater} will
 * automatically fall back to inflating on the UI thread.
 *
 * <p>NOTE that the inflated View hierarchy is NOT added to the parent. It is
 * equivalent to calling {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
 * with attachToRoot set to false. Callers will likely want to call
 * {@link ViewGroup#addView(View)} in the {@link OnInflateFinishedListener}
 * callback at a minimum.
 *
 * <p>This inflater does not support setting a {@link LayoutInflater.Factory}
 * nor {@link LayoutInflater.Factory2}. Similarly it does not support inflating
 * layouts that contain fragments.
 */
public final class AsyncLayoutInflater {

    LayoutInflater mInflater;
    Handler mHandler;
    ExecutorService mInflateThread = Executors.newSingleThreadExecutor();

    Logger logger;


    public AsyncLayoutInflater(LayoutInflater inflater, Logger logger) {
        mInflater = inflater;
        this.logger = logger;
        mHandler = new Handler(Looper.getMainLooper(), mHandlerCallback);
    }

    public Disposable inflate(int resId, ViewGroup parent, boolean main, OnInflateFinishedListener callback) {
        InflateRequest request = new InflateRequest();
        request.inflater = this;
        if (logger != null) {
            request.logger = logger;
        }
        request.resId = resId;
        request.parent = parent;
        request.callback = callback;
        request.set(false);
        if (main) {
            Message.obtain(request.inflater.mHandler, 0, request).sendToTarget();
        } else {
            request.future = mInflateThread.submit(request);
        }
        return request;
    }


    private Callback mHandlerCallback = new Callback() {
        @Override
        public boolean handleMessage(@NotNull Message msg) {
            final InflateRequest request = (InflateRequest) msg.obj;
            //检查是否被取消
            if (request.isDisposed()) {
                return true;
            }

            try {
                if (request.view == null) {
                    request.view = mInflater.inflate(
                            request.resId, request.parent, false);
                    //检查是否被取消
                    if (request.isDisposed()) {
                        return true;
                    }
                }

                request.callback.onInflateFinished(request.view, request.resId, request.parent);
            } catch (Throwable e) {
                if (logger != null) {
                    logger.log("onInflateFinished error:" + e.getMessage(), e);
                }
            }
            return true;
        }
    };

    private static class InflateRequest extends AtomicBoolean implements Disposable, Runnable {
        AsyncLayoutInflater inflater;
        ViewGroup parent;
        int resId;
        View view;
        OnInflateFinishedListener callback;
        Logger logger = Logger.empty;
        Future<?> future = null;

        InflateRequest() {
        }

        @Override
        public boolean isDisposed() {
            Future<?> future = this.future;
            if (future != null) {
                return get() || future.isCancelled();
            } else {
                return get();
            }
        }

        @Override
        public void dispose() {
            set(true);
            Future<?> future = this.future;
            if (future != null) {
                this.future = null;
                future.cancel(true);
            }
        }

        @Override
        public void run() {
            if (isDisposed()) {
                logger.log("inflate isDisposed:" + isDisposed() + "  resid:" + resId + " parent:" + parent, null);
                return;
            }
            try {
                view = inflater.mInflater.inflate(resId, parent, false);
            } catch (InterruptedException ex) {
                // Probably a Looper failure, retry on the UI thread
                logger.log("resId:" + resId + " Failed to inflate resource in the background! " + ex.getMessage(), ex);
                return;
            } catch (Throwable ex) {
                // Probably a Looper failure, retry on the UI thread
                logger.log("resId:" + resId + " Failed to inflate resource in the background! Retrying on the UI thread:" + ex.getMessage(), ex);
            }
            if (isDisposed()) {
                logger.log("inflate isDisposed:" + isDisposed() + "  resid:" + resId + " parent:" + parent, null);
                return;
            }
            logger.log("send message inflate complete:" + (view != null), null);
            future = null;
            Message.obtain(inflater.mHandler, 0, this).sendToTarget();
        }
    }
}

