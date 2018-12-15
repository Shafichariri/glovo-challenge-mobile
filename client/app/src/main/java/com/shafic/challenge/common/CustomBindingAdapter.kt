package com.shafic.challenge.common

import android.databinding.BindingAdapter
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
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
    }
}