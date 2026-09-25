package com.mgacreative.touros

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mgacreative.touros.di.initKoin

fun main() {
    initKoin()

    application {
        val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
        Window(
            onCloseRequest = ::exitApplication,
            title = "Axileto",
            state = windowState,
        ) {
            App()
        }
    }
}
