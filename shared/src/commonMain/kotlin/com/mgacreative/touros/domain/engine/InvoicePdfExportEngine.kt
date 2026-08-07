package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.Invoice
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 3.1.5 Fatura PDF Oluşturma ve Belge Yönetimi Entegrasyon Motoru.
 */
class InvoicePdfExportEngine(
    private val supabaseClient: SupabaseClient
) {
    fun generateInvoicePdfHtml(invoice: Invoice): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Fatura ${invoice.invoiceNo}</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 30px; color: #333; }
                    .header { display: flex; justify-content: space-between; border-bottom: 2px solid #2563eb; padding-bottom: 15px; }
                    .title { font-size: 24px; font-weight: bold; color: #2563eb; }
                    .meta { margin-top: 20px; font-size: 14px; }
                    .table { width: 100%; border-collapse: collapse; margin-top: 25px; }
                    .table th, .table td { border: 1px solid #ddd; padding: 10px; text-align: left; }
                    .table th { background-color: #f1f5f9; }
                    .total { text-align: right; margin-top: 20px; font-size: 18px; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div>
                        <div class="title">SATIS FATURASI</div>
                        <div>TourOS Seyahat & Turizm A.Ş.</div>
                    </div>
                    <div>
                        <div><strong>Fatura No:</strong> ${invoice.invoiceNo}</div>
                        <div><strong>Tarih:</strong> ${invoice.issuedAt ?: "2026-08-06"}</div>
                    </div>
                </div>

                <div class="meta">
                    <div><strong>Musteri:</strong> ${invoice.customerName}</div>
                    <div><strong>Vergi No / T.C.:</strong> ${invoice.customerTaxNo ?: "-"}</div>
                    <div><strong>Vade Tarihi:</strong> ${invoice.dueDate ?: "2026-08-13"}</div>
                </div>

                <table class="table">
                    <thead>
                        <tr>
                            <th>Hizmet Aciklamasi</th>
                            <th>Matrah</th>
                            <th>KDV Orani</th>
                            <th>KDV Tutari</th>
                            <th>Toplam Tutar</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Tur & Rezervasyon Hizmet Bedeli (${invoice.notes ?: "Genel Hizmet"})</td>
                            <td>${invoice.subtotal} ${invoice.currency}</td>
                            <td>%${invoice.taxRate}</td>
                            <td>${invoice.taxAmount} ${invoice.currency}</td>
                            <td>${invoice.totalAmount} ${invoice.currency}</td>
                        </tr>
                    </tbody>
                </table>

                <div class="total">
                    <div>Odenecek Toplam Tutar: ${invoice.totalAmount} ${invoice.currency}</div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    suspend fun exportPdfAndSaveToDocuments(invoice: Invoice): Result<String> {
        return runCatching {
            val pdfName = "Fatura_${invoice.invoiceNo}.pdf"
            val mockStorageUrl = "https://supabase.touros.app/storage/v1/object/public/documents/${invoice.tenantId}/$pdfName"

            val params = buildJsonObject {
                put("p_invoice_id", invoice.id)
                put("p_pdf_name", pdfName)
                put("p_pdf_url", mockStorageUrl)
                put("p_tenant_id", invoice.tenantId)
            }

            supabaseClient.postgrest.rpc("export_invoice_pdf_and_link_document", params)
            mockStorageUrl
        }
    }
}
