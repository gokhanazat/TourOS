package com.mgacreative.touros

import web.navigator.navigator

class JsPlatform: Platform {
    private val userAgent = navigator.userAgent
    private val browserList = listOf("Chrome", "Firefox", "Safari", "Edge")

    override val name: String = userAgent.findAnyOf(browserList, ignoreCase = true)
            ?.let { (startIndex) -> userAgent.substring(startIndex).substringBefore(" ") }
            ?: "Unknown"
}

actual fun getPlatform(): Platform = JsPlatform()
actual fun getCurrentEpochMillis(): Long = kotlin.js.Date.now().toLong()
actual fun getWebQueryParameter(key: String): String? = runCatching {
    val search = kotlinx.browser.window.location.search
    org.w3c.dom.url.URLSearchParams(search).get(key)
}.getOrNull()

actual fun getWebAgencyIdentifier(): String? = runCatching {
    val search = kotlinx.browser.window.location.search
    val params = org.w3c.dom.url.URLSearchParams(search)
    val ag = params.get("agency") ?: params.get("ref") ?: params.get("b2b")
    if (!ag.isNullOrBlank()) {
        kotlinx.browser.sessionStorage.setItem("touros_agency_ref", ag.trim())
        runCatching {
            kotlinx.browser.window.history.replaceState(null, kotlinx.browser.document.title, kotlinx.browser.window.location.pathname)
        }
        return@runCatching ag.trim()
    }
    val saved = kotlinx.browser.sessionStorage.getItem("touros_agency_ref")
    if (!saved.isNullOrBlank()) return@runCatching saved.trim()
    val host = kotlinx.browser.window.location.hostname.lowercase()
    if (host.isNotBlank() && host != "axileto.com" && host != "www.axileto.com" && host != "localhost" && host != "127.0.0.1") {
        return@runCatching "DOMAIN:$host"
    }
    null
}.getOrNull()

actual fun clearWebAgencyQueryFromUrl() {
    runCatching {
        kotlinx.browser.window.history.replaceState(null, kotlinx.browser.document.title, kotlinx.browser.window.location.pathname)
    }
}

actual fun setWebDocumentTitle(title: String) {
    runCatching {
        kotlinx.browser.document.title = title
    }
}