package com.shafic.challenge.navigation

import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.shafic.challenge.common.settingsStarterIntent
import com.shafic.challenge.common.simpleClassName
import com.shafic.challenge.ui.cityPicker.CityPickerActivity
import com.shafic.challenge.ui.map.MainActivity
import com.shafic.challenge.ui.permission.PermissionsActivity
import java.lang.ref.WeakReference

/**
 * Prototype:
 * Navigator is responsible for handling the step in the navigation process only.
 *
 * A better implementation would be to lose the map<String,weakRef> (with add and remove)
 * and instead have the class be instantiated with an [weakRef]Activity (and destroyed using live data lifecycle-aware components)
 * whenever an activity is started, so each of our activities would have it's own navigation-coordinator.
 * */
object Navigator : NavigationProvider {
    private val TAG = Navigator.javaClass.simpleName

    private var weakActivity: WeakReference<AppCompatActivity>? = null
        get() = map[currentActivityKey]

    private var name: String = ""
        get() = map[currentActivityKey]?.get()?.javaClass?.simpleName ?: "NO-NAME"

    private var map: MutableMap<String, WeakReference<AppCompatActivity>?> = mutableMapOf()

    private var currentActivityKey: String? = null


    fun add(weakActivityReference: WeakReference<AppCompatActivity>) {
        val activity = weakActivityReference.get() ?: return
        val key = activity.simpleClassName()
        map[key] = weakActivityReference
        currentActivityKey = key
    }

    fun remove(weakActivityReference: WeakReference<AppCompatActivity>) {
        val activity = weakActivityReference.get() ?: return
        val key = activity.simpleClassName()
        map.remove(key)
    }

    override fun showMap() {
        val activity = weakActivity?.get() ?: return handleNoActivityReference()
        val intent = MainActivity.intent(activity)
        activity.startActivity(intent)
    }

    override fun showCityPicker(requestCode: Int) {
        val activity = weakActivity?.get() ?: return handleNoActivityReference()
        val intent = CityPickerActivity.intent(activity)
        activity.startActivityForResult(intent, requestCode)
    }

    override fun showPermissionHandler(requestCode: Int) {
        val activity = weakActivity?.get() ?: return handleNoActivityReference()
        val intent = PermissionsActivity.intent(activity)
        activity.startActivityForResult(intent, requestCode)
    }

    override fun finishPermissionHandler(resultCode: Int) {
        val activity = weakActivity?.get() ?: return handleNoActivityReference()
        activity.setResult(resultCode)
        activity.finish()
    }

    override fun goToAppSettings() {
        val activity = weakActivity?.get() ?: return handleNoActivityReference()

        val appSettingsIntent = activity.settingsStarterIntent()
        appSettingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(activity, appSettingsIntent, null)
    }

    private fun handleNoActivityReference() {
        if (weakActivity == null) {
            Log.e(TAG, "WEAK-REFERENCE of Activity is Null [KEY: $currentActivityKey]")
            throw RuntimeException("WEAK-REFERENCE of Activity is Null")
        } else if (weakActivity?.get() == null) {
            Log.e(TAG, "Activity REFERENCEd weakly is Null [KEY: $currentActivityKey]")
            throw RuntimeException("WEAK-REFERENCE of Activity is Null")
        }
    }
}

interface NavigationProvider {
    fun showMap()
    fun showCityPicker(requestCode: Int)
    fun showPermissionHandler(requestCode: Int)
    fun finishPermissionHandler(resultCode: Int)
    fun goToAppSettings()
}
