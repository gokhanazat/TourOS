package com.mgacreative.touros

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

@JsFun("() => Date.now()")
private external fun jsDateNow(): Double

actual fun getPlatform(): Platform = WasmPlatform()
actual fun getCurrentEpochMillis(): Long = jsDateNow().toLong()