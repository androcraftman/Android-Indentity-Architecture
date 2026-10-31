package com.android.practise.kata.android_indentity_architecture.domain.usecase

import com.android.practise.kata.android_indentity_architecture.data.repository.FakeAuthRepository
import com.android.practise.kata.android_indentity_architecture.domain.model.AuthException
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
        val state = observeAuthStateUseCase().value
        assertThat(state).isInstanceOf(AuthState.UnverifiedEmail::class.java)
        assertThat((state as AuthState.UnverifiedEmail).user.email).isEqualTo("dev@androcraft.com")
    }

    @Test
    fun signUpUseCase_propagatesFailure_whenWeakPassword() = runTest {
        val result = signUpUseCase("dev@androcraft.com", "123")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(AuthException.WeakPasswordException::class.java)
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
    fun signInUseCase_propagatesFailure_whenCredentialsInvalid() = runTest {
        fakeRepository.preloadAccount(
            email = "dev@androcraft.com",
            passwordHash = "SecurePass123!",
            isEmailVerified = true,
        )

        val result = signInUseCase("dev@androcraft.com", "WrongPassword")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(AuthException.InvalidCredentialsException::class.java)
    }

    @Test
    fun sendEmailVerificationUseCase_triggersRepository_andReturnsSuccess() = runTest {
        signUpUseCase("verify@androcraft.com", "SecurePass123!")

        val result = sendEmailVerificationUseCase()

        assertThat(result.isSuccess).isTrue()
        assertThat(fakeRepository.getVerificationEmailDispatchCount()).isEqualTo(2)
    }

    @Test
    fun sendEmailVerificationUseCase_fails_whenUnauthenticated() = runTest {
        val result = sendEmailVerificationUseCase()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(AuthException.UserNotFoundException::class.java)
    }

    @Test
    fun sendPasswordResetUseCase_triggersRepository_andReturnsSuccess() = runTest {
        fakeRepository.preloadAccount(
            email = "reset@androcraft.com",
            passwordHash = "Secret123!",
            isEmailVerified = true,
        )

        val result = sendPasswordResetUseCase("reset@androcraft.com")

        assertThat(result.isSuccess).isTrue()
        assertThat(fakeRepository.getPasswordResetDispatchCount()).isEqualTo(1)
    }

    @Test
    fun sendPasswordResetUseCase_fails_whenAccountNotFound() = runTest {
        val result = sendPasswordResetUseCase("missing@androcraft.com")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(AuthException.UserNotFoundException::class.java)
    }

    @Test
    fun reloadUserSessionUseCase_refreshesVerificationState() = runTest {
        fakeRepository.preloadAccount(
            email = "unverified@androcraft.com",
            passwordHash = "Secret123!",
            isEmailVerified = false,
        )
        signInUseCase("unverified@androcraft.com", "Secret123!")
        assertThat(observeAuthStateUseCase().value).isInstanceOf(AuthState.UnverifiedEmail::class.java)

        fakeRepository.markEmailVerified("unverified@androcraft.com")
        val result = reloadUserSessionUseCase()

        assertThat(result.isSuccess).isTrue()
        assertThat(observeAuthStateUseCase().value).isInstanceOf(AuthState.Authenticated::class.java)
    }

    @Test
    fun reloadUserSessionUseCase_fails_whenSessionDoesNotExist() = runTest {
        val result = reloadUserSessionUseCase()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(AuthException.UserNotFoundException::class.java)
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
