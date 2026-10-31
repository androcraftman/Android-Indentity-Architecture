package com.android.practise.kata.android_indentity_architecture.domain.usecase

import com.android.practise.kata.android_indentity_architecture.data.repository.FakeAuthRepository
import com.android.practise.kata.android_indentity_architecture.domain.model.AuthState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthUseCasesTest {

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var signInUseCase: SignInUseCase
    private lateinit var signUpUseCase: SignUpUseCase
    private lateinit var observeAuthStateUseCase: ObserveAuthStateUseCase
    private lateinit var sendEmailVerificationUseCase: SendEmailVerificationUseCase
    private lateinit var sendPasswordResetUseCase: SendPasswordResetUseCase
    private lateinit var reloadUserSessionUseCase: ReloadUserSessionUseCase
    private lateinit var signOutUseCase: SignOutUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeAuthRepository()
        signInUseCase = SignInUseCase(fakeRepository)
        signUpUseCase = SignUpUseCase(fakeRepository)
        observeAuthStateUseCase = ObserveAuthStateUseCase(fakeRepository)
        sendEmailVerificationUseCase = SendEmailVerificationUseCase(fakeRepository)
        sendPasswordResetUseCase = SendPasswordResetUseCase(fakeRepository)
        reloadUserSessionUseCase = ReloadUserSessionUseCase(fakeRepository)
        signOutUseCase = SignOutUseCase(fakeRepository)
    }

    @Test
    fun signUpAndObserveState_invokesRepositoryAndTransitionsState() = runTest {
        val result = signUpUseCase("dev@androcraft.com", "SecurePass123!")

        assertThat(result.isSuccess).isTrue()
        assertThat(observeAuthStateUseCase().value).isInstanceOf(AuthState.UnverifiedEmail::class.java)
    }

    @Test
    fun signInWithPreloadedAccount_invokesRepositorySuccessfully() = runTest {
        fakeRepository.preloadAccount(
            email = "dev@androcraft.com",
            passwordHash = "SecurePass123!",
            isEmailVerified = true,
        )

        val result = signInUseCase("dev@androcraft.com", "SecurePass123!")

        assertThat(result.isSuccess).isTrue()
        assertThat(observeAuthStateUseCase().value).isInstanceOf(AuthState.Authenticated::class.java)
    }

    @Test
    fun signOutUseCase_resetsStateToUnauthenticated() = runTest {
        fakeRepository.preloadAccount(
            email = "dev@androcraft.com",
            passwordHash = "SecurePass123!",
            isEmailVerified = true,
        )
        signInUseCase("dev@androcraft.com", "SecurePass123!")

        val result = signOutUseCase()

        assertThat(result.isSuccess).isTrue()
        assertThat(observeAuthStateUseCase().value).isEqualTo(AuthState.Unauthenticated)
    }
}
