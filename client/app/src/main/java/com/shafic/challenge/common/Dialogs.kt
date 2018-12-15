package com.shafic.challenge.common

import android.content.Context
import android.support.v7.app.AlertDialog
import com.shafic.challenge.R

class Dialogs {
    companion object {
        fun create(
            context: Context?,
            positiveAction: (() -> Unit)? = null,
            negativeAction: (() -> Unit)? = null,
            title: String? = null,
            message: String?,
            positiveButton: String? = context?.getString(R.string.ok),
            negativeButton: String? = context?.getString(R.string.cancel)
        ): AlertDialog? {

            val context = context ?: return null

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.setTitle(title)
            alertDialog.setMessage(message)
            alertDialog.setButton(
                AlertDialog.BUTTON_POSITIVE, positiveButton
            ) { _, _ -> positiveAction?.invoke() }

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeButton) { _, _ ->
                negativeAction?.invoke()
            }

            return alertDialog
        }
    }
}
