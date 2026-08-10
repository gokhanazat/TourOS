package com.mgacreative.touros.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

@Composable
actual fun rememberFilePickerLauncher(
    mimeType: String,
    onFileSelected: (fileName: String, bytes: ByteArray) -> Unit
): () -> Unit {
    return remember(mimeType, onFileSelected) {
        {
            val fileInput = document.createElement("input") as HTMLInputElement
            fileInput.type = "file"
            fileInput.accept = mimeType
            fileInput.onchange = { _ ->
                val file = fileInput.files?.item(0)
                if (file != null) {
                    val reader = FileReader()
                    reader.onload = { _ ->
                        val result = reader.result
                        if (result != null) {
                            val arrayBuffer = result as ArrayBuffer
                            val uint8Array = Uint8Array(arrayBuffer)
                            val byteArray = ByteArray(uint8Array.length) { i -> uint8Array[i] }
                            onFileSelected(file.name, byteArray)
                        }
                    }
                    reader.readAsArrayBuffer(file)
                }
            }
            fileInput.click()
        }
    }
}

@Composable
actual fun Modifier.onFileDrop(onFilesDropped: (List<String>) -> Unit): Modifier = this
