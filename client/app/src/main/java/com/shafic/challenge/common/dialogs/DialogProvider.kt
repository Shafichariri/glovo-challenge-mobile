package com.shafic.challenge.common.dialogs

import android.support.v7.app.AlertDialog
import com.shafic.challenge.data.models.City

interface DialogProvider {

    fun createNetworkErrorDialog(positiveAction: (() -> Unit)): AlertDialog?
    fun createNeedsPermissionAlert(neutralAction: (() -> Unit)): AlertDialog?
    fun createCityPickerDialog(positiveAction: (() -> Unit)): AlertDialog?
    fun createGeoCoderErrorDialog(message: String?): AlertDialog?
    fun createAlertSelectionValidation(
        city: City,
        positiveAction: (() -> Unit),
        negativeAction: (() -> Unit)
    ): AlertDialog?
    fun createAppSettingsLauncherDialog(
        positiveAction: (() -> Unit),
        negativeAction: (() -> Unit)
    ): AlertDialog?
}
