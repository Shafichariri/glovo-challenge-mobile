package com.shafic.challenge.injection.module

import android.content.Context
import com.shafic.challenge.injection.ApplicationScope
import dagger.Module
import dagger.Provides

@Module
class ContextModule(private val context: Context) {

    @Provides
    @ApplicationScope
    internal fun provideContext(): Context {
        return context
    }
}
