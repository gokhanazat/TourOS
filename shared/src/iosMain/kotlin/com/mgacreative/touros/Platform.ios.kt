package com.mgacreative.touros

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun getCurrentEpochMillis(): Long = (platform.Foundation.NSDate().timeIntervalSince1970 * 1000.0).toLong()
actual fun getWebQueryParameter(key: String): String? = null
actual fun getWebAgencyIdentifier(): String? = null
actual fun clearWebAgencyQueryFromUrl() {}
actual fun setWebDocumentTitle(title: String) {}