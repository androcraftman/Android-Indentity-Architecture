package com.android.practise.kata.android_indentity_architecture.domain.model

data class UserSession(
    val uid: String,
    val email: String,
    val isEmailVerified: Boolean,
    val providerId: String = "password",
)
