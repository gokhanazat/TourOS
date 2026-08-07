package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.repository.AuthRepository

class ForgotPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun sendResetEmail(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("E-posta adresi boş olamaz"))
        }
        return authRepository.sendPasswordResetEmail(email.trim())
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Şifre en az 6 karakter olmalıdır"))
        }
        return authRepository.resetPassword(newPassword)
    }
}
