package com.shafic.challenge.repositories

import com.shafic.challenge.injection.module.api
import io.reactivex.Scheduler


abstract class Repository(
    val backgroundThread: Scheduler = api().backgroundThread
)
