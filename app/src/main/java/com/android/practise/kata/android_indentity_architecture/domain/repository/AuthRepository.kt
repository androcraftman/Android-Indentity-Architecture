package com.android.practise.kata.android_indentity_architecture.domain.repository

import com.android.practise.kata.android_indentity_architecture.domain.model.AuthState
import com.android.practise.kata.android_indentity_architecture.domain.model.UserSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun signInWithEmail(email: String, password: String): Result<UserSession>

    suspend fun signUpWithEmail(email: String, password: String): Result<UserSession>

    suspend fun sendEmailVerification(): Result<Unit>

    suspend fun sendPasswordReset(email: String): Result<Unit>

    suspend fun reloadUserSession(): Result<UserSession>

    suspend fun signOut(): Result<Unit>
}
