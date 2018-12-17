package com.shafic.challenge.common.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import com.shafic.challenge.R
import com.shafic.challenge.data.models.City


class DialogProviderImplementation(val context: Context) : DialogProvider {

    override fun createNetworkErrorDialog(positiveAction: (() -> Unit)): AlertDialog? {
        val title = context.getString(R.string.error)
        val unknownError = context.getString(R.string.server_not_reachable)
        return Dialogs.createDefault(context, title = title, message = unknownError, positiveAction = positiveAction)
    }

    override fun createNeedsPermissionAlert(neutralAction: () -> Unit): AlertDialog? {
        val title = context.getString(R.string.dialog_location_permission_lost_show_title)
        val message = context.getString(R.string.dialog_location_permission_lost_show_message)
        return Dialogs.createNeutral(
            context, title = title, message = message,
            neutralAction = neutralAction
        )
    }

    override fun createCityPickerDialog(positiveAction: () -> Unit): AlertDialog? {
        val title = context.getString(R.string.dialog_city_picker_title)
        val message = context.getString(R.string.dialog_city_picker_message)
        return Dialogs.createDefault(
            context, title = title, message = message,
            positiveAction = positiveAction
        )
    }

    override fun createGeoCoderErrorDialog(message: String?): AlertDialog? {
        val title = context.getString(R.string.dialog_geocoder_failed_title)
        val unknownError = context.getString(R.string.dialog_geocoder_failed_message)
        return Dialogs.createNeutral(context, title = title, message = message ?: unknownError)

    }

    override fun createAlertSelectionValidation(
        city: City, positiveAction: () -> Unit, negativeAction: () -> Unit
    ): AlertDialog? {
        return Dialogs.createDefault(
            context = context,
            message = context.getString(R.string.dialog_city_selection_message, city.name),
            title = context.getString(R.string.dialog_city_selection_title),
            negativeAction = negativeAction,
            positiveAction = positiveAction
        )
    }

    override fun createAppSettingsLauncherDialog(positiveAction: () -> Unit, negativeAction: () -> Unit): AlertDialog? {
        return Dialogs.createDefault(
            context = context,
            message = context.getString(R.string.dialog_location_permission_show_settings_message),
            title = context.getString(R.string.dialog_location_permission_show_settings_title),
            negativeButton = context.getString(R.string.dialog_negative_button_title),
            positiveButton = context.getString(R.string.dialog_positive_button_title),
            positiveAction = positiveAction,
            negativeAction = negativeAction
        )
    }
}
