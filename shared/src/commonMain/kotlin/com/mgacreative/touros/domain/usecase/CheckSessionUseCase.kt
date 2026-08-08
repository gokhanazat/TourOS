package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.repository.AuthRepository

/**
 * Uygulama başlangıcında aktif oturum durumunu kontrol eder.
 * Geçerli bir oturum varsa kullanıcıyı döndürür, yoksa token yenilemeyi dener.
 */
class CheckSessionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User?> {
        return runCatching {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null && authRepository.isSessionValid()) {
                return@runCatching currentUser
            }

            // Token yenilemeyi dene
            val refreshResult = authRepository.refreshSession()
            if (refreshResult.isSuccess) {
                refreshResult.getOrNull()
            } else {
                null
            }
        }.recover { null }
    }
}
