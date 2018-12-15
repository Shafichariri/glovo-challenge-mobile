package com.shafic.challenge.ui.cityPicker.list

import com.shafic.challenge.R
import com.shafic.challenge.common.base.AdapterItem
import com.shafic.challenge.data.models.City
import com.shafic.challenge.data.models.Country
import com.shafic.challenge.data.models.id


sealed class CityPickerAdapterItem(id: String, layoutId: Int) : AdapterItem(id, layoutId) {
    class CityItem(val city: City) : CityPickerAdapterItem(id = city.id(), layoutId = R.layout.list_city_item)
    class CountryItem(val country: Country) :
        CityPickerAdapterItem(id = country.id(), layoutId = R.layout.list_country_item)
}
