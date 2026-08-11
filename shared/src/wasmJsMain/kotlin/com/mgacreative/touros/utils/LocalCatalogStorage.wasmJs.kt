package com.mgacreative.touros.utils

import kotlinx.browser.localStorage

actual object LocalCatalogStorage {
    private const val KEY = "touros_catalog_cache"

    actual fun saveCatalogJson(jsonContent: String) {
        runCatching {
            localStorage.setItem(KEY, jsonContent)
        }
    }

    actual fun loadCatalogJson(): String? {
        return runCatching {
            localStorage.getItem(KEY)
        }.getOrNull()
    }

    actual fun clearCatalogJson() {
        runCatching {
            localStorage.removeItem(KEY)
        }
    }
}
