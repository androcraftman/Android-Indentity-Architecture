package com.android.practise.kata.android_indentity_architecture.domain.model

sealed interface AuthState {
    data object Loading : AuthState
    data class Authenticated(val user: UserSession) : AuthState
    data class UnverifiedEmail(val user: UserSession) : AuthState
    data object Unauthenticated : AuthState
}
