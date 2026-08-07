package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.Account
import com.mgacreative.touros.domain.model.Commission
import com.mgacreative.touros.domain.model.Expense
import com.mgacreative.touros.domain.model.Invoice
import com.mgacreative.touros.domain.model.Payment

/**
 * 3.1.1 Muhasebe & Finans Modülü Repository Arayüzü.
 */
interface FinanceRepository {
    // Accounts (Hesaplar)
    suspend fun getAccounts(tenantId: String): Result<List<Account>>
    suspend fun createAccount(account: Account): Result<Account>
    suspend fun updateAccount(account: Account): Result<Account>

    // Invoices (Faturalar)
    suspend fun getInvoices(tenantId: String): Result<List<Invoice>>
    suspend fun createInvoice(invoice: Invoice): Result<Invoice>
    suspend fun updateInvoiceStatus(invoiceId: String, status: String): Result<Boolean>

    // Payments (Ödemeler/Tahsilatlar)
    suspend fun getPayments(tenantId: String): Result<List<Payment>>
    suspend fun createPayment(payment: Payment): Result<Payment>

    // Commissions (Komisyonlar)
    suspend fun getCommissions(tenantId: String): Result<List<Commission>>
    suspend fun createCommission(commission: Commission): Result<Commission>
    suspend fun payCommission(commissionId: String): Result<Boolean>

    // Expenses (Giderler/Masraflar)
    suspend fun getExpenses(tenantId: String): Result<List<Expense>>
    suspend fun createExpense(expense: Expense): Result<Expense>
}
