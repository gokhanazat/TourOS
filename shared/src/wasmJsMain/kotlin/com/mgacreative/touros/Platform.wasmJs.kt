package com.mgacreative.touros

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

@JsFun("() => Date.now()")
private external fun jsDateNow(): Double

@JsFun("(key) => { try { const params = new URLSearchParams(window.location.search); return params.get(key); } catch (e) { return null; } }")
private external fun jsGetQueryParam(key: String): String?

@JsFun("() => { try { const params = new URLSearchParams(window.location.search); let ag = params.get('agency') || params.get('ref') || params.get('b2b'); if (ag && ag.trim().length > 0) { sessionStorage.setItem('touros_agency_ref', ag.trim()); try { window.history.replaceState({}, document.title, window.location.pathname); } catch(e){} return ag.trim(); } const saved = sessionStorage.getItem('touros_agency_ref'); if (saved && saved.trim().length > 0) return saved.trim(); const host = window.location.hostname.toLowerCase(); if (host && host !== 'axileto.com' && host !== 'www.axileto.com' && host !== 'localhost' && host !== '127.0.0.1') { return 'DOMAIN:' + host; } return null; } catch(e) { return null; } }")
private external fun jsGetWebAgencyIdentifier(): String?

@JsFun("() => { try { window.history.replaceState({}, document.title, window.location.pathname); } catch(e){} }")
private external fun jsClearWebAgencyQueryFromUrl()

actual fun getPlatform(): Platform = WasmPlatform()
actual fun getCurrentEpochMillis(): Long = jsDateNow().toLong()
actual fun getWebQueryParameter(key: String): String? = jsGetQueryParam(key)
actual fun getWebAgencyIdentifier(): String? = jsGetWebAgencyIdentifier()
actual fun clearWebAgencyQueryFromUrl() { jsClearWebAgencyQueryFromUrl() }

@JsFun("(title) => { try { document.title = title; } catch(e){} }")
private external fun jsSetDocumentTitle(title: String)

actual fun setWebDocumentTitle(title: String) { jsSetDocumentTitle(title) }