package com.shafic.challenge.common.util

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import com.shafic.challenge.common.tint
import com.shafic.challenge.injection.module.app

class Util {
    companion object {

        fun formatString(@StringRes stringRes: Int, arg: String): String {
            val context = app().context
            return context.getString(stringRes, arg)
        }

        fun stringOrDefualt(value: String?, @StringRes default: Int): String {
            if (value.isNullOrBlank()) {
                val context = app().context
                context.getString(default)
            }
            return value ?: ""
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap {
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
            return bitmap
        }

        private fun getBitmap(vectorDrawable: VectorDrawableCompat): Bitmap {
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
            return bitmap
        }

        fun loadBitmapFromVector(
            context: Context, vectorDrawableId: Int, @ColorRes tintColorResId: Int? =
                null
        ): Bitmap {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                var drawable: VectorDrawableCompat? =
                    VectorDrawableCompat.create(context.resources, vectorDrawableId, context.resources.newTheme())
                drawable?.tint(context, tintColorResId)
                if (drawable is VectorDrawableCompat) {
                    return Util.getBitmap(vectorDrawable = drawable)
                } else if (drawable is BitmapDrawable) {
                    return drawable.bitmap
                }
            } else {
                var drawable: Drawable? = ContextCompat.getDrawable(context, vectorDrawableId)
                drawable?.tint(context, tintColorResId)
                if (drawable is VectorDrawable) {
                    return Util.getBitmap(vectorDrawable = drawable)
                }
            }
            throw IllegalArgumentException("unsupported drawable type")
        }
    }
}
