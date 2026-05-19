package com.example.pillulkin.data.local.dao;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LiveDataUtils {
    public static <T> T getOrAwaitValue(LiveData<T> liveData, long timeout, TimeUnit unit) throws InterruptedException {
        final Object[] result = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T t) {
                result[0] = t;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        new Handler(Looper.getMainLooper()).post(() -> liveData.observeForever(observer));
        if (!latch.await(timeout, unit)) {
            new Handler(Looper.getMainLooper()).post(() -> liveData.removeObserver(observer));
            throw new RuntimeException("LiveData value was never set");
        }
        return (T) result[0];
    }
}
