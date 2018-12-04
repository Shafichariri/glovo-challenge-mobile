package com.shafic.newassignment.network

/**
 * NetworkManager Singleton
 *
 * Declaring class as an object in Kotlin is Java's Singleton equivalent.
 *
 * Created by shafic on 7/15/17.
 */

object NetworkManager {
    var jsonClient = NetworkClientFactory.createJsonClient(NetworkConstants.API_BASE_URL,
            apiKey = null)

    //Add extra utilities | Switch Server | Use another client | extract defaulted network errors
}
