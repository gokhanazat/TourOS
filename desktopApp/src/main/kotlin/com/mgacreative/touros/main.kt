package com.mgacreative.touros

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mgacreative.touros.di.initKoin

fun main() {
    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "TourOS",
        ) {
            App()
        }
    }
}