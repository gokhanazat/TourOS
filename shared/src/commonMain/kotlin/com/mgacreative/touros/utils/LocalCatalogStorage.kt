package com.mgacreative.touros.utils

/**
 * Katalog Ürün Yönetimi için Cross-Platform Yerel Önbellek Depolama Arayüzü.
 * Web (LocalStorage), Desktop/Android (Disk File), iOS (NSUserDefaults) üzerinde
 * verilerin silinmeden saklanmasını sağlar.
 */
expect object LocalCatalogStorage {
    fun saveCatalogJson(jsonContent: String)
    fun loadCatalogJson(): String?
    fun clearCatalogJson()
}
