package com.android.practise.kata.android_indentity_architecture.domain.usecase

import com.android.practise.kata.android_indentity_architecture.domain.model.AuthState
import com.android.practise.kata.android_indentity_architecture.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveAuthStateUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): StateFlow<AuthState> = authRepository.authState
}
