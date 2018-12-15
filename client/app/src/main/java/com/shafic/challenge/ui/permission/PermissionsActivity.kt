package com.shafic.challenge.ui.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.shafic.challenge.R
import com.shafic.challenge.common.Dialogs
import com.shafic.challenge.common.base.AbstractBaseActivity
import com.shafic.challenge.common.base.BaseViewModel
import com.shafic.challenge.common.settingsStarterIntent
import com.shafic.challenge.databinding.ActivityPermissionsBinding
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
            val data = data ?: return null
            if (!shouldHandleActivityResult(requestCode, resultCode, data)) {
                return null
            }
            //Return TRUE if permission is granted
            return resultCode == GRANTED_RESULT_CODE
        }

        fun intent(context: Context): Intent = Intent(context, PermissionsActivity::class.java)
    }

    @NonNull
    private val compositeDisposable = CompositeDisposable()

    override val layoutId: Int
        get() = R.layout.activity_permissions

    override fun onCreateViewDataBinding(savedInstanceState: Bundle?): ActivityPermissionsBinding? {
        return DataBindingUtil.setContentView(this, layoutId)
    }

    override fun onCreated(savedInstanceState: Bundle?) {
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
                setResult(FAILED_RESULT_CODE)
                finish()
            })
        compositeDisposable.add(disposable)
    }

    private fun isLocationPermissionGranted(): Boolean {
        return RealRxPermission.getInstance(application).isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun handlePermissionResult(granted: Permission) {
        when (granted.state()) {
            Permission.State.GRANTED -> {
                //TODO: Use extras to handle multiple permissions requests
                setResult(GRANTED_RESULT_CODE)
                finish()
            }
            Permission.State.DENIED -> {
                //TODO: On denied
                setResult(DENIED_RESULT_CODE)
                finish()
            }
            Permission.State.DENIED_NOT_SHOWN, Permission.State.REVOKED_BY_POLICY -> {
                showAppSettingsLauncherDialog()
            }
        }

    }

    private fun showAppSettingsLauncherDialog() {
        val alertDialog = Dialogs.createDefault(context = this,
            message = getString(R.string.dialog_location_permission_show_settings_message),
            title = getString(R.string.dialog_location_permission_show_settings_title),
            negativeButton = getString(R.string.dialog_negative_button_title),
            positiveButton = getString(R.string.dialog_positive_button_title),
            positiveAction = { goToAppSettings() },
            negativeAction = {
                setResult(DENIED_RESULT_CODE)
                finish()
            })

        alertDialog?.show()
    }

    private fun goToAppSettings() {
        val appSettingsIntent = settingsStarterIntent()
        appSettingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(this, appSettingsIntent, null)
    }
}

class PermissionsViewModel : BaseViewModel() {

}
