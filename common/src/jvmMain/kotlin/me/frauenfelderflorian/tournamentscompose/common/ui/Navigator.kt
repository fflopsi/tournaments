package me.frauenfelderflorian.tournamentscompose.common.ui

import moe.tlaster.precompose.navigation.Navigator

actual class Navigator actual constructor(actual val controller: Any) {
    init {
        if (controller !is Navigator) {
            throw TypeCastException("controller needs to be of type Navigator")
        }
    }

    actual fun navigate(route: Routes) {
        (controller as Navigator).navigate(route.route)
    }

    actual fun navigateUp() {
        (controller as Navigator).goBack()
    }
}
