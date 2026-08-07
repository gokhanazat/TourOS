package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.Invoice
import com.mgacreative.touros.domain.repository.FinanceRepository

/**
 * 3.1.2 Otomatik Gelir Kaydı Motoru (Accounting Engine).
 * Onaylanan her rezervasyon için otomatik satış faturası (Invoice) hesaplar ve oluşturur.
 */
class AutoRevenueEngine(
    private val financeRepository: FinanceRepository
) {
    suspend fun processBookingApproval(booking: Booking): Result<Invoice> {
        return runCatching {
            val total = booking.totalPrice
            val taxRate = 20.0
            val subtotal = ((total / 1.20) * 100).toInt() / 100.0
            val taxAmount = ((total - subtotal) * 100).toInt() / 100.0

            val invoice = Invoice(
                invoiceNo = "INV-${booking.bookingCode}",
                bookingId = booking.id,
                invoiceType = "sale",
                customerName = booking.customerName,
                customerTaxNo = null,
                subtotal = subtotal,
                taxRate = taxRate,
                taxAmount = taxAmount,
                totalAmount = total,
                currency = booking.currency,
                status = "issued",
                issuedAt = "2026-08-06",
                dueDate = "2026-08-13",
                notes = "Rezervasyon onayı ile otomatik oluşturulan satış faturası (Accounting Engine)",
                tenantId = booking.tenantId
            )

            val res = financeRepository.createInvoice(invoice)
            res.getOrThrow()
        }
    }
}
