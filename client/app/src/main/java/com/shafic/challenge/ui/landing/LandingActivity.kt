package com.shafic.challenge.ui.landing

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.shafic.challenge.R
import com.shafic.challenge.common.Dialogs
import com.shafic.challenge.common.base.AbstractBaseActivity
import com.shafic.challenge.common.settingsStarterIntent
import com.shafic.challenge.common.toast
import com.shafic.challenge.databinding.ActivityLandingBinding
import com.shafic.challenge.injection.ViewModelFactory
import com.shafic.challenge.ui.cityPicker.CityPickerActivity
import com.shafic.challenge.ui.map.MainActivity
import com.vanniktech.rxpermission.Permission
import com.vanniktech.rxpermission.RealRxPermission
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.CompositeDisposable


class LandingActivity : AbstractBaseActivity<ActivityLandingBinding>(), ViewListener {
    companion object {
        private val TAG = LandingActivity::class.java.simpleName
        fun intent(context: Context): Intent = Intent(context, LandingActivity::class.java)
    }

    @NonNull
    private val compositeDisposable = CompositeDisposable()

    private lateinit var viewModel: LandingActivityViewModel

    override val layoutId: Int
        get() = R.layout.activity_landing

    override fun onCreateViewDataBinding(savedInstanceState: Bundle?): ActivityLandingBinding? {
        return DataBindingUtil.setContentView(this, layoutId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleCitySelection(requestCode, resultCode, data)
    }

    override fun onCreated(savedInstanceState: Bundle?) {
        actionBar?.title = resources.getString(R.string.action_bar_title_landing)
        val binding = viewBinding()
        binding?.listener = this
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(LandingActivityViewModel::class.java)
        handleRouting()
    }

    override fun onResume() {
        super.onResume()
        updateDisplayData()
    }

    private fun updateDisplayData() {
        val isGranted = isLocationPermissionGranted()
        val messageId =
            if (isGranted) R.string.text_view_permission_granted else R.string.text_view_permission_not_granted
        viewModel.setPermissionMessage(resources.getString(messageId))
        viewModel.setIsPermissionGranted(isGranted)
        val binding = viewBinding()
        binding?.viewModel = viewModel
    }

    private fun handleRouting() {
        if (!isLocationPermissionGranted()) {
            requestLocationPermission()
        } else {
            goToMapActivity()
        }
    }

    override fun requestLocationPermission() {
        val rxPermission = RealRxPermission.getInstance(application)
        val disposable = rxPermission
            .requestEach(Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe({
                updateDisplayData()
                handlePermissionResult(it)
            }, { it?.printStackTrace() })
        compositeDisposable.add(disposable)
    }

    private fun isLocationPermissionGranted(): Boolean {
        return RealRxPermission.getInstance(application).isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun handlePermissionResult(granted: Permission) {
        when (granted.state()) {
            Permission.State.GRANTED -> {
                goToMapActivity()
            }
            Permission.State.DENIED -> {
                //TODO: On denied
            }
            Permission.State.DENIED_NOT_SHOWN, Permission.State.REVOKED_BY_POLICY -> {
                showAppSettingsLauncherDialog()
            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun showAppSettingsLauncherDialog() {
        val alertDialog = Dialogs.create(context = this,
            message = getString(R.string.dialog_location_permission_show_settings_message),
            title = getString(R.string.dialog_location_permission_show_settings_title),
            negativeButton = getString(R.string.dialog_negative_button_title),
            positiveButton = getString(R.string.dialog_positive_button_title),
            positiveAction = { goToAppSettings() })

        alertDialog?.show()
    }

    private fun goToAppSettings() {
        val appSettingsIntent = settingsStarterIntent()
        appSettingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(this, appSettingsIntent, null)
    }

    private fun handleCitySelection(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = CityPickerActivity.handleActivityResult(requestCode, resultCode, data) ?: return
        toast(this, "[cityCode: ${result.cityCode}  ||| countryCode: ${result.countryCode}]")
    }

    override fun goToMapActivity() {
        val intent = MainActivity.intent(this)
        startActivity(intent)
    }

    override fun goToCountryPicker() {
        val intent = CityPickerActivity.intent(this)
        startActivityForResult(intent, CityPickerActivity.SELECTION_REQUEST_CODE)
    }
}

interface ViewListener {
    fun requestLocationPermission()
    fun goToCountryPicker()
    fun goToMapActivity()
}
