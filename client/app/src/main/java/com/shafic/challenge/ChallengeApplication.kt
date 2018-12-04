package com.shafic.challenge

import android.app.Application
import com.shafic.challenge.injection.module.DaggerAppComponent

class ChallengeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder().create(this).inject(this)
    }
}
