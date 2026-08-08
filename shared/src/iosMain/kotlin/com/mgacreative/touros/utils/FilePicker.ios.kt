package com.mgacreative.touros.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberFilePickerLauncher(
    mimeType: String,
    onFileSelected: (fileName: String, bytes: ByteArray) -> Unit
): () -> Unit {
    return remember(mimeType, onFileSelected) {
        {
            // iOS file picker stub
        }
    }
}
