package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class AgencyLedgerSummaryDto(
    val id: String = "",
    val name: String = "",
    val operator_code: String? = null,
    val monthly_subscription_fee: Double = 2500.0,
    val current_balance: Double = 0.0,
    val is_debt_locked: Boolean = false,
    val is_active: Boolean = true,
    val currency: String = "TRY",
    val email: String? = null,
    val phone: String? = null
)

@Serializable
data class AgencyLedgerTransactionDto(
    val id: String = "",
    val company_id: String = "",
    val transaction_date: String? = null,
    val transaction_type: String = "DEBIT", // 'DEBIT' or 'CREDIT'
    val category: String = "MONTHLY_SUBSCRIPTION",
    val amount: Double = 0.0,
    val balance_after: Double = 0.0,
    val currency: String = "TRY",
    val reference_no: String? = null,
    val description: String = "",
    val created_by: String? = "SaaS Admin"
)

@Serializable
data class SystemConfigEntryDto(
    val config_key: String = "",
    val config_value: String = "",
    val description: String? = null
)

class AdminAgencyLedgerViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _agencies = MutableStateFlow<List<AgencyLedgerSummaryDto>>(emptyList())
    val agencies: StateFlow<List<AgencyLedgerSummaryDto>> = _agencies.asStateFlow()

    private val _selectedAgency = MutableStateFlow<AgencyLedgerSummaryDto?>(null)
    val selectedAgency: StateFlow<AgencyLedgerSummaryDto?> = _selectedAgency.asStateFlow()

    private val _transactions = MutableStateFlow<List<AgencyLedgerTransactionDto>>(emptyList())
    val transactions: StateFlow<List<AgencyLedgerTransactionDto>> = _transactions.asStateFlow()

    private val _isAutoLockEnabled = MutableStateFlow(false)
    val isAutoLockEnabled: StateFlow<Boolean> = _isAutoLockEnabled.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 1. Kilit Modu Yapılandırmasını Oku
            runCatching {
                supabaseClient.postgrest["saas_system_config"]
                    .select {
                        filter {
                            eq("config_key", "auto_debt_lock_enabled")
                        }
                    }
                    .decodeSingleOrNull<SystemConfigEntryDto>()
            }.onSuccess { cfg ->
                _isAutoLockEnabled.value = cfg?.config_value?.toBooleanStrictOrNull() ?: false
            }

            // 2. Acenteleri ve Bakiyeleri Oku
            runCatching {
                supabaseClient.postgrest["companies"]
                    .select {
                        filter {
                            eq("company_type", "acente")
                        }
                    }
                    .decodeList<AgencyLedgerSummaryDto>()
            }.onSuccess { list ->
                _agencies.value = list.sortedByDescending { it.current_balance }
                if (_selectedAgency.value == null && list.isNotEmpty()) {
                    selectAgency(list.first())
                } else if (_selectedAgency.value != null) {
                    val updated = list.find { it.id == _selectedAgency.value?.id }
                    if (updated != null) selectAgency(updated)
                }
            }.onFailure {
                // Fallback Mock Veri (Offline / DB Hazır Olana Kadar)
                if (_agencies.value.isEmpty()) {
                    val mockList = listOf(
                        AgencyLedgerSummaryDto(
                            id = "mock-1",
                            name = "MGA Travel Istanbul",
                            operator_code = "MGA-001",
                            monthly_subscription_fee = 3500.0,
                            current_balance = 0.0,
                            is_debt_locked = false,
                            is_active = true
                        ),
                        AgencyLedgerSummaryDto(
                            id = "mock-2",
                            name = "Antalya Güneşi Turizm",
                            operator_code = "AGT-104",
                            monthly_subscription_fee = 2500.0,
                            current_balance = 2500.0,
                            is_debt_locked = true,
                            is_active = false
                        ),
                        AgencyLedgerSummaryDto(
                            id = "mock-3",
                            name = "Kapadokya Balon Acentesi",
                            operator_code = "KPD-202",
                            monthly_subscription_fee = 1800.0,
                            current_balance = 3600.0,
                            is_debt_locked = true,
                            is_active = false
                        )
                    )
                    _agencies.value = mockList
                    selectAgency(mockList.first())
                }
            }

            _isLoading.value = false
        }
    }

    fun selectAgency(agency: AgencyLedgerSummaryDto) {
        _selectedAgency.value = agency
        viewModelScope.launch {
            runCatching {
                supabaseClient.postgrest["agency_ledger_transactions"]
                    .select {
                        filter {
                            eq("company_id", agency.id)
                        }
                    }
                    .decodeList<AgencyLedgerTransactionDto>()
            }.onSuccess { txList ->
                _transactions.value = txList.sortedByDescending { it.transaction_date }
            }.onFailure {
                // Mock hareketler
                _transactions.value = listOf(
                    AgencyLedgerTransactionDto(
                        id = "tx-1",
                        company_id = agency.id,
                        transaction_date = "2026-08-01T00:00:00Z",
                        transaction_type = "DEBIT",
                        category = "MONTHLY_SUBSCRIPTION",
                        amount = agency.monthly_subscription_fee,
                        balance_after = agency.current_balance,
                        description = "Ağustos 2026 Sabit Abonelik & B2B Sorgu Paketi",
                        reference_no = "SUB-202608-${agency.operator_code ?: "001"}"
                    )
                )
            }
        }
    }

    fun toggleAutoLock(enabled: Boolean) {
        viewModelScope.launch {
            _isAutoLockEnabled.value = enabled
            runCatching {
                val params = buildJsonObject {
                    put("config_key", "auto_debt_lock_enabled")
                    put("config_value", enabled.toString())
                    put("updated_at", "NOW()")
                }
                supabaseClient.postgrest["saas_system_config"].upsert(params)
            }
            _notificationMessage.value = if (enabled) {
                "🔒 Otomatik Borç Kilit Modu AKTİF edildi. Borcu olan acenteler kilitlenecektir."
            } else {
                "🔓 Kilit Modu PASİF yapıldı. Acenteler borçtan dolayı kilitlenmeyecek (Test/İzleme Modu)."
            }
            loadData()
        }
    }

    fun recordPayment(
        companyId: String,
        amount: Double,
        paymentMethod: String,
        referenceNo: String,
        description: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = runCatching {
                val params = buildJsonObject {
                    put("p_company_id", companyId)
                    put("p_amount", amount.toString())
                    put("p_payment_method", paymentMethod)
                    put("p_reference_no", referenceNo)
                    put("p_description", description)
                }
                supabaseClient.postgrest.rpc("record_agency_payment", params)
            }

            res.onSuccess {
                _notificationMessage.value = "✅ Tahsilat başarıyla işlendi ve acente borcundan düşüldü."
                loadData()
            }.onFailure { err ->
                _notificationMessage.value = "⚠️ Tahsilat işlenirken hata oluştu: ${err.message}"
            }
            _isLoading.value = false
        }
    }

    fun recordDebit(
        companyId: String,
        amount: Double,
        category: String,
        referenceNo: String,
        description: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = runCatching {
                val params = buildJsonObject {
                    put("p_company_id", companyId)
                    put("p_amount", amount.toString())
                    put("p_category", category)
                    put("p_reference_no", referenceNo)
                    put("p_description", description)
                }
                supabaseClient.postgrest.rpc("record_agency_debit", params)
            }

            res.onSuccess {
                _notificationMessage.value = "⚠️ Acenteye $amount TL tutarında borç kaydedildi."
                loadData()
            }.onFailure { err ->
                _notificationMessage.value = "Hata: ${err.message}"
            }
            _isLoading.value = false
        }
    }

    fun triggerMonthlyBilling() {
        viewModelScope.launch {
            _isLoading.value = true
            val res = runCatching {
                supabaseClient.postgrest.rpc("apply_monthly_subscription_debts")
            }
            res.onSuccess {
                _notificationMessage.value = "🚀 Tüm acentelerin aylık abonelik borçları başarıyla tahakkuk ettirildi."
                loadData()
            }.onFailure { err ->
                _notificationMessage.value = "Hata: ${err.message}"
            }
            _isLoading.value = false
        }
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }
}
