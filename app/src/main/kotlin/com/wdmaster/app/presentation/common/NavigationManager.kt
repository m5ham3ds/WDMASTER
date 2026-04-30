package com.wdmaster.app.presentation.common

import androidx.navigation.NavController
import androidx.navigation.NavOptions

object NavigationManager {

    fun navigateTo(navController: NavController, route: String, popUpTo: String? = null) {
        val builder = NavOptions.Builder()
        popUpTo?.let {
            builder.setPopUpTo(it, true)
        }
        navController.navigate(route, builder.build())
    }
}