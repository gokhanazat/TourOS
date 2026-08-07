package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.DocumentItem

/**
 * 3.4.2 Otomatik Voucher ve Paket Tur Hizmet Sözleşmesi PDF Şablon Motoru.
 */
class VoucherContractTemplateEngine {

    fun buildVoucherHtmlTemplate(
        bookingId: String,
        guestName: String,
        tourTitle: String,
        hotelName: String,
        departureDate: String,
        paxCount: Int
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8"/>
                <style>
                    body { font-family: sans-serif; margin: 20px; color: #1e293b; }
                    .header { border-bottom: 2px solid #2563eb; padding-bottom: 10px; margin-bottom: 20px; }
                    .badge { background-color: #dbeafe; color: #1e40af; padding: 4px 8px; border-radius: 4px; font-weight: bold; }
                    .box { background: #f8fafc; border: 1px solid #e2e8f0; padding: 15px; border-radius: 8px; margin-bottom: 15px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h2>🎟️ SEYAHAT VOUCHER (SEYAHAT BELGESİ)</h2>
                    <span class="badge">Rezervasyon No: #$bookingId</span>
                </div>
                <div class="box">
                    <p><strong>Misafir Adı:</strong> $guestName</p>
                    <p><strong>Tur Programı:</strong> $tourTitle</p>
                    <p><strong>Konaklama Oteli:</strong> $hotelName</p>
                    <p><strong>Kalkış Tarihi:</strong> $departureDate</p>
                    <p><strong>Kişi Sayısı:</strong> $paxCount Yetişkin</p>
                </div>
                <div class="box">
                    <h4>📍 Önemli Bilgilendirme & Rehber İletişim:</h4>
                    <p>Lütfen hareket saatinden 30 dakika önce kalkış noktasında hazır bulununuz.</p>
                    <p>Acil Durum Destek Hattı: +90 (850) 555 0 868</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun buildContractHtmlTemplate(
        bookingId: String,
        guestName: String,
        tourTitle: String,
        totalPrice: Double,
        currency: String
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8"/>
                <style>
                    body { font-family: sans-serif; margin: 20px; color: #1e293b; }
                    .header { border-bottom: 2px solid #16a34a; padding-bottom: 10px; margin-bottom: 20px; }
                    .terms { font-size: 11px; color: #475569; line-height: 1.4; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h2>📝 PAKET TUR HİZMET SÖZLEŞMESİ</h2>
                    <p>Sözleşme No: CT-$bookingId | Tarih: 06.08.2026</p>
                </div>
                <div>
                    <p><strong>Taraf (Tüketici):</strong> $guestName</p>
                    <p><strong>Satın Alınan Tur:</strong> $tourTitle</p>
                    <p><strong>Toplam Paket Tutarı:</strong> $totalPrice $currency</p>
                </div>
                <hr/>
                <div class="terms">
                    <h4>Madde 1 - İptal ve İade Şartları:</h4>
                    <p>Tüketici, turun başlamasına 30 gün kalaya kadar fesih hakkını kullandığında ödediği tutarın tamamı iade edilir.</p>
                    <h4>Madde 2 - Bagaj ve Sorumluluk:</h4>
                    <p>Acenta, mücbir sebepler dışında paket tur kapsamındaki tüm hizmetlerin sunulmasından sorumludur.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun generateDummyPdfItem(bookingId: String, docType: String, tenantId: String): DocumentItem {
        val title = if (docType == "contract") "Hizmet Sözleşmesi - B-$bookingId" else "Seyahat Voucher - B-$bookingId"
        val path = "$tenantId/$docType/${docType}_$bookingId.pdf"
        return DocumentItem(
            id = "doc-pdf-${(10000..99999).random()}",
            documentType = docType,
            title = title,
            filePath = path,
            fileSize = 1450000L,
            mimeType = "application/pdf",
            storageBucket = "documents",
            publicUrl = "https://touros.storage.supabase.co/documents/$path",
            bookingId = bookingId,
            tenantId = tenantId,
            createdAt = "2026-08-06 13:54"
        )
    }
}
