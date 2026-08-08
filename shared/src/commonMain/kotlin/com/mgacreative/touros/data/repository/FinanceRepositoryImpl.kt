package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.AccountEntity
import com.mgacreative.touros.data.database.entity.CommissionEntity
import com.mgacreative.touros.data.database.entity.ExpenseEntity
import com.mgacreative.touros.data.database.entity.InvoiceEntity
import com.mgacreative.touros.data.database.entity.PaymentEntity
import com.mgacreative.touros.domain.model.Account
import com.mgacreative.touros.domain.model.Commission
import com.mgacreative.touros.domain.model.Expense
import com.mgacreative.touros.domain.model.Invoice
import com.mgacreative.touros.domain.model.Payment
import com.mgacreative.touros.domain.repository.FinanceRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FinanceRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : FinanceRepository {

    // =================  ACCOUNTS  =================
    override suspend fun getAccounts(tenantId: String): Result<List<Account>> {
        return runCatching {
            val list = supabaseClient.postgrest.from("accounts")
                .select { filter { eq("tenant_id", tenantId) } }
                .decodeList<AccountEntity>()

            list.map {
                Account(
                    id = it.id,
                    name = it.name,
                    accountType = it.accountType,
                    currency = it.currency,
                    balance = it.balance,
                    iban = it.iban,
                    bankName = it.bankName,
                    isActive = it.isActive,
                    tenantId = it.tenantId
                )
            }
        }
    }

    override suspend fun createAccount(account: Account): Result<Account> {
        return runCatching {
            val entity = AccountEntity(
                name = account.name,
                accountType = account.accountType,
                currency = account.currency,
                balance = account.balance,
                iban = account.iban,
                bankName = account.bankName,
                isActive = account.isActive,
                tenantId = account.tenantId
            )
            val created = supabaseClient.postgrest.from("accounts")
                .insert(entity) { select() }
                .decodeSingle<AccountEntity>()

            account.copy(id = created.id)
        }
    }

    override suspend fun updateAccount(account: Account): Result<Account> {
        return runCatching {
            val entity = AccountEntity(
                id = account.id,
                name = account.name,
                accountType = account.accountType,
                currency = account.currency,
                balance = account.balance,
                iban = account.iban,
                bankName = account.bankName,
                isActive = account.isActive,
                tenantId = account.tenantId
            )
            supabaseClient.postgrest.from("accounts")
                .update(entity) { filter { eq("id", account.id) } }
            account
        }
    }

    // =================  INVOICES  =================
    override suspend fun getInvoices(tenantId: String): Result<List<Invoice>> {
        return runCatching {
            val list = supabaseClient.postgrest.from("invoices")
                .select { filter { eq("tenant_id", tenantId) } }
                .decodeList<InvoiceEntity>()

            list.map {
                Invoice(
                    id = it.id,
                    invoiceNo = it.invoiceNo,
                    bookingId = it.bookingId,
                    invoiceType = it.invoiceType,
                    customerName = it.customerName,
                    customerTaxNo = it.customerTaxNo,
                    subtotal = it.subtotal,
                    taxRate = it.taxRate,
                    taxAmount = it.taxAmount,
                    totalAmount = it.totalAmount,
                    currency = it.currency,
                    status = it.status,
                    issuedAt = it.issuedAt,
                    dueDate = it.dueDate,
                    notes = it.notes,
                    tenantId = it.tenantId
                )
            }
        }
    }

    override suspend fun createInvoice(invoice: Invoice): Result<Invoice> {
        return runCatching {
            val entity = InvoiceEntity(
                invoiceNo = invoice.invoiceNo,
                bookingId = invoice.bookingId,
                invoiceType = invoice.invoiceType,
                customerName = invoice.customerName,
                customerTaxNo = invoice.customerTaxNo,
                subtotal = invoice.subtotal,
                taxRate = invoice.taxRate,
                taxAmount = invoice.taxAmount,
                totalAmount = invoice.totalAmount,
                currency = invoice.currency,
                status = invoice.status,
                issuedAt = invoice.issuedAt,
                dueDate = invoice.dueDate,
                notes = invoice.notes,
                tenantId = invoice.tenantId
            )
            val created = supabaseClient.postgrest.from("invoices")
                .insert(entity) { select() }
                .decodeSingle<InvoiceEntity>()

            invoice.copy(id = created.id)
        }
    }

    override suspend fun updateInvoiceStatus(invoiceId: String, status: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("invoices")
                .update(mapOf("status" to status)) { filter { eq("id", invoiceId) } }
            true
        }
    }

    // =================  PAYMENTS  =================
    override suspend fun getPayments(tenantId: String): Result<List<Payment>> {
        return runCatching {
            val list = supabaseClient.postgrest.from("payments")
                .select { filter { eq("tenant_id", tenantId) } }
                .decodeList<PaymentEntity>()

            list.map {
                Payment(
                    id = it.id,
                    invoiceId = it.invoiceId,
                    accountId = it.accountId,
                    amount = it.amount,
                    currency = it.currency,
                    paymentMethod = it.paymentMethod,
                    paymentDate = it.paymentDate,
                    referenceNo = it.referenceNo,
                    notes = it.notes,
                    tenantId = it.tenantId
                )
            }
        }
    }

    override suspend fun createPayment(payment: Payment): Result<Payment> {
        return runCatching {
            val entity = PaymentEntity(
                invoiceId = payment.invoiceId,
                accountId = payment.accountId,
                amount = payment.amount,
                currency = payment.currency,
                paymentMethod = payment.paymentMethod,
                paymentDate = payment.paymentDate,
                referenceNo = payment.referenceNo,
                notes = payment.notes,
                tenantId = payment.tenantId
            )
            val created = supabaseClient.postgrest.from("payments")
                .insert(entity) { select() }
                .decodeSingle<PaymentEntity>()

            payment.copy(id = created.id)
        }
    }

    // =================  COMMISSIONS  =================
    override suspend fun getCommissions(tenantId: String): Result<List<Commission>> {
        return runCatching {
            val list = supabaseClient.postgrest.from("commissions")
                .select { filter { eq("tenant_id", tenantId) } }
                .decodeList<CommissionEntity>()

            list.map {
                Commission(
                    id = it.id,
                    bookingId = it.bookingId,
                    agentName = it.agentName,
                    agentType = it.agentType,
                    rate = it.rate,
                    amount = it.amount,
                    currency = it.currency,
                    isPaid = it.isPaid,
                    paidAt = it.paidAt,
                    notes = it.notes,
                    tenantId = it.tenantId
                )
            }
        }
    }

    override suspend fun createCommission(commission: Commission): Result<Commission> {
        return runCatching {
            val entity = CommissionEntity(
                bookingId = commission.bookingId,
                agentName = commission.agentName,
                agentType = commission.agentType,
                rate = commission.rate,
                amount = commission.amount,
                currency = commission.currency,
                isPaid = commission.isPaid,
                paidAt = commission.paidAt,
                notes = commission.notes,
                tenantId = commission.tenantId
            )
            val created = supabaseClient.postgrest.from("commissions")
                .insert(entity) { select() }
                .decodeSingle<CommissionEntity>()

            commission.copy(id = created.id)
        }
    }

    override suspend fun payCommission(commissionId: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("b2b_agency_commissions")
                .update(buildJsonObject { put("is_paid", true) }) { filter { eq("id", commissionId) } }
            true
        }
    }

    // =================  EXPENSES  =================
    override suspend fun getExpenses(tenantId: String): Result<List<Expense>> {
        return runCatching {
            val list = supabaseClient.postgrest.from("expenses")
                .select { filter { eq("tenant_id", tenantId) } }
                .decodeList<ExpenseEntity>()

            list.map {
                Expense(
                    id = it.id,
                    accountId = it.accountId,
                    departureId = it.departureId,
                    category = it.category,
                    description = it.description,
                    amount = it.amount,
                    currency = it.currency,
                    expenseDate = it.expenseDate,
                    receiptUrl = it.receiptUrl,
                    notes = it.notes,
                    tenantId = it.tenantId
                )
            }
        }
    }

    override suspend fun createExpense(expense: Expense): Result<Expense> {
        return runCatching {
            val entity = ExpenseEntity(
                accountId = expense.accountId,
                departureId = expense.departureId,
                category = expense.category,
                description = expense.description,
                amount = expense.amount,
                currency = expense.currency,
                expenseDate = expense.expenseDate,
                receiptUrl = expense.receiptUrl,
                notes = expense.notes,
                tenantId = expense.tenantId
            )
            val created = supabaseClient.postgrest.from("expenses")
                .insert(entity) { select() }
                .decodeSingle<ExpenseEntity>()

            expense.copy(id = created.id)
        }
    }
}
