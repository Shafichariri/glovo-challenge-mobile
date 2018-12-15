package com.shafic.challenge.ui.cityPicker

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.shafic.challenge.common.base.BaseViewModel
import com.shafic.challenge.data.api.CitiesService
import com.shafic.challenge.data.api.CountriesService
import com.shafic.challenge.data.models.City
import com.shafic.challenge.data.models.Country
import com.shafic.challenge.ui.cityPicker.list.CityPickerAdapterItem
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CityPickerViewModel : BaseViewModel() {

    @NonNull
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var countryApi: CountriesService
    @Inject
    lateinit var citiesApi: CitiesService

    private val itemsLiveData: MutableLiveData<MutableList<CityPickerAdapterItem>> = MutableLiveData()
    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    private val selectedCity: MutableLiveData<City?> = MutableLiveData()

    fun getItemsLiveData(): LiveData<MutableList<CityPickerAdapterItem>> {
        return itemsLiveData
    }

    fun getIsLoading(): LiveData<Boolean> {
        return isLoading
    }

    fun getSelectedCity(): LiveData<City?> {
        return selectedCity
    }

    fun cancelSelection() {
        selectedCity.value = null
    }
    
    fun onAdapterItemClick(item: CityPickerAdapterItem) {
        when (item) {
            is CityPickerAdapterItem.CityItem -> {
                selectedCity.value = item.city
            }
            else -> {
                //Maybe collapse
                return
            }
        }
    }


    fun loadData() {
        isLoading.value = true
        val disposable =
            Maybe.zip<Array<Country>, Array<City>, List<CityPickerAdapterItem>>(countryApi.get(), citiesApi.get(),
                BiFunction { countries, cities ->
                    return@BiFunction createAdapterItems(countries, cities)
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    itemsLiveData.value = it.toMutableList()
                    isLoading.value = false
                }, {
                    it.printStackTrace()
                    isLoading.value = false
                })
        compositeDisposable.add(disposable)
    }

    private fun createAdapterItems(countries: Array<Country>?, cities: Array<City>?): List<CityPickerAdapterItem> {
        val citiesGroupedByCountry = cities?.groupBy { it.countryCode }?.toSortedMap() ?: return emptyList()
        return citiesGroupedByCountry
            .map { entry ->
                var items = mutableListOf<CityPickerAdapterItem>()
                val country = countries?.find { it.code == entry.key } ?: return@map items
                val countryItem = CityPickerAdapterItem.CountryItem(country = country)
                items.add(countryItem)

                val cities = entry.value
                val cityItems = cities.map { CityPickerAdapterItem.CityItem(it) }.sortedBy { it.city.name }
                items.addAll(cityItems)
                return@map items
            }.flatten()
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}
