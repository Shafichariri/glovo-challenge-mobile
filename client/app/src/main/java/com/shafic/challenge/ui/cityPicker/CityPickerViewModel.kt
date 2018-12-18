package com.shafic.challenge.ui.cityPicker

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import com.shafic.challenge.common.base.BaseViewModel
import com.shafic.challenge.data.models.City
import com.shafic.challenge.navigation.coordinators.CityPickerFlowProvider
import com.shafic.challenge.ui.cityPicker.list.CityPickerAdapterItem
import com.shafic.challenge.ui.cityPicker.useCase.CountriesAndCitiesGroupingUseCase
import com.shafic.challenge.ui.cityPicker.useCase.CountriesAndCitiesGroupingUseCaseImp
import com.shafic.challenge.ui.cityPicker.useCase.CountriesAndCitiesZipAndGroupUseCase
import com.shafic.challenge.ui.cityPicker.useCase.CountriesAndCitiesZipAndGroupUseCaseImp
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class CityPickerViewModel : BaseViewModel() {

    @NonNull
    private val compositeDisposable = CompositeDisposable()

    private val groupingUseCase: CountriesAndCitiesGroupingUseCase = CountriesAndCitiesGroupingUseCaseImp()
    private val zipAndGroupUseCase: CountriesAndCitiesZipAndGroupUseCase = CountriesAndCitiesZipAndGroupUseCaseImp()

    private val itemsLiveData: MutableLiveData<MutableList<CityPickerAdapterItem>> = MutableLiveData()
    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    private val selectedCity: MutableLiveData<City?> = MutableLiveData()
    private val error: MutableLiveData<Boolean> = MutableLiveData()
    private var flow: CityPickerFlowProvider? = null

    fun setFlowCoordinator(flow: CityPickerFlowProvider) {
        this.flow = flow
    }

    fun getItemsLiveData(): LiveData<MutableList<CityPickerAdapterItem>> {
        return itemsLiveData
    }

    fun getIsLoading(): LiveData<Boolean> {
        return isLoading
    }

    fun getSelectedCity(): LiveData<City?> {
        return selectedCity
    }

    fun finishWithConfirmedSelection(intent: Intent) {
        flow?.closeCityPicker(CityPickerActivity.SELECTION_RESULT_CODE, intent)
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

    fun reset() {
        isLoading.value = false
    }

    fun loadData() {
        isLoading.value = true
        val disposable = zipAndGroupUseCase.zip(groupingUseCase)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                itemsLiveData.value = it.toMutableList()
                isLoading.value = false
                error.value = false
            }, {
                it.printStackTrace()
                isLoading.value = false
                error.value = true
            })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}
