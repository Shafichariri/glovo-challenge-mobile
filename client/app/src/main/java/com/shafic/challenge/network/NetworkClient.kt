package com.shafic.newassignment.network

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException


/**
 * Created by shafic on 7/12/17.
 */

class NetworkClient(private val builder: Retrofit.Builder,
                    private val defaultClient: OkHttpClient,
                    private val unsignedSignedClient: OkHttpClient) {

    internal fun <S> createService(serviceClass: Class<S>): S {
        return builder.client(defaultClient)
                .build()
                .create(serviceClass)
    }

    fun <S> createUnsignedService(serviceClass: Class<S>): S {
        return builder.client(unsignedSignedClient)
                .build()
                .create(serviceClass)
    }

    @Throws(IOException::class)
    internal fun <S> parse(bodyClass: Class<S>, responseBody: ResponseBody): S {
        val converter: Converter<ResponseBody, S> = builder.build()
                .responseBodyConverter(bodyClass, arrayOfNulls<Annotation>(0))
        return converter.convert(responseBody)!!
    }
}
