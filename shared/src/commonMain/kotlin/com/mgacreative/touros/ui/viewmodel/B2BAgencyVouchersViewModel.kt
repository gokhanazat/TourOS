package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.B2BAgencyVoucherItem
import com.mgacreative.touros.domain.usecase.GetB2BAgencyVouchersUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class B2BAgencyVouchersUiState(
    val searchQuery: String = "",
    val vouchers: List<B2BAgencyVoucherItem> = emptyList(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class B2BAgencyVouchersViewModel(
    private val getB2BAgencyVouchersUseCase: GetB2BAgencyVouchersUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(B2BAgencyVouchersUiState())
    val uiState: StateFlow<B2BAgencyVouchersUiState> = _uiState.asStateFlow()

    init {
        loadVouchers()
    }

    fun loadVouchers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getB2BAgencyVouchersUseCase(tenantId)
            res.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    vouchers = list,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun printVoucher(voucher: B2BAgencyVoucherItem) {
        val updatedList = _uiState.value.vouchers.map { v ->
            if (v.voucherId == voucher.voucherId) v.copy(printedCount = v.printedCount + 1) else v
        }
        _uiState.value = _uiState.value.copy(
            vouchers = updatedList,
            notificationMessage = "🖨️ '${voucher.bookingCode}' Kodu İçin Seyahat Voucher'ı Yazıcıya Gönderildi."
        )
    }
}
