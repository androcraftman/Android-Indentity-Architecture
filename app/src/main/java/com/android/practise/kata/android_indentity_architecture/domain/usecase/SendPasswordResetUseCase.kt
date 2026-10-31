package com.android.practise.kata.android_indentity_architecture.domain.usecase

import com.android.practise.kata.android_indentity_architecture.domain.repository.AuthRepository

class SendPasswordResetUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String): Result<Unit> =
        authRepository.sendPasswordReset(email = email)
}
