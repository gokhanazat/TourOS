package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.AgencyOperatorConnectionEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AgencyOperatorConnectionsUiState {
    data object Loading : AgencyOperatorConnectionsUiState
    data class Success(val connections: List<AgencyOperatorConnectionEntity>) : AgencyOperatorConnectionsUiState
    data class Error(val message: String) : AgencyOperatorConnectionsUiState
}

class AgencyOperatorConnectionsViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgencyOperatorConnectionsUiState>(AgencyOperatorConnectionsUiState.Loading)
    val uiState: StateFlow<AgencyOperatorConnectionsUiState> = _uiState.asStateFlow()

    init {
        loadConnections()
    }

    fun loadConnections() {
        viewModelScope.launch {
            _uiState.value = AgencyOperatorConnectionsUiState.Loading
            runCatching {
                supabaseClient.postgrest["agency_operator_connections"]
                    .select()
                    .decodeList<AgencyOperatorConnectionEntity>()
            }.onSuccess { list ->
                _uiState.value = AgencyOperatorConnectionsUiState.Success(list)
            }.onFailure { err ->
                _uiState.value = AgencyOperatorConnectionsUiState.Error(err.message ?: "Bağlantılar yüklenemedi")
            }
        }
    }

    fun createConnection(
        operatorCompanyId: String,
        priceAdjustmentType: String,
        priceAdjustmentValue: Double,
        commissionRate: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                val newConn = AgencyOperatorConnectionEntity(
                    agencyId = "00000000-0000-0000-0000-000000000001",
                    operatorCompanyId = operatorCompanyId,
                    priceAdjustmentType = priceAdjustmentType,
                    priceAdjustmentValue = priceAdjustmentValue,
                    commissionRate = commissionRate,
                    status = "ACTIVE"
                )
                supabaseClient.postgrest["agency_operator_connections"].insert(newConn)
            }.onSuccess {
                loadConnections()
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Bağlantı eklenirken hata oluştu")
            }
        }
    }
}
