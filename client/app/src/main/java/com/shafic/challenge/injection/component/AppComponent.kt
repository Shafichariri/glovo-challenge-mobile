package com.shafic.challenge.injection.component

import android.content.Context
import io.reactivex.Scheduler


interface AppComponent {
    val context: Context
    val mainThread: Scheduler
    val backgroundThread: Scheduler

    companion object {
        lateinit var instance: AppComponent
    }
}
