package com.shafic.challenge.data.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class City(
    val code: String,
    val name: String,
    val currency: String?,
    @SerializedName("country_code")
    val countryCode: String,
    val enabled: Boolean?,
    @SerializedName("time_zone")
    val timeZone: String?,
    @SerializedName("working_area") val workingArea: Array<String>,
    val busy: Boolean?,
    @SerializedName("language_code") val languageCode: String?
) {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        val otherCity = other as City

        if (!Arrays.equals(workingArea, otherCity.workingArea)) return false

        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(workingArea)
    }
}
