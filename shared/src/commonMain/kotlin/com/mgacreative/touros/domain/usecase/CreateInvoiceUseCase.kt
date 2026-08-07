package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Invoice
import com.mgacreative.touros.domain.repository.FinanceRepository

/**
 * 3.1.5 Manuel Fatura Oluşturma Use Case.
 */
class CreateInvoiceUseCase(
    private val financeRepository: FinanceRepository
) {
    suspend operator fun invoke(invoice: Invoice): Result<Invoice> {
        if (invoice.invoiceNo.isBlank()) {
            return Result.failure(IllegalArgumentException("Fatura numarası boş olamaz."))
        }
        if (invoice.totalAmount <= 0) {
            return Result.failure(IllegalArgumentException("Fatura tutarı 0'dan büyük olmalıdır."))
        }
        return financeRepository.createInvoice(invoice)
    }
}
