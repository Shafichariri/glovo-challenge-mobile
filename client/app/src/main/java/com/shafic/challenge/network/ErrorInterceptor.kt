package com.shafic.challenge.network

import com.shafic.challenge.common.BaseEvent
import com.shafic.challenge.common.RxBus
import okhttp3.Interceptor
import okhttp3.Response

class ErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain?): Response {
        try {
            if (chain == null) {
                RxBus.publish(BaseEvent.ConnectionFailed(NetworkErrors.shortCircuit))
                return Response.Builder().build()
            }
            return chain.proceed(chain.request())
        } catch (exception: Exception) {
            exception.printStackTrace()
            //Send a notification of the failure
            RxBus.publish(BaseEvent.ConnectionFailed(NetworkErrors.connectException))
            return Response.Builder().build()
        }
    }
}
