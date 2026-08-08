package com.mgacreative.touros.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberFilePickerLauncher(
    mimeType: String,
    onFileSelected: (fileName: String, bytes: ByteArray) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileNameFromUri(context, uri) ?: "image.jpg"
            val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes()
            }
            if (bytes != null && bytes.isNotEmpty()) {
                onFileSelected(fileName, bytes)
            }
        }
    }

    return { launcher.launch(mimeType) }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            cursor.getString(nameIndex)
        } else null
    }
}
