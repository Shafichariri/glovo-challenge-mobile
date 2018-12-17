package com.shafic.challenge.network

class NetworkErrors {
    data class NetworkError(val code: Int, val messge: String)
    companion object {
        val shortCircuit = NetworkError(1000, "CONNECTION_SHORT_CIRCUIT")
        val connectException = NetworkError(1000, "CONNECTION_EXCEPTION")
    }
}
