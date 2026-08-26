package com.mgacreative.touros.utils

import kotlinx.browser.window

actual object DocumentPrinter {
    actual fun printOrSaveHtml(htmlContent: String, title: String) {
        runCatching {
            val win = window.open("", "_blank")
            win?.document?.write(htmlContent)
            win?.document?.close()
            win?.focus()
        }
    }
}
