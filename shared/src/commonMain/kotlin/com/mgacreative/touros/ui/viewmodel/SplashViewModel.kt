package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.usecase.CheckSessionUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SplashState {
    data object Loading : SplashState
    data class Authenticated(val role: UserRole) : SplashState
    data object Unauthenticated : SplashState
}

class SplashViewModel(
    private val checkSessionUseCase: CheckSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashState>(SplashState.Loading)
    val uiState: StateFlow<SplashState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _uiState.value = SplashState.Loading
            // Min 1000ms splash animasyon görünürlüğü
            val startTime = currentTimeMillis()

            val userResult = checkSessionUseCase()
            val elapsedTime = currentTimeMillis() - startTime
            if (elapsedTime < 1000) {
                delay(1000 - elapsedTime)
            }

            val user = userResult.getOrNull()
            if (user != null) {
                _uiState.value = SplashState.Authenticated(user.role)
            } else {
                _uiState.value = SplashState.Unauthenticated
            }
        }
    }

    private fun currentTimeMillis(): Long {
        return kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds
    }
}
