package com.mgacreative.touros.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun rememberFilePickerLauncher(
    mimeType: String,
    onFileSelected: (fileName: String, bytes: ByteArray) -> Unit
): () -> Unit {
    return remember(mimeType, onFileSelected) {
        {
            try {
                val dialog = FileDialog(null as Frame?, "Görsel Seç", FileDialog.LOAD)
                dialog.isVisible = true
                val file = dialog.file
                val directory = dialog.directory
                if (file != null && directory != null) {
                    val targetFile = File(directory, file)
                    if (targetFile.exists() && targetFile.isFile) {
                        val bytes = targetFile.readBytes()
                        onFileSelected(targetFile.absolutePath, bytes)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
actual fun Modifier.onFileDrop(onFilesDropped: (List<String>) -> Unit): Modifier = this
