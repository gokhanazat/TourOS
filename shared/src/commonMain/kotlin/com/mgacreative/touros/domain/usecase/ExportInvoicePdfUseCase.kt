package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.engine.InvoicePdfExportEngine
import com.mgacreative.touros.domain.model.Invoice

/**
 * 3.1.5 Faturayı PDF Olarak Dışa Aktarıp Belge Yönetimine Kaydetme Use Case.
 */
class ExportInvoicePdfUseCase(
    private val invoicePdfExportEngine: InvoicePdfExportEngine
) {
    suspend operator fun invoke(invoice: Invoice): Result<String> {
        return invoicePdfExportEngine.exportPdfAndSaveToDocuments(invoice)
    }
}
