package com.android.practise.kata.android_indentity_architecture.domain.usecase

import com.android.practise.kata.android_indentity_architecture.domain.model.UserSession
import com.android.practise.kata.android_indentity_architecture.domain.repository.AuthRepository

class SignUpUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<UserSession> =
        authRepository.signUpWithEmail(email = email, password = password)
}
