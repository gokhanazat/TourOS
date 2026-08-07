package com.mgacreative.touros

import androidx.compose.ui.window.ComposeUIViewController
import com.mgacreative.touros.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}