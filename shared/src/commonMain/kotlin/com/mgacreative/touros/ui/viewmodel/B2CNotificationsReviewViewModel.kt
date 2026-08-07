package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.B2CPushNotificationItem
import com.mgacreative.touros.domain.model.B2CTourReviewRequest
import com.mgacreative.touros.domain.usecase.GetB2CPushNotificationsUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.SubmitB2CTourReviewUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class B2CNotificationsReviewUiState(
    val selectedTab: Int = 0, // 0: Bildirimler, 1: Tur Değerlendirme
    val notifications: List<B2CPushNotificationItem> = emptyList(),
    val selectedTourTitle: String = "Kapadokya Balon & Vadi Turu",
    val rating: Double = 5.0,
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
) {
    val unreadCount: Int get() = notifications.count { !it.isRead }
}

class B2CNotificationsReviewViewModel(
    private val getB2CPushNotificationsUseCase: GetB2CPushNotificationsUseCase,
    private val submitB2CTourReviewUseCase: SubmitB2CTourReviewUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(B2CNotificationsReviewUiState())
    val uiState: StateFlow<B2CNotificationsReviewUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getB2CPushNotificationsUseCase(tenantId)
            res.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    notifications = list,
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

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun updateRating(rating: Double) {
        _uiState.value = _uiState.value.copy(rating = rating)
    }

    fun submitReview(comment: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val req = B2CTourReviewRequest(
                tourId = "t101",
                rating = _uiState.value.rating,
                comment = comment
            )

            val res = submitB2CTourReviewUseCase(req, tenantId)
            res.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    notificationMessage = result.message
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Değerlendirme kaydedilemedi."
                )
            }
        }
    }
}
