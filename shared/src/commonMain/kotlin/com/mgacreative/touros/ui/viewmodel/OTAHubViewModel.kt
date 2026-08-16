package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.domain.model.ota.OTABooking
import com.mgacreative.touros.domain.model.ota.OTAChannelProductMapping
import com.mgacreative.touros.domain.model.ota.OTASyncLog
import com.mgacreative.touros.domain.repository.HotelRepository
import com.mgacreative.touros.domain.repository.OTARepository
import com.mgacreative.touros.domain.repository.TourRepository
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OTAHubUiState(
    val isLoading: Boolean = false,
    val accounts: List<OTAAccount> = emptyList(),
    val mappings: List<OTAChannelProductMapping> = emptyList(),
    val tours: List<Tour> = emptyList(),
    val hotels: List<Hotel> = emptyList(),
    val syncLogs: List<OTASyncLog> = emptyList(),
    val bookings: List<OTABooking> = emptyList(),
    val selectedProviderForConfig: OTAAccount? = null,
    val activeTab: Int = 0, // 0: Kanallar & API, 1: Kanal Ürün Dağıtımı
    val filterProviderId: String = "ALL",
    val syncStatusMessage: String? = null,
    val errorMessage: String? = null
)

class OTAHubViewModel(
    private val otaRepository: OTARepository,
    private val tourRepository: TourRepository,
    private val hotelRepository: HotelRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OTAHubUiState())
    val uiState: StateFlow<OTAHubUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val tenantId = getCurrentUserUseCase()?.tenantId ?: "tenant-001"

            val accountsRes = otaRepository.getAccounts(tenantId)
            val mappingsRes = otaRepository.getMappings(tenantId)
            val toursRes = tourRepository.getTours(tenantId)
            val hotelsRes = hotelRepository.getHotels(tenantId)
            val logsRes = otaRepository.getSyncLogs(tenantId)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    accounts = accountsRes.getOrDefault(emptyList()),
                    mappings = mappingsRes.getOrDefault(emptyList()),
                    tours = toursRes.getOrDefault(emptyList()),
                    hotels = hotelsRes.getOrDefault(emptyList()),
                    syncLogs = logsRes.getOrDefault(emptyList())
                )
            }
        }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(activeTab = index) }
    }

    fun openProviderConfig(account: OTAAccount) {
        _uiState.update { it.copy(selectedProviderForConfig = account) }
    }

    fun closeProviderConfig() {
        _uiState.update { it.copy(selectedProviderForConfig = null) }
    }

    fun saveAccountConfig(updated: OTAAccount) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            otaRepository.saveAccount(updated)
            val tenantId = getCurrentUserUseCase()?.tenantId ?: "tenant-001"
            val accountsRes = otaRepository.getAccounts(tenantId)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    accounts = accountsRes.getOrDefault(emptyList()),
                    selectedProviderForConfig = null,
                    syncStatusMessage = "✅ ${updated.accountName} API ve bağlantı ayarları kaydedildi."
                )
            }
        }
    }

    fun toggleProductChannel(
        productId: String,
        productTitle: String,
        productType: String,
        providerId: String,
        isEnabled: Boolean
    ) {
        viewModelScope.launch {
            val tenantId = getCurrentUserUseCase()?.tenantId ?: "tenant-001"
            otaRepository.toggleProductChannel(
                tenantId = tenantId,
                productId = productId,
                productTitle = productTitle,
                productType = productType,
                providerId = providerId,
                isEnabled = isEnabled
            )
            val mappingsRes = otaRepository.getMappings(tenantId)
            _uiState.update {
                it.copy(
                    mappings = mappingsRes.getOrDefault(emptyList()),
                    syncStatusMessage = "✅ '$productTitle' ürünü ${providerId.uppercase()} kanalında ${if (isEnabled) "satışa açıldı" else "satıştan kaldırıldı"}."
                )
            }
        }
    }

    fun syncNow(providerId: String, isFullSync: Boolean, tenantId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val tId = tenantId ?: (getCurrentUserUseCase()?.tenantId ?: "tenant-001")
            val result = otaRepository.syncBookings(providerId, tId)
            val logsRes = otaRepository.getSyncLogs(tId)

            result.onSuccess { bookings ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        bookings = bookings,
                        syncLogs = logsRes.getOrDefault(emptyList()),
                        syncStatusMessage = "⚡ ${providerId.uppercase()} senkronizasyonu tamamlandı."
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
        }
    }

    fun filterLogs(providerId: String) {
        viewModelScope.launch {
            val tenantId = getCurrentUserUseCase()?.tenantId ?: "tenant-001"
            val logsRes = otaRepository.getSyncLogs(tenantId, providerId)
            _uiState.update {
                it.copy(
                    filterProviderId = providerId,
                    syncLogs = logsRes.getOrDefault(emptyList())
                )
            }
        }
    }

    fun clearNotification() {
        _uiState.update { it.copy(syncStatusMessage = null, errorMessage = null) }
    }
}
