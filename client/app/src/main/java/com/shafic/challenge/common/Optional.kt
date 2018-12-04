package com.shafic.challenge.common


data class Optional<T>(val value: T?) {

    companion object {
        fun <T> empty(): Optional<T> {
            return Optional(null)
        }
    }
}
