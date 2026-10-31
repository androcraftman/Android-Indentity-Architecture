package com.android.practise.kata.android_indentity_architecture.di

import com.android.practise.kata.android_indentity_architecture.data.repository.FakeAuthRepository
import com.android.practise.kata.android_indentity_architecture.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = FakeAuthRepository()
}
