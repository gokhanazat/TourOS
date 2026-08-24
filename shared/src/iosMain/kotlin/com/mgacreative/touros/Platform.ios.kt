package com.mgacreative.touros

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun getCurrentEpochMillis(): Long = (platform.Foundation.NSDate().timeIntervalSince1970 * 1000.0).toLong()