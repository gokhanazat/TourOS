package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.Expense
import com.mgacreative.touros.domain.model.SupplierTransaction
import com.mgacreative.touros.domain.repository.FinanceRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * 3.1.3 Tedarikçi Ödemelerinden Otomatik Gider ve Cari Kaydı Oluşturan Motor.
 */
class AutoSupplierExpenseEngine(
    private val financeRepository: FinanceRepository,
    private val supabaseClient: SupabaseClient
) {
    suspend fun settleSupplierTransaction(transaction: SupplierTransaction): Result<Expense> {
        return runCatching {
            // 1. Tedarikçi cari kaydını kapatıldı (isSettled = true) olarak güncelle
            supabaseClient.postgrest.from("supplier_transactions")
                .update(mapOf("is_settled" to true)) { filter { eq("id", transaction.id) } }

            // 2. Otomatik gider kaydı oluştur
            val expense = Expense(
                departureId = transaction.departureId,
                category = transaction.supplierType,
                description = "Tedarikçi Ödemesi: ${transaction.supplierName} (${transaction.description})",
                amount = transaction.amount,
                currency = transaction.currency,
                expenseDate = "2026-08-06",
                notes = "Otomatik Tedarikçi Cari Kapanış Gideri Engine",
                tenantId = transaction.tenantId
            )

            financeRepository.createExpense(expense).getOrThrow()
        }
    }
}
