package com.benmohammad.mvitodos.util

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

fun <T> pairWithDelay(immediate: T, delayed: T): Observable<T> {
    return Observable.timer(2, TimeUnit.SECONDS)
        .map { delayed }
        .startWith(immediate)
}