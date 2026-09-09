package com.mgacreative.touros.utils

import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual object LocalAuthStorage {
    private const val KEY = "touros_auth_credentials"
    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveCredentials(credentials: SavedAuthCredentials) {
        runCatching {
            val content = json.encodeToString(SavedAuthCredentials.serializer(), credentials)
            NSUserDefaults.standardUserDefaults.setObject(content, forKey = KEY)
        }
    }

    actual fun loadCredentials(): SavedAuthCredentials? {
        return runCatching {
            val content = NSUserDefaults.standardUserDefaults.stringForKey(KEY)
            if (!content.isNullOrBlank()) {
                json.decodeFromString(SavedAuthCredentials.serializer(), content)
            } else null
        }.getOrNull()
    }

    actual fun clearCredentials() {
        runCatching {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY)
        }
    }
}
