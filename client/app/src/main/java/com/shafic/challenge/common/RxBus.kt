package com.shafic.challenge.common

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject


object RxBus {
    private val mSubject = PublishSubject.create<BaseEvent>()

    fun publish(event: BaseEvent) {
        // Publish on UI thread to be able to update views
        Flowable
            .fromCallable {
                mSubject.onNext(event)
                return@fromCallable
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

    }

    fun events(): Flowable<BaseEvent> = mSubject.toFlowable(BackpressureStrategy.BUFFER)
}
