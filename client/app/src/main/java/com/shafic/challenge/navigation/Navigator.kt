package com.shafic.challenge.navigation

import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.shafic.challenge.common.simpleClassName
import com.shafic.challenge.ui.cityPicker.CityPickerActivity
import com.shafic.challenge.ui.map.MainActivity
import com.shafic.challenge.ui.permission.PermissionsActivity
import java.lang.ref.WeakReference

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
        Log.e(TAG, "[ADD] [$key] | [ACTIVE] [$name] --------->")
        map[key] = weakActivityReference
        currentActivityKey = key
    }

    fun remove(weakActivityReference: WeakReference<AppCompatActivity>) {
        val activity = weakActivityReference.get() ?: return
        val key = activity.simpleClassName()
        Log.e(TAG, "[REMOVE] [$key]  | [ACTIVE] [$name] --------->")
        map.remove(key)
    }

    override fun showMap() {
        val activity = weakActivity?.get() ?: return handleNoActivityReference()
        
        Log.e(TAG, "SHOW MAP ===> Using [$name]")
        
        val intent = MainActivity.intent(activity)
        activity.startActivity(intent)
    }

    override fun showCityPicker(requestCode: Int) {
        val activity = weakActivity?.get() ?: return handleNoActivityReference()
        
        Log.e(TAG, "SHOW CITY PICKER ===> Using [$name]")
        
        val intent = CityPickerActivity.intent(activity)
        activity.startActivityForResult(intent, requestCode)
    }

    override fun showPermissionHandler(requestCode: Int) {
        val activity = weakActivity?.get() ?: return handleNoActivityReference()
        
        Log.e(TAG, "SHOW PERMISSION HANDLER ===> Using [$name]")
        
        val intent = PermissionsActivity.intent(activity)
        activity.startActivityForResult(intent, requestCode)
    }

    private fun handleNoActivityReference() {
        if (weakActivity == null) {
            Log.e(TAG, "WEAK-REFERENCE of Activity is Null")
            throw RuntimeException("WEAK-REFERENCE of Activity is Null")
        } else if (weakActivity?.get() == null) {
            Log.e(TAG, "Activity REFERENCEd weakly is Null")
            throw RuntimeException("WEAK-REFERENCE of Activity is Null")
        }
    }
}

//interface ActivityProvider {
//    var weakActivity: WeakReference<AppCompatActivity>?
//}

interface NavigationProvider {
    fun showMap()
    fun showCityPicker(requestCode: Int)
    fun showPermissionHandler(requestCode: Int)
}
