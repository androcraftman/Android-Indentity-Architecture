package com.android.practise.kata.android_indentity_architecture.domain.usecase

import com.android.practise.kata.android_indentity_architecture.domain.model.UserSession
import com.android.practise.kata.android_indentity_architecture.domain.repository.AuthRepository

class SignInUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<UserSession> =
        authRepository.signInWithEmail(email = email, password = password)
}
