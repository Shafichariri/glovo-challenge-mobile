package com.shafic.challenge.ui.cityPicker.list

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.shafic.challenge.common.base.BaseAdapter
import com.shafic.challenge.common.base.BaseViewHolder
import com.shafic.challenge.databinding.ListCityItemBinding
import com.shafic.challenge.databinding.ListCountryItemBinding
import java.util.*

class CitiesAdapter(context: Context, data: MutableList<CityPickerAdapterItem> = ArrayList()) :
    BaseAdapter<BaseViewHolder, CityPickerAdapterItem>(context, data) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate(layoutInflater, viewType, parent, false)
        return bindViewHolders(binding)
    }

    private fun bindViewHolders(binding: ViewDataBinding): BaseViewHolder {
        when (binding) {
            is ListCountryItemBinding -> return CountryViewHolder(viewBinding = binding)
            is ListCityItemBinding -> return CityViewHolder(viewBinding = binding)
        }
        throw Exception("View Binding Not Found / Registered")
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        when (holder) {
            is CountryViewHolder -> holder.bind(data[position] as CityPickerAdapterItem.CountryItem)
            is CityViewHolder -> holder.bind(data[position] as CityPickerAdapterItem.CityItem)
        }
    }
}
