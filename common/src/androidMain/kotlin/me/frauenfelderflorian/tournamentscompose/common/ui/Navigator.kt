package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.navigation.NavController

actual class Navigator actual constructor(actual val controller: Any) {
    init {
        if (controller !is NavController) {
            throw TypeCastException("controller needs to be of type NavController")
        }
    }

    actual fun navigate(route: Routes) {
        (controller as NavController).navigate(route.route)
    }

    actual fun navigateUp() {
        (controller as NavController).navigateUp()
    }
}
