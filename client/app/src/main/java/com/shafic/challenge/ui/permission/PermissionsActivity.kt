package com.shafic.challenge.ui.permission

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.shafic.challenge.R
import com.shafic.challenge.common.Dialogs
import com.shafic.challenge.common.base.AbstractBaseActivity
import com.shafic.challenge.databinding.ActivityPermissionsBinding
import com.shafic.challenge.injection.ViewModelFactory
import com.shafic.challenge.navigation.coordinators.MainFlowCoordinator
import com.vanniktech.rxpermission.Permission
import com.vanniktech.rxpermission.RealRxPermission
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.CompositeDisposable

class PermissionsActivity : AbstractBaseActivity<ActivityPermissionsBinding>() {
    companion object {
        const val PERMISSIONS_REQUEST_CODE = 34535
        const val GRANTED_RESULT_CODE = 113453
        const val DENIED_RESULT_CODE = 113452
        const val FAILED_RESULT_CODE = 113451

        private val TAG = PermissionsActivity::class.java.simpleName

        private fun shouldHandleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
            return PermissionsActivity.PERMISSIONS_REQUEST_CODE == requestCode
        }

        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean? {
            if (!shouldHandleActivityResult(requestCode, resultCode, data)) {
                return null
            }
            //Return TRUE if permission is granted
            return resultCode == GRANTED_RESULT_CODE
        }
        //TODO: Pass In Permission name arguments
        fun intent(context: Context): Intent = Intent(context, PermissionsActivity::class.java)
    }

    @NonNull
    private val compositeDisposable = CompositeDisposable()
    private lateinit var viewModel: PermissionsViewModel

    override val layoutId: Int
        get() = R.layout.activity_permissions

    override fun onCreateViewDataBinding(savedInstanceState: Bundle?): ActivityPermissionsBinding? {
        return DataBindingUtil.setContentView(this, layoutId)
    }

    override fun onCreated(savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(PermissionsViewModel::class.java)
        viewModel.setFlowCoordinator(MainFlowCoordinator())
        startRequest()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun startRequest() {
        if (!isLocationPermissionGranted()) {
            requestLocationPermission()
        } else {
            setResult(GRANTED_RESULT_CODE)
            finish()
        }
    }

    private fun requestLocationPermission() {
        val rxPermission = RealRxPermission.getInstance(application)
        val disposable = rxPermission
            .requestEach(Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe({
                handlePermissionResult(it)
            }, {
                it?.printStackTrace()
                viewModel.finishPermissionWithResult(FAILED_RESULT_CODE)
            })
        compositeDisposable.add(disposable)
    }

    private fun isLocationPermissionGranted(): Boolean {
        return RealRxPermission.getInstance(application).isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun handlePermissionResult(granted: Permission) {
        //TODO: Use extras to handle multiple permissions requests
        when (granted.state()) {
            Permission.State.GRANTED -> {
                viewModel.finishPermissionWithResult(GRANTED_RESULT_CODE)
            }
            Permission.State.DENIED -> {
                viewModel.finishPermissionWithResult(DENIED_RESULT_CODE)
            }
            Permission.State.DENIED_NOT_SHOWN, Permission.State.REVOKED_BY_POLICY -> {
                showAppSettingsLauncherDialog()
            }
        }

    }

    private fun showAppSettingsLauncherDialog() {
        //TODO: Delegate message and title (also Parameters to intent) 
        val alertDialog = Dialogs.createDefault(context = this,
            message = getString(R.string.dialog_location_permission_show_settings_message),
            title = getString(R.string.dialog_location_permission_show_settings_title),
            negativeButton = getString(R.string.dialog_negative_button_title),
            positiveButton = getString(R.string.dialog_positive_button_title),
            positiveAction = { viewModel.goToSettings() },
            negativeAction = {
                viewModel.finishPermissionWithResult(DENIED_RESULT_CODE)
            })

        alertDialog?.show()
    }
}
