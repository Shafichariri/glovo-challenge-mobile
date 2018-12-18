package com.shafic.challenge.common

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.databinding.BindingAdapter
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatImageView
import android.view.View


class CustomBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter(value = "visibleIf")
        fun setVisibleIf(view: View, visible: Boolean) {
            view.visibility = if (visible) View.VISIBLE else View.GONE
        }

        @JvmStatic
        @BindingAdapter("colorTint")
        fun setColorTint(view: AppCompatImageView, @ColorRes color: Int) {
            DrawableCompat.setTint(view.drawable, ContextCompat.getColor(view.context, color))
        }


        @JvmStatic
        @BindingAdapter("elevation")
        fun setViewElevation(view: AppCompatImageView, @DimenRes dimenRes: Int) {
            ViewCompat.setElevation(view, view.resources.getDimension(dimenRes))
        }

        @JvmStatic
        @BindingAdapter(value = "visibleIfLiveData")
        fun setVisibleIfLiveData(view: View, visible: LiveData<Boolean>) {
            val activity = view.parentActivity() ?: return
            visible.observe(activity, Observer {
                view.visibility = if (it == true) View.VISIBLE else View.GONE
            })
        }
        
    }
}
