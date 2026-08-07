package com.mgacreative.touros

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform