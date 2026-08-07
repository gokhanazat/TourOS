package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.B2CTourItem
import com.mgacreative.touros.domain.model.B2CTourSearchFilter
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.SearchB2CToursUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class B2CTourSearchUiState(
    val filter: B2CTourSearchFilter = B2CTourSearchFilter(),
    val availableCategories: List<String> = listOf("Tümü", "Kültür Turu", "Deniz & Mavi Tur", "Doğa & Yayla", "Günübirlik"),
    val availableCountries: List<String> = listOf("Tümü", "Türkiye", "İtalya", "Mısır", "Yunanistan"),
    val tours: List<B2CTourItem> = emptyList(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class B2CTourSearchViewModel(
    private val searchB2CToursUseCase: SearchB2CToursUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(B2CTourSearchUiState())
    val uiState: StateFlow<B2CTourSearchUiState> = _uiState.asStateFlow()

    init {
        performSearch()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(filter = _uiState.value.filter.copy(searchQuery = query))
        performSearch()
    }

    fun selectCategory(cat: String) {
        val categoryVal = if (cat == "Tümü") null else cat
        _uiState.value = _uiState.value.copy(filter = _uiState.value.filter.copy(category = categoryVal))
        performSearch()
    }

    fun selectCountry(country: String) {
        val countryVal = if (country == "Tümü") null else country
        _uiState.value = _uiState.value.copy(filter = _uiState.value.filter.copy(country = countryVal))
        performSearch()
    }

    fun updatePriceRange(minPrice: Double?, maxPrice: Double?) {
        _uiState.value = _uiState.value.copy(filter = _uiState.value.filter.copy(minPrice = minPrice, maxPrice = maxPrice))
        performSearch()
    }

    private fun performSearch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = searchB2CToursUseCase(_uiState.value.filter, tenantId)
            res.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    tours = list,
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
}
