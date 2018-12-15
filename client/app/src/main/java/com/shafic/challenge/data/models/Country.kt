package com.shafic.challenge.data.models

data class Country(val code: String, val name: String)

fun Country.id(): String {
    return code
}
