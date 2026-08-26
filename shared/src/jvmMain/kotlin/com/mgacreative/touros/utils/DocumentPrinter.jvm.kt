package com.mgacreative.touros.utils

import java.awt.Desktop
import java.io.File

actual object DocumentPrinter {
    actual fun printOrSaveHtml(htmlContent: String, title: String) {
        try {
            val safeTitle = title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val tempFile = File.createTempFile("touros_${safeTitle}_", ".html")
            tempFile.writeText(htmlContent, Charsets.UTF_8)
            tempFile.deleteOnExit()
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(tempFile.toURI())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
