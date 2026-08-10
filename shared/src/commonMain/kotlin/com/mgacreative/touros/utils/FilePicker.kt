package com.mgacreative.touros.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

const val MAX_IMAGE_SIZE_BYTES = 1024 * 1024 // 1 MB (1,048,576 bytes) limit

@Composable
expect fun rememberFilePickerLauncher(
    mimeType: String = "image/*",
    onFileSelected: (fileName: String, bytes: ByteArray) -> Unit
): () -> Unit

@Composable
expect fun Modifier.onFileDrop(onFilesDropped: (List<String>) -> Unit): Modifier
