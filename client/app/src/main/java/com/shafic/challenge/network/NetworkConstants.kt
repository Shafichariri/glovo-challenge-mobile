package com.shafic.newassignment.network

import com.shafic.challenge.BuildConfig


/**
 * NetworkConstants Singleton
 *
 * Static final fields
 * Created by shafic on 7/15/17.
 */

class NetworkConstants {
    companion object {
        const val READ_TIMEOUT_SECONDS = 60L
//        const val APP_VERSION: String = BuildConfig.VERSION_NAME
//        const val DEVICE_OS: String = "android"

//        val DEVICE_ID: String by lazy {
//            val application = BaseApplication.getInstance<TreckerApplication>()
//
//            return@lazy Settings.Secure.getString(application.contentResolver,
//                    Settings.Secure.ANDROID_ID)
//        }
        
        val API_BASE_URL by lazy {
            BuildConfig.BASE_URL + BuildConfig.API_PATH_EXTENSION
        }
        
        /**
         * Set API KEY if needed
         *
         *  val API_KEY by lazy {
         *   BuildConfig.API_KEY
         *  }
         */
    }
}
