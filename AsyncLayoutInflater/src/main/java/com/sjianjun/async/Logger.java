package com.sjianjun.async;

public interface Logger {
    void log(String msg,Throwable e);

    Logger empty = new Logger() {
        @Override
        public void log(String msg, Throwable e) {

        }
    };
}
