package com.mgacreative.touros

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.mgacreative.touros.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        initKoin()
    } catch (e: Throwable) {
        // Prevent web initialization crash if Koin is already started or encounters startup exception
    }

    ComposeViewport {
        App()
    }
}