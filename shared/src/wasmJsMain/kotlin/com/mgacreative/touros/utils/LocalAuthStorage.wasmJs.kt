package com.mgacreative.touros.utils

import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json

actual object LocalAuthStorage {
    private const val KEY = "touros_auth_credentials"
    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveCredentials(credentials: SavedAuthCredentials) {
        runCatching {
            val content = json.encodeToString(SavedAuthCredentials.serializer(), credentials)
            localStorage.setItem(KEY, content)
        }
    }

    actual fun loadCredentials(): SavedAuthCredentials? {
        return runCatching {
            val content = localStorage.getItem(KEY)
            if (!content.isNullOrBlank()) {
                json.decodeFromString(SavedAuthCredentials.serializer(), content)
            } else null
        }.getOrNull()
    }

    actual fun clearCredentials() {
        runCatching {
            localStorage.removeItem(KEY)
        }
    }
}
