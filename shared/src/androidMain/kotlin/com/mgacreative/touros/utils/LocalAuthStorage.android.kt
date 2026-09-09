package com.mgacreative.touros.utils

import kotlinx.serialization.json.Json
import java.io.File

actual object LocalAuthStorage {
    private const val FILE_NAME = "touros_auth_credentials.json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private fun getCacheFile(): File {
        val baseDir = File(System.getProperty("user.home") ?: System.getProperty("java.io.tmpdir") ?: ".")
        return File(baseDir, FILE_NAME)
    }

    actual fun saveCredentials(credentials: SavedAuthCredentials) {
        runCatching {
            val content = json.encodeToString(SavedAuthCredentials.serializer(), credentials)
            getCacheFile().writeText(content)
        }
    }

    actual fun loadCredentials(): SavedAuthCredentials? {
        return runCatching {
            val file = getCacheFile()
            if (file.exists() && file.length() > 0) {
                json.decodeFromString(SavedAuthCredentials.serializer(), file.readText())
            } else null
        }.getOrNull()
    }

    actual fun clearCredentials() {
        runCatching {
            val file = getCacheFile()
            if (file.exists()) file.delete()
        }
    }
}
