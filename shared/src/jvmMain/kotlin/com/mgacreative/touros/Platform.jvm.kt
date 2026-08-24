package com.mgacreative.touros

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()
actual fun getCurrentEpochMillis(): Long = System.currentTimeMillis()