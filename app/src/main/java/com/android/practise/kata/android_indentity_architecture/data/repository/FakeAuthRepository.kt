package com.android.practise.kata.android_indentity_architecture.data.repository

import com.android.practise.kata.android_indentity_architecture.domain.model.AuthException
import com.android.practise.kata.android_indentity_architecture.domain.model.AuthState
import com.android.practise.kata.android_indentity_architecture.domain.model.UserSession
import com.android.practise.kata.android_indentity_architecture.domain.repository.AuthRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository(
    initialState: AuthState = AuthState.Unauthenticated,
) : AuthRepository {

    private data class StoredAccount(
        val uid: String,
        val email: String,
        val passwordHash: String,
        var isEmailVerified: Boolean,
    )

    private val accounts = ConcurrentHashMap<String, StoredAccount>()
    private val _authState = MutableStateFlow<AuthState>(initialState)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val verificationEmailDispatches = AtomicInteger(0)
    private val passwordResetDispatches = AtomicInteger(0)

    var simulateNetworkError: Boolean = false

    fun getVerificationEmailDispatchCount(): Int = verificationEmailDispatches.get()

    fun getPasswordResetDispatchCount(): Int = passwordResetDispatches.get()

    fun preloadAccount(
        email: String,
        passwordHash: String,
        isEmailVerified: Boolean,
        uid: String = UUID.randomUUID().toString(),
    ): UserSession {
        val account = StoredAccount(
            uid = uid,
            email = email.lowercase(),
            passwordHash = passwordHash,
            isEmailVerified = isEmailVerified,
        )
        accounts[account.email] = account
        return UserSession(
            uid = account.uid,
            email = account.email,
            isEmailVerified = account.isEmailVerified,
        )
    }

    fun markEmailVerified(email: String) {
        accounts[email.lowercase()]?.isEmailVerified = true
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<UserSession> {
        val outcome: Result<UserSession> = when {
            simulateNetworkError -> Result.failure(AuthException.NetworkException())
            else -> {
                val account = accounts[email.lowercase()]
                when {
                    account == null -> Result.failure(AuthException.UserNotFoundException())
                    account.passwordHash != password -> Result.failure(AuthException.InvalidCredentialsException())
                    else -> {
                        val session = UserSession(
                            uid = account.uid,
                            email = account.email,
                            isEmailVerified = account.isEmailVerified,
                        )
                        _authState.value = if (session.isEmailVerified) {
                            AuthState.Authenticated(session)
                        } else {
                            AuthState.UnverifiedEmail(session)
                        }
                        Result.success(session)
                    }
                }
            }
        }
        return outcome
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<UserSession> {
        val outcome: Result<UserSession> = when {
            simulateNetworkError -> Result.failure(AuthException.NetworkException())
            accounts.containsKey(email.lowercase()) -> Result.failure(AuthException.UserCollisionException())
            password.length < 6 -> Result.failure(AuthException.WeakPasswordException())
            else -> {
                val newAccount = StoredAccount(
                    uid = UUID.randomUUID().toString(),
                    email = email.lowercase(),
                    passwordHash = password,
                    isEmailVerified = false,
                )
                accounts[newAccount.email] = newAccount
                verificationEmailDispatches.incrementAndGet()

                val session = UserSession(
                    uid = newAccount.uid,
                    email = newAccount.email,
                    isEmailVerified = false,
                )
                _authState.value = AuthState.UnverifiedEmail(session)
                Result.success(session)
            }
        }
        return outcome
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        val current = _authState.value
        val email = when (current) {
            is AuthState.UnverifiedEmail -> current.user.email
            is AuthState.Authenticated -> current.user.email
            else -> null
        }

        val outcome: Result<Unit> = when {
            simulateNetworkError -> Result.failure(AuthException.NetworkException())
            email == null || !accounts.containsKey(email.lowercase()) -> {
                Result.failure(AuthException.UserNotFoundException())
            }
            else -> {
                verificationEmailDispatches.incrementAndGet()
                Result.success(Unit)
            }
        }
        return outcome
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        val outcome: Result<Unit> = when {
            simulateNetworkError -> Result.failure(AuthException.NetworkException())
            !accounts.containsKey(email.lowercase()) -> Result.failure(AuthException.UserNotFoundException())
            else -> {
                passwordResetDispatches.incrementAndGet()
                Result.success(Unit)
            }
        }
        return outcome
    }

    override suspend fun reloadUserSession(): Result<UserSession> {
        val current = _authState.value
        val email = when (current) {
            is AuthState.UnverifiedEmail -> current.user.email
            is AuthState.Authenticated -> current.user.email
            else -> null
        }

        val account = email?.let { accounts[it.lowercase()] }

        val outcome: Result<UserSession> = when {
            simulateNetworkError -> Result.failure(AuthException.NetworkException())
            account == null -> Result.failure(AuthException.UserNotFoundException())
            else -> {
                val updatedSession = UserSession(
                    uid = account.uid,
                    email = account.email,
                    isEmailVerified = account.isEmailVerified,
                )
                _authState.value = if (updatedSession.isEmailVerified) {
                    AuthState.Authenticated(updatedSession)
                } else {
                    AuthState.UnverifiedEmail(updatedSession)
                }
                Result.success(updatedSession)
            }
        }
        return outcome
    }

    override suspend fun signOut(): Result<Unit> {
        _authState.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }
}
