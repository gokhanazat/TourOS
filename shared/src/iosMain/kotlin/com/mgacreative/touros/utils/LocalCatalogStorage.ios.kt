package com.mgacreative.touros.utils

import platform.Foundation.NSUserDefaults

actual object LocalCatalogStorage {
    private const val KEY = "touros_catalog_cache"

    actual fun saveCatalogJson(jsonContent: String) {
        runCatching {
            NSUserDefaults.standardUserDefaults.setObject(jsonContent, forKey = KEY)
        }
    }

    actual fun loadCatalogJson(): String? {
        return runCatching {
            NSUserDefaults.standardUserDefaults.stringForKey(KEY)
        }.getOrNull()
    }

    actual fun clearCatalogJson() {
        runCatching {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY)
        }
    }
}
