package com.shafic.newassignment.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat
import java.util.concurrent.TimeUnit


/**
 * Created by shafic on 7/15/17.
 */

object NetworkClientFactory {

    internal fun createJsonClient(baseUrl: String, apiKey: String?): NetworkClient {
        val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(createGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)


        return NetworkClient(retrofit, createDefaultSignedClient(apiKey), createDefaultUnSignedClient())
    }

    private fun createGson(): Gson {
        val gson: Gson = GsonBuilder()
                //Register TypeAdapter with teh needed JsonDeserializer: Example Java Date
                .create()
        return gson
    }

    private fun createDefaultUnSignedClient(): OkHttpClient {
        val defaultClient = OkHttpClient.Builder()

        return defaultClient
                .readTimeout(NetworkConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(createLoggingInterceptor())
                .build()
    }

    private fun createDefaultSignedClient(apiKey: String?): OkHttpClient {
        val defaultClient = OkHttpClient.Builder()
                .readTimeout(NetworkConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(createLoggingInterceptor())


        if (apiKey != null) {
            defaultClient.addInterceptor(createKeyAuthenticationInterceptor(apiKey))
        }

        //Add authentication interceptor 
        //Using defaultClient.authenticator([implementor of Authenticator])
        
        return defaultClient.build()
    }

    private fun createKeyAuthenticationInterceptor(apiKey: String): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
                    ?.newBuilder()
                    ?.addHeader("api-key", apiKey)
                    ?.build()
            chain.proceed(request!!)
        }
    }

    private fun createLoggingInterceptor(): Interceptor {
        val logging = HttpLoggingInterceptor()
        // set your desired log level
        logging.level = HttpLoggingInterceptor.Level.BODY

        return logging

    }
}
