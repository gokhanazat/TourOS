package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.TourDetail
import com.mgacreative.touros.domain.usecase.GetTourDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

sealed interface TourDetailUiState {
    data object Loading : TourDetailUiState
    data class Success(val tourDetail: TourDetail) : TourDetailUiState
    data class Error(val message: String) : TourDetailUiState
}

class TourDetailViewModel(
    private val getTourDetailUseCase: GetTourDetailUseCase,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<TourDetailUiState>(TourDetailUiState.Loading)
    val uiState: StateFlow<TourDetailUiState> = _uiState.asStateFlow()

    fun loadTourDetail(tourId: String) {
        viewModelScope.launch {
            _uiState.value = TourDetailUiState.Loading
            getTourDetailUseCase(tourId)
                .onSuccess { detail ->
                    _uiState.value = TourDetailUiState.Success(detail)
                }
                .onFailure { error ->
                    _uiState.value = TourDetailUiState.Error(
                        error.message ?: "Tur detayları yüklenirken hata oluştu"
                    )
                }
        }
    }

    fun deleteDeparture(departureId: String, tourId: String) {
        viewModelScope.launch {
            runCatching {
                supabaseClient.postgrest.from("departures")
                    .delete {
                        filter {
                            eq("id", departureId)
                        }
                    }
            }.onSuccess {
                loadTourDetail(tourId)
            }
        }
    }
}
