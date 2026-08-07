package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.repository.AuthRepository

class VerifyEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun resendVerificationEmail(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("E-posta adresi boş olamaz"))
        }
        return authRepository.resendVerificationEmail(email.trim())
    }

    suspend fun isEmailVerified(): Boolean {
        val currentUser = authRepository.getCurrentUser()
        return currentUser?.isEmailVerified == true
    }
}
