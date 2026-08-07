package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.model.GuideReview
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetGuidesUseCase
import com.mgacreative.touros.domain.usecase.SubmitGuideReviewUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GuideRatingUiState {
    data object Loading : GuideRatingUiState
    data class Success(
        val targetGuide: Guide,
        val reviews: List<GuideReview> = emptyList(),
        val selectedStar: Int = 5,
        val customerNameInput: String = "",
        val commentInput: String = "",
        val successNotification: String? = null
    ) : GuideRatingUiState
    data class Error(val message: String) : GuideRatingUiState
}

class GuideRatingViewModel(
    private val getGuidesUseCase: GetGuidesUseCase,
    private val submitGuideReviewUseCase: SubmitGuideReviewUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuideRatingUiState>(GuideRatingUiState.Loading)
    val uiState: StateFlow<GuideRatingUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = GuideRatingUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val guidesRes = getGuidesUseCase(tenantId)
            val guide = guidesRes.getOrDefault(emptyList()).firstOrNull() ?: Guide(
                id = "g1",
                fullName = "Zeynep Arslan",
                licenseNumber = "K-12345",
                specialization = "Kapadokya & Kültür Turları",
                rating = 4.8,
                totalToursCompleted = 48
            )

            val fallbackReviews = listOf(
                GuideReview("r1", guide.id, "dep-101", null, "Hans Müller", 5, "Zeynep Hanım harika bir rehber! Kapadokya tarihini çok iyi anlattı.", tenantId, "2026-08-04"),
                GuideReview("r2", guide.id, "dep-101", null, "Sarah Jenkins", 5, "Excellence guide, very helpful during balloon tour!", tenantId, "2026-08-02"),
                GuideReview("r3", guide.id, "dep-100", null, "Mehmet Kaya", 4, "Genel olarak memnun kaldık, teşekkürler.", tenantId, "2026-07-28")
            )

            _uiState.value = GuideRatingUiState.Success(
                targetGuide = guide,
                reviews = fallbackReviews,
                selectedStar = 5
            )
        }
    }

    fun setSelectedStar(star: Int) {
        val state = _uiState.value as? GuideRatingUiState.Success ?: return
        _uiState.value = state.copy(selectedStar = star)
    }

    fun updateCustomerName(name: String) {
        val state = _uiState.value as? GuideRatingUiState.Success ?: return
        _uiState.value = state.copy(customerNameInput = name)
    }

    fun updateComment(comment: String) {
        val state = _uiState.value as? GuideRatingUiState.Success ?: return
        _uiState.value = state.copy(commentInput = comment)
    }

    fun submitReview() {
        viewModelScope.launch {
            val state = _uiState.value as? GuideRatingUiState.Success ?: return@launch
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val newReview = GuideReview(
                guideId = state.targetGuide.id,
                departureId = "dep-101",
                customerName = state.customerNameInput.ifBlank { "Misafir Yolcu" },
                rating = state.selectedStar,
                comment = state.commentInput.ifBlank { null },
                tenantId = tenantId
            )

            val res = submitGuideReviewUseCase(newReview)
            res.onSuccess { savedReview ->
                val updatedReviews = listOf(savedReview) + state.reviews
                val newAvgRating = ((updatedReviews.map { it.rating }.average()) * 10).toInt() / 10.0
                val updatedGuide = state.targetGuide.copy(
                    rating = newAvgRating,
                    totalToursCompleted = state.targetGuide.totalToursCompleted + 1
                )

                _uiState.value = state.copy(
                    targetGuide = updatedGuide,
                    reviews = updatedReviews,
                    customerNameInput = "",
                    commentInput = "",
                    selectedStar = 5,
                    successNotification = "⭐ Müşteri değerlendirmesi alındı! Rehber puanı ${updatedGuide.rating} olarak güncellendi."
                )
            }.onFailure { err ->
                _uiState.value = GuideRatingUiState.Error(err.message ?: "Değerlendirme gönderilemedi.")
            }
        }
    }
}
