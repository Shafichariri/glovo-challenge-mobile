package com.shafic.challenge.injection.module

import android.content.Context
import com.shafic.challenge.injection.component.AppComponent
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

//Class Accessor
fun app(): AppComponent = AppComponent.instance

internal class AppModule(override val context: Context) : AppComponent {
    override val mainThread: Scheduler
        get() = AndroidSchedulers.mainThread()
    override val backgroundThread: Scheduler
        get() = Schedulers.io()
}
