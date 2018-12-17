package com.shafic.challenge.common.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import com.shafic.challenge.R

class Dialogs {
    companion object {
        fun createNeutral(
            context: Context?,
            neutralAction: (() -> Unit)? = null,
            title: String? = null,
            message: String?,
            neutralButton: String? = context?.getString(R.string.ok)
        ): AlertDialog? {
            val emptyNeutralAction: (() -> Unit)? = if (neutralAction == null) {
                {
                    //EMPTY BLOCK
                }
            } else null

            return create(
                context,
                neutralButton = neutralButton,
                title = title,
                message = message,
                neutralAction = neutralAction ?: emptyNeutralAction
            )
        }

        fun createDefault(
            context: Context?,
            positiveAction: (() -> Unit)? = null,
            negativeAction: (() -> Unit)? = null,
            title: String? = null,
            message: String?,
            positiveButton: String? = context?.getString(R.string.ok),
            negativeButton: String? = context?.getString(R.string.cancel)
        ): AlertDialog? {

            val emptyNegativeAction: (() -> Unit)? = if (negativeAction == null) {
                {
                    //EMPTY BLOCK
                }
            } else null
            val emptyPositiveAction: (() -> Unit)? = if (positiveAction == null) {
                {
                    //EMPTY BLOCK
                }
            } else null

            return create(
                context,
                positiveAction = positiveAction ?: emptyPositiveAction,
                negativeAction = negativeAction ?: emptyNegativeAction,
                positiveButton = positiveButton,
                negativeButton = negativeButton,
                title = title,
                message = message
            )
        }

        private fun create(
            context: Context?,
            positiveAction: (() -> Unit)? = null,
            negativeAction: (() -> Unit)? = null,
            neutralAction: (() -> Unit)? = null,
            title: String? = null,
            message: String?,
            positiveButton: String? = context?.getString(R.string.ok),
            negativeButton: String? = context?.getString(R.string.cancel),
            neutralButton: String? = context?.getString(R.string.ok)
        ): AlertDialog? {

            val context = context ?: return null

            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.setTitle(title)
            alertDialog.setMessage(message)

            if (positiveAction != null) {
                alertDialog.setButton(
                    AlertDialog.BUTTON_POSITIVE, positiveButton
                ) { _, _ -> positiveAction() }
            }

            if (negativeAction != null) {
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeButton) { _, _ ->
                    negativeAction()
                }
            }

            if (neutralAction != null) {
                alertDialog.setButton(
                    AlertDialog.BUTTON_NEUTRAL, neutralButton
                ) { _, _ ->
                    neutralAction()
                }
            }
            return alertDialog
        }
    }
}
