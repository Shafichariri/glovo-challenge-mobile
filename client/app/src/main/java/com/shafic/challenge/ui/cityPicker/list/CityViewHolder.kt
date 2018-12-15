package com.shafic.challenge.ui.cityPicker.list

import com.shafic.challenge.common.base.DataBoundViewHolder
import com.shafic.challenge.databinding.ListCityItemBinding


class CityViewHolder(viewBinding: ListCityItemBinding) : DataBoundViewHolder<ListCityItemBinding>(viewBinding) {

    fun bind(item: CityPickerAdapterItem.CityItem) {
        viewBinding.cityName = item.city.name
    }
}
