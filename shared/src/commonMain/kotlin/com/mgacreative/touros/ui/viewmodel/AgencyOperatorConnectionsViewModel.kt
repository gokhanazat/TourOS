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

    private val sampleOperators = listOf(
        AgencyOperatorConnectionEntity(
            id = "sample-1",
            agencyId = "agency-01",
            operatorCompanyId = "op-coral-01",
            operatorName = "Coral Travel",
            operatorLogo = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=120&q=80",
            operatorType = "GLOBAL",
            integrationType = "API",
            apiEndpoint = "https://api.coraltravel.com/v2/b2b",
            apiKey = "crl_live_894f2910a",
            priceAdjustmentType = "percentage",
            priceAdjustmentValue = 12.5,
            commissionRate = 8.0,
            currency = "EUR",
            taxOffice = "Mecidiyeköy VD",
            taxNumber = "2240091823",
            iban = "TR88 0006 4000 0011 2233 4455 66",
            bankName = "İş Bankası - Levent Şubesi",
            contactName = "Ahmet Yılmaz (Operasyon Müdürü)",
            contactPhone = "+90 212 555 0199",
            contactEmail = "b2b@coraltravel.com",
            status = "ACTIVE"
        ),
        AgencyOperatorConnectionEntity(
            id = "sample-2",
            agencyId = "agency-01",
            operatorCompanyId = "op-pegas-02",
            operatorName = "Pegas Touristik",
            operatorLogo = "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=120&q=80",
            operatorType = "GLOBAL",
            integrationType = "REST",
            apiEndpoint = "https://b2b.pegast.ru/api/v1",
            apiKey = "pgs_prod_77192bc",
            priceAdjustmentType = "percentage",
            priceAdjustmentValue = 10.0,
            commissionRate = 9.5,
            currency = "USD",
            taxOffice = "Zincirlikuyu VD",
            taxNumber = "7290019283",
            iban = "TR44 0001 5000 0099 8877 6655 44",
            bankName = "Garanti BBVA - Beşiktaş",
            contactName = "Elena Volkova (Key Account)",
            contactPhone = "+90 532 999 4422",
            contactEmail = "partner@pegast.ru",
            status = "ACTIVE"
        ),
        AgencyOperatorConnectionEntity(
            id = "sample-3",
            agencyId = "agency-01",
            operatorCompanyId = "op-ets-03",
            operatorName = "ETS Tur",
            operatorLogo = "",
            operatorType = "DOMESTIC",
            integrationType = "SOAP",
            apiEndpoint = "https://ws.etstur.com/agency/v3",
            apiKey = "ets_agency_9921",
            priceAdjustmentType = "fixed",
            priceAdjustmentValue = 500.0,
            commissionRate = 12.0,
            currency = "TRY",
            taxOffice = "Kadıköy VD",
            taxNumber = "3810029381",
            iban = "TR12 0006 2000 0033 4455 6677 88",
            bankName = "Akbank - Kadıköy",
            contactName = "Canan Demir (İç Pazar Sorumlusu)",
            contactPhone = "+90 216 444 0387",
            contactEmail = "acente@etstur.com",
            status = "PAUSED"
        )
    )

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
                if (list.isEmpty()) {
                    _uiState.value = AgencyOperatorConnectionsUiState.Success(sampleOperators)
                } else {
                    _uiState.value = AgencyOperatorConnectionsUiState.Success(list)
                }
            }.onFailure {
                // Supabase bağlantısı henüz kurulmamışsa lokal demo listeyi göster
                _uiState.value = AgencyOperatorConnectionsUiState.Success(sampleOperators)
            }
        }
    }

    fun saveConnection(
        connection: AgencyOperatorConnectionEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val currentList = (_uiState.value as? AgencyOperatorConnectionsUiState.Success)?.connections ?: emptyList()
            
            runCatching {
                if (connection.id.isBlank() || connection.id.startsWith("sample-")) {
                    val newConn = connection.copy(
                        id = if (connection.id.isBlank()) "conn-${currentList.size + 1}" else connection.id,
                        agencyId = "agency-01"
                    )
                    supabaseClient.postgrest["agency_operator_connections"].insert(newConn)
                    newConn
                } else {
                    supabaseClient.postgrest["agency_operator_connections"]
                        .update(connection) {
                            filter { eq("id", connection.id) }
                        }
                    connection
                }
            }.onSuccess { savedItem ->
                val updatedList = if (currentList.any { it.id == savedItem.id }) {
                    currentList.map { if (it.id == savedItem.id) savedItem else it }
                } else {
                    currentList + savedItem
                }
                _uiState.value = AgencyOperatorConnectionsUiState.Success(updatedList)
                onSuccess()
            }.onFailure {
                // Lokal listede güncelle/ekle fallback
                val updatedList = if (currentList.any { it.id == connection.id }) {
                    currentList.map { if (it.id == connection.id) connection else it }
                } else {
                    val newId = "sample-${currentList.size + 1}"
                    currentList + connection.copy(id = newId)
                }
                _uiState.value = AgencyOperatorConnectionsUiState.Success(updatedList)
                onSuccess()
            }
        }
    }

    fun deleteConnection(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val currentList = (_uiState.value as? AgencyOperatorConnectionsUiState.Success)?.connections ?: emptyList()
            runCatching {
                supabaseClient.postgrest["agency_operator_connections"].delete {
                    filter { eq("id", id) }
                }
            }.onSuccess {
                _uiState.value = AgencyOperatorConnectionsUiState.Success(currentList.filterNot { it.id == id })
                onSuccess()
            }.onFailure {
                // Fallback local deletion
                _uiState.value = AgencyOperatorConnectionsUiState.Success(currentList.filterNot { it.id == id })
                onSuccess()
            }
        }
    }
}
