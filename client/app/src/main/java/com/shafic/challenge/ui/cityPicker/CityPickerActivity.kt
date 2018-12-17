package com.shafic.challenge.ui.cityPicker

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.shafic.challenge.R
import com.shafic.challenge.common.BaseEvent
import com.shafic.challenge.common.RxBus
import com.shafic.challenge.common.base.AbstractBaseActivity
import com.shafic.challenge.common.base.BaseAdapter
import com.shafic.challenge.common.dialogs.DialogProvider
import com.shafic.challenge.common.dialogs.DialogProviderImplementation
import com.shafic.challenge.data.models.City
import com.shafic.challenge.databinding.ActivityCityPickerBinding
import com.shafic.challenge.injection.ViewModelFactory
import com.shafic.challenge.ui.cityPicker.list.CitiesAdapter
import com.shafic.challenge.ui.cityPicker.list.CityPickerAdapterItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class CityPickerActivity : AbstractBaseActivity<ActivityCityPickerBinding>(),
    BaseAdapter.OnItemClickListener<CityPickerAdapterItem> {
    data class SelectedItem(val cityCode: String, val countryCode: String)

    companion object {
        const val SELECTION_REQUEST_CODE = 20000
        const val SELECTION_RESULT_CODE = 20001
        const val EXTRA_KEY_CITY_CODE = "CITY_CODE"
        const val EXTRA_KEY_COUNTRY_CODE = "CITY_COUNTRY_CODE"

        private val TAG = CityPickerActivity::class.java.simpleName

        fun shouldHandleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
            return SELECTION_REQUEST_CODE == requestCode && SELECTION_RESULT_CODE == resultCode &&
                    data != null
        }

        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): SelectedItem? {
            val data = data ?: return null
            if (!shouldHandleActivityResult(requestCode, resultCode, data)) {
                return null
            }
            val cityCode = data.getStringExtra(CityPickerActivity.EXTRA_KEY_CITY_CODE)
            val countryCode = data.getStringExtra(CityPickerActivity.EXTRA_KEY_COUNTRY_CODE)
            return SelectedItem(cityCode, countryCode)
        }

        fun intent(context: Context): Intent = Intent(context, CityPickerActivity::class.java)
    }

    private lateinit var viewModel: CityPickerViewModel
    private val compositeDisposable = CompositeDisposable()
    private val dialogProvider: DialogProvider by lazy { DialogProviderImplementation(context = this) }

    override val layoutId: Int
        get() = R.layout.activity_city_picker

    override fun onCreateViewDataBinding(savedInstanceState: Bundle?): ActivityCityPickerBinding? {
        return DataBindingUtil.setContentView(this, layoutId)
    }

    override fun onCreated(savedInstanceState: Bundle?) {
        setupNetworkErrorListener()
        actionBar?.title = resources.getString(R.string.action_bar_title_city_picker)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val binding = viewBinding() ?: return

        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(CityPickerViewModel::class.java)
        setupRecyclerView()

        viewModel.getItemsLiveData().observe(this, Observer { updateAdapter(it) })
        viewModel.getIsLoading().observe(this, Observer { binding.isLoading = it ?: false })
        viewModel.getSelectedCity().observe(this, Observer { handleCitySelection(it) })
        viewModel.loadData()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun setupRecyclerView() {
        val binding = viewBinding() ?: return
        val recyclerView = binding.recyclerView
        val adapter = CitiesAdapter(this, viewModel.getItemsLiveData().value ?: arrayListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.onItemClickListener = this
    }

    private fun updateAdapter(list: MutableList<CityPickerAdapterItem>?) {
        val items = list ?: return
        val binding = viewBinding() ?: return
        val adapter = binding.recyclerView.adapter as? CitiesAdapter
        adapter?.update(items)
    }

    private fun handleCitySelection(city: City?) {
        val city = city ?: return
        dialogProvider.createAlertSelectionValidation(city = city,
            positiveAction = {
                finishActivityWithSelectionResult(city)
            },
            negativeAction = {
                viewModel.cancelSelection()
            })?.show()
    }

    private fun finishActivityWithSelectionResult(city: City) {
        val intent = Intent()
        intent.putExtra(EXTRA_KEY_CITY_CODE, city.code)
        intent.putExtra(EXTRA_KEY_COUNTRY_CODE, city.countryCode)
        setResult(SELECTION_RESULT_CODE, intent)
        finish()
    }

    //region
    override fun onItemClick(position: Int, item: CityPickerAdapterItem) {
        viewModel.onAdapterItemClick(item)
    }
    //endregion

    private fun setupNetworkErrorListener() {
        val disposable = RxBus.events()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ event: BaseEvent? ->
                when (event) {
                    is BaseEvent.ConnectionFailed -> {
                        //Error Info: val error = event.error
                        viewModel.reset()
                        dialogProvider.createNetworkErrorDialog { viewModel.loadData() }?.show()
                    }
                }
            }, { throwable: Throwable? ->
                throwable?.printStackTrace()
            })
        compositeDisposable.add(disposable)
    }
}
