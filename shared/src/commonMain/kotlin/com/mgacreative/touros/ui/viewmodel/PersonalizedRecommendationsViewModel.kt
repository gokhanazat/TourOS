package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.recommendation.TourRecommendation
import com.mgacreative.touros.domain.model.segmentation.CustomerSegment
import com.mgacreative.touros.domain.usecase.recommendation.GetPersonalizedRecommendationsUseCase
import com.mgacreative.touros.domain.usecase.segmentation.AnalyzeCustomerSegmentationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PersonalizedRecommendationsUiState(
    val isLoading: Boolean = false,
    val recommendations: List<TourRecommendation> = emptyList(),
    val customerSegment: CustomerSegment? = null,
    val errorMessage: String? = null
)

class PersonalizedRecommendationsViewModel(
    private val getPersonalizedRecommendationsUseCase: GetPersonalizedRecommendationsUseCase,
    private val analyzeCustomerSegmentationUseCase: AnalyzeCustomerSegmentationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalizedRecommendationsUiState())
    val uiState: StateFlow<PersonalizedRecommendationsUiState> = _uiState.asStateFlow()

    fun loadRecommendations(customerId: String, tenantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val recResult = getPersonalizedRecommendationsUseCase(customerId, tenantId)
            recResult.onSuccess { list ->
                _uiState.update { it.copy(isLoading = false, recommendations = list) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
        }
    }
}
