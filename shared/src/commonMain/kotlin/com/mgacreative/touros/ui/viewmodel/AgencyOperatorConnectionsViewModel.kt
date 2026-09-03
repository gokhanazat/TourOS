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
            }.onFailure { error ->
                _uiState.value = AgencyOperatorConnectionsUiState.Error(error.message ?: "Bağlantılar yüklenemedi")
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
