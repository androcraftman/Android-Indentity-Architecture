package com.android.practise.kata.android_indentity_architecture.data.repository

import app.cash.turbine.test
import com.android.practise.kata.android_indentity_architecture.domain.model.AuthException
import com.android.practise.kata.android_indentity_architecture.domain.model.AuthState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FakeAuthRepositoryTest {

    private lateinit var repository: FakeAuthRepository

    @Before
    fun setUp() {
        repository = FakeAuthRepository()
    }

    @Test
    fun initialAuthState_isUnauthenticated() = runTest {
        assertThat(repository.authState.value).isEqualTo(AuthState.Unauthenticated)
    }

    @Test
    fun signUpWithValidCredentials_transitionsStateToUnverifiedEmail() = runTest {
        val result = repository.signUpWithEmail("test@example.com", "Password123!")

        assertThat(result.isSuccess).isTrue()
        val session = result.getOrThrow()
        assertThat(session.email).isEqualTo("test@example.com")
        assertThat(session.isEmailVerified).isFalse()

        val currentState = repository.authState.value
        assertThat(currentState).isInstanceOf(AuthState.UnverifiedEmail::class.java)
        assertThat((currentState as AuthState.UnverifiedEmail).user.email).isEqualTo("test@example.com")
        assertThat(repository.getVerificationEmailDispatchCount()).isEqualTo(1)
    }

    @Test
    fun signUpWithDuplicateEmail_failsWithUserCollisionException() = runTest {
        repository.signUpWithEmail("test@example.com", "Password123!")
        val duplicateResult = repository.signUpWithEmail("test@example.com", "DifferentPassword123!")

        assertThat(duplicateResult.isFailure).isTrue()
        assertThat(duplicateResult.exceptionOrNull()).isInstanceOf(AuthException.UserCollisionException::class.java)
    }

    @Test
    fun signUpWithWeakPassword_failsWithWeakPasswordException() = runTest {
        val result = repository.signUpWithEmail("weak@example.com", "123")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(AuthException.WeakPasswordException::class.java)
    }

    @Test
    fun signInWithUnverifiedAccount_transitionsToUnverifiedEmail() = runTest {
        repository.preloadAccount(
            email = "unverified@example.com",
            passwordHash = "Secret123!",
            isEmailVerified = false,
        )

        val result = repository.signInWithEmail("unverified@example.com", "Secret123!")

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.authState.value).isInstanceOf(AuthState.UnverifiedEmail::class.java)
    }

    @Test
    fun signInWithVerifiedAccount_transitionsToAuthenticated() = runTest {
        repository.preloadAccount(
            email = "verified@example.com",
            passwordHash = "Secret123!",
            isEmailVerified = true,
        )

        val result = repository.signInWithEmail("verified@example.com", "Secret123!")

        assertThat(result.isSuccess).isTrue()
        val state = repository.authState.value
        assertThat(state).isInstanceOf(AuthState.Authenticated::class.java)
        assertThat((state as AuthState.Authenticated).user.email).isEqualTo("verified@example.com")
    }

    @Test
    fun signInWithInvalidPassword_failsWithInvalidCredentialsException() = runTest {
        repository.preloadAccount(
            email = "user@example.com",
            passwordHash = "CorrectPassword123!",
            isEmailVerified = true,
        )

        val result = repository.signInWithEmail("user@example.com", "WrongPassword")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(AuthException.InvalidCredentialsException::class.java)
    }

    @Test
    fun signInWithNonExistentAccount_failsWithUserNotFoundException() = runTest {
        val result = repository.signInWithEmail("nonexistent@example.com", "Password123!")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(AuthException.UserNotFoundException::class.java)
    }

    @Test
    fun reloadUserSession_updatesToAuthenticatedWhenEmailVerified() = runTest {
        repository.preloadAccount(
            email = "candidate@example.com",
            passwordHash = "Password123!",
            isEmailVerified = false,
        )
        repository.signInWithEmail("candidate@example.com", "Password123!")
        assertThat(repository.authState.value).isInstanceOf(AuthState.UnverifiedEmail::class.java)

        repository.markEmailVerified("candidate@example.com")
        val reloadResult = repository.reloadUserSession()

        assertThat(reloadResult.isSuccess).isTrue()
        assertThat(repository.authState.value).isInstanceOf(AuthState.Authenticated::class.java)
    }

    @Test
    fun sendPasswordReset_succeedsForExistingAccount() = runTest {
        repository.preloadAccount(
            email = "reset@example.com",
            passwordHash = "OldPassword123!",
            isEmailVerified = true,
        )

        val result = repository.sendPasswordReset("reset@example.com")

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.getPasswordResetDispatchCount()).isEqualTo(1)
    }

    @Test
    fun sendPasswordReset_failsForUnknownAccount() = runTest {
        val result = repository.sendPasswordReset("unknown@example.com")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(AuthException.UserNotFoundException::class.java)
    }

    @Test
    fun signOut_clearsSessionAndEmitsUnauthenticated() = runTest {
        repository.preloadAccount(
            email = "signout@example.com",
            passwordHash = "Password123!",
            isEmailVerified = true,
        )
        repository.signInWithEmail("signout@example.com", "Password123!")
        assertThat(repository.authState.value).isInstanceOf(AuthState.Authenticated::class.java)

        val result = repository.signOut()

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.authState.value).isEqualTo(AuthState.Unauthenticated)
    }

    @Test
    fun simulateNetworkError_causesOperationsToFailWithNetworkException() = runTest {
        repository.simulateNetworkError = true

        val signInResult = repository.signInWithEmail("any@example.com", "password")
        val signUpResult = repository.signUpWithEmail("any@example.com", "password")
        val resetResult = repository.sendPasswordReset("any@example.com")

        assertThat(signInResult.exceptionOrNull()).isInstanceOf(AuthException.NetworkException::class.java)
        assertThat(signUpResult.exceptionOrNull()).isInstanceOf(AuthException.NetworkException::class.java)
        assertThat(resetResult.exceptionOrNull()).isInstanceOf(AuthException.NetworkException::class.java)
    }

    @Test
    fun authStateFlow_emitsStateTransitionsInRealTime() = runTest {
        repository.authState.test {
            assertThat(awaitItem()).isEqualTo(AuthState.Unauthenticated)

            repository.signUpWithEmail("flow@example.com", "Password123!")
            val unverified = awaitItem()
            assertThat(unverified).isInstanceOf(AuthState.UnverifiedEmail::class.java)

            repository.signOut()
            assertThat(awaitItem()).isEqualTo(AuthState.Unauthenticated)
        }
    }
}
