package com.shafic.challenge.ui.permission

import com.shafic.challenge.common.base.BaseViewModel
import com.shafic.challenge.navigation.coordinators.MainFlowProvider


class PermissionsViewModel : BaseViewModel() {
    private var flow: MainFlowProvider? = null

    fun setFlowCoordinator(flow: MainFlowProvider) {
        this.flow = flow
    }

    fun finishPermissionWithResult(resultCode: Int) {
        flow?.closePermissionHandler(resultCode)
    }

    fun goToSettings() {
        flow?.goToAppSettings()
    }
}
