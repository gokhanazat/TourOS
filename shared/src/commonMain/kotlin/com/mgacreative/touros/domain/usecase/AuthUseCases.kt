package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, agencyCode: String = ""): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("E-posta ve şifre boş olamaz"))
        }
        return authRepository.signInWithEmail(email.trim(), password, agencyCode.trim())
    }
}

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, fullName: String): Result<User> {
        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("Tüm alanlar doldurulmalıdır"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Şifre en az 6 karakter olmalıdır"))
        }
        return authRepository.signUpWithEmail(email.trim(), password, fullName.trim())
    }
}

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }

    fun observe(): StateFlow<User?> {
        return authRepository.observeAuthState()
    }
}

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut()
    }
}
