package com.shafic.challenge.injection.module

import android.app.Application
import com.shafic.challenge.ChallengeApplication
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton


@Singleton
@Component(modules = [AppModule::class])
internal interface AppComponent : AndroidInjector<ChallengeApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<ChallengeApplication>()
}

@Module(includes = [AndroidInjectionModule::class])
abstract class AppModule {

    @Binds
    abstract fun application(app: ChallengeApplication): Application
}
