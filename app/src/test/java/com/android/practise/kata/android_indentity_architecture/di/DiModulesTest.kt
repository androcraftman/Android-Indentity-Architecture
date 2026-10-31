package com.android.practise.kata.android_indentity_architecture.di

import com.android.practise.kata.android_indentity_architecture.data.repository.FakeAuthRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DiModulesTest {

    @Test
    fun repositoryModule_providesFakeAuthRepositoryInstance() {
        val repository = RepositoryModule.provideAuthRepository()

        assertThat(repository).isNotNull()
        assertThat(repository).isInstanceOf(FakeAuthRepository::class.java)
    }

    @Test
    fun useCaseModule_providesAllConfiguredUseCases() {
        val repository = RepositoryModule.provideAuthRepository()

        val observeUseCase = UseCaseModule.provideObserveAuthStateUseCase(repository)
        val signInUseCase = UseCaseModule.provideSignInUseCase(repository)
        val signUpUseCase = UseCaseModule.provideSignUpUseCase(repository)
        val sendEmailVerificationUseCase = UseCaseModule.provideSendEmailVerificationUseCase(repository)
        val sendPasswordResetUseCase = UseCaseModule.provideSendPasswordResetUseCase(repository)
        val reloadUserSessionUseCase = UseCaseModule.provideReloadUserSessionUseCase(repository)
        val signOutUseCase = UseCaseModule.provideSignOutUseCase(repository)

        assertThat(observeUseCase).isNotNull()
        assertThat(signInUseCase).isNotNull()
        assertThat(signUpUseCase).isNotNull()
        assertThat(sendEmailVerificationUseCase).isNotNull()
        assertThat(sendPasswordResetUseCase).isNotNull()
        assertThat(reloadUserSessionUseCase).isNotNull()
        assertThat(signOutUseCase).isNotNull()
    }
}
