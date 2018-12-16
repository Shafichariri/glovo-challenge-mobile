package com.shafic.challenge.common

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import android.widget.Toast.makeText
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion

//region ACTIVITY EXTENSIONS
fun AppCompatActivity.toast(context: Context, @StringRes strId: Int, length: Int = Toast.LENGTH_SHORT) {
    makeText(context, strId, length).show()
}

fun AppCompatActivity.toast(context: Context, msg: String, length: Int = Toast.LENGTH_SHORT) {
    makeText(context, msg, length).show()
}

fun AppCompatActivity.isOnMainThread(): Boolean {
    return Looper.myLooper() == Looper.getMainLooper()
}

fun AppCompatActivity.settingsStarterIntent(): Intent {
    return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        .addCategory(Intent.CATEGORY_DEFAULT)
}

fun AppCompatActivity.simpleClassName(): String {
    return this.javaClass.simpleName
}

//endregion

//region VIEW EXTENSION
fun View.parentActivity(): AppCompatActivity? {
    return this.context as? AppCompatActivity
}
//endregion

//region MAP MODEL EXTENSIONS
fun VisibleRegion.polygon(): List<LatLng> {
    return arrayListOf(
        nearLeft, farLeft,
        farLeft, farRight,
        farRight, nearRight,
        nearRight, nearLeft
    )
}
//endregion

//region Bitmap and DrawableZ 
fun Bitmap.getDescriptor(): BitmapDescriptor {
    return BitmapDescriptorFactory.fromBitmap(this)
}

fun Drawable.tint(context: Context, @ColorRes tintColorResId: Int? = null) {
    if (tintColorResId != null) {
        //Apply Tint If Color is Available
        val drawable = DrawableCompat.wrap(this)
        DrawableCompat.setTint(drawable.mutate(), ContextCompat.getColor(context, tintColorResId))
    }
}
//endregion
