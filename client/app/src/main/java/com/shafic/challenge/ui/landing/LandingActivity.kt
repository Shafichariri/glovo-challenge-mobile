package com.shafic.challenge.ui.landing

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.shafic.challenge.R
import com.shafic.challenge.common.settingsStarterIntent
import com.shafic.challenge.ui.map.MainActivity
import com.vanniktech.rxpermission.Permission
import com.vanniktech.rxpermission.RealRxPermission
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.CompositeDisposable


class LandingActivity : AppCompatActivity() {

    @NonNull
    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        handleRouting()
    }

    private fun handleRouting() {
        if (!isLocationPermissionGranted()) {
            requestLocationPermission()
        } else {
            val intent = MainActivity.intent(this)
            startActivity(intent)
        }
    }

    private fun requestLocationPermission() {
        val rxPermission = RealRxPermission.getInstance(application)
        val disposable = rxPermission
            .requestEach(Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe({ handlePermissionResult(it) }, { it?.printStackTrace() })
        compositeDisposable.add(disposable)
    }

    private fun isLocationPermissionGranted(): Boolean {
        return RealRxPermission.getInstance(application).isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun handlePermissionResult(granted: Permission) {
        when (granted.state()) {
            Permission.State.GRANTED -> {
                //TODO: Go to
            }
            Permission.State.DENIED -> {
                Log.d("SHAFIC", "DENIED")
            }
            Permission.State.DENIED_NOT_SHOWN, Permission.State.REVOKED_BY_POLICY -> {
                Log.d("SHAFIC", "DENIED_NOT_SHOWN || REVOKED_BY_POLICY")
                showAppSettingsLauncherDialog()
            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun showAppSettingsLauncherDialog() {
        val alertDialog = AlertDialog.Builder(this).create()
        val positiveButtonTitle = getString(R.string.dialog_positive_button_title)
        val negativeButtonTitle = getString(R.string.dialog_negative_button_title)
        alertDialog.setTitle(getString(R.string.dialog_location_permission_show_settings_title))
        alertDialog.setMessage(getString(R.string.dialog_location_permission_show_settings_message))
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, positiveButtonTitle
        ) { _, _ -> goToAppSettings() }

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeButtonTitle, Message())

        alertDialog.show()
    }

    private fun goToAppSettings() {
        val appSettingsIntent = settingsStarterIntent()
        appSettingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(this, appSettingsIntent, null)
    }
}
