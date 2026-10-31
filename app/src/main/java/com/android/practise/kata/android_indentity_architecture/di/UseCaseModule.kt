package com.android.practise.kata.android_indentity_architecture.di

import com.android.practise.kata.android_indentity_architecture.domain.repository.AuthRepository
import com.android.practise.kata.android_indentity_architecture.domain.usecase.ObserveAuthStateUseCase
import com.android.practise.kata.android_indentity_architecture.domain.usecase.ReloadUserSessionUseCase
import com.android.practise.kata.android_indentity_architecture.domain.usecase.SendEmailVerificationUseCase
import com.android.practise.kata.android_indentity_architecture.domain.usecase.SendPasswordResetUseCase
import com.android.practise.kata.android_indentity_architecture.domain.usecase.SignInUseCase
import com.android.practise.kata.android_indentity_architecture.domain.usecase.SignOutUseCase
import com.android.practise.kata.android_indentity_architecture.domain.usecase.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideObserveAuthStateUseCase(repository: AuthRepository): ObserveAuthStateUseCase =
        ObserveAuthStateUseCase(repository)

    @Provides
    @Singleton
    fun provideSignInUseCase(repository: AuthRepository): SignInUseCase =
        SignInUseCase(repository)

    @Provides
    @Singleton
    fun provideSignUpUseCase(repository: AuthRepository): SignUpUseCase =
        SignUpUseCase(repository)

    @Provides
    @Singleton
    fun provideSendEmailVerificationUseCase(repository: AuthRepository): SendEmailVerificationUseCase =
        SendEmailVerificationUseCase(repository)

    @Provides
    @Singleton
    fun provideSendPasswordResetUseCase(repository: AuthRepository): SendPasswordResetUseCase =
        SendPasswordResetUseCase(repository)

    @Provides
    @Singleton
    fun provideReloadUserSessionUseCase(repository: AuthRepository): ReloadUserSessionUseCase =
        ReloadUserSessionUseCase(repository)

    @Provides
    @Singleton
    fun provideSignOutUseCase(repository: AuthRepository): SignOutUseCase =
        SignOutUseCase(repository)
}
