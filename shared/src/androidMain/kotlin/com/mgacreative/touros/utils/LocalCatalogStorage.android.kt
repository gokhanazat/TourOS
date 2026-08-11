package com.mgacreative.touros.utils

import java.io.File

actual object LocalCatalogStorage {
    private const val FILE_NAME = "touros_catalog_cache.json"

    private fun getCacheFile(): File {
        val baseDir = File(System.getProperty("user.home") ?: System.getProperty("java.io.tmpdir") ?: ".")
        return File(baseDir, FILE_NAME)
    }

    actual fun saveCatalogJson(jsonContent: String) {
        runCatching {
            getCacheFile().writeText(jsonContent)
        }
    }

    actual fun loadCatalogJson(): String? {
        return runCatching {
            val file = getCacheFile()
            if (file.exists() && file.length() > 0) file.readText() else null
        }.getOrNull()
    }

    actual fun clearCatalogJson() {
        runCatching {
            val file = getCacheFile()
            if (file.exists()) file.delete()
        }
    }
}
