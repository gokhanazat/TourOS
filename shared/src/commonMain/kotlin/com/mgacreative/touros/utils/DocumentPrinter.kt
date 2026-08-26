package com.mgacreative.touros.utils

/**
 * HTML tabanlı Sözleşme, Voucher ve Operatör formlarını tarayıcıda açma ve yazdırma motoru.
 */
expect object DocumentPrinter {
    fun printOrSaveHtml(htmlContent: String, title: String = "document")
}
