package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.engine.AutoSupplierExpenseEngine
import com.mgacreative.touros.domain.model.Expense
import com.mgacreative.touros.domain.model.SupplierTransaction

/**
 * 3.1.3 Tedarikçi Ödemesinden Otomatik Gider Kaydı Oluşturma Use Case.
 */
class ProcessSupplierExpenseUseCase(
    private val autoSupplierExpenseEngine: AutoSupplierExpenseEngine
) {
    suspend operator fun invoke(transaction: SupplierTransaction): Result<Expense> {
        if (transaction.amount <= 0) {
            return Result.failure(IllegalArgumentException("Tedarikçi ödeme tutarı 0'dan büyük olmalıdır."))
        }
        return autoSupplierExpenseEngine.settleSupplierTransaction(transaction)
    }
}
