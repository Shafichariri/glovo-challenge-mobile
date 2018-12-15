package com.shafic.challenge.ui.cityPicker.list

import com.shafic.challenge.common.base.DataBoundViewHolder
import com.shafic.challenge.databinding.ListCountryItemBinding

class CountryViewHolder(viewBinding: ListCountryItemBinding) :
    DataBoundViewHolder<ListCountryItemBinding>(viewBinding) {
    
    fun bind(item: CityPickerAdapterItem.CountryItem) {
        viewBinding.countryName = item.country.name
    }
}
