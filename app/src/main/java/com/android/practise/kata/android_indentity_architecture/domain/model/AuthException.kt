package com.android.practise.kata.android_indentity_architecture.domain.model

sealed class AuthException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class InvalidCredentialsException(
        message: String = "Invalid email or password",
        cause: Throwable? = null,
    ) : AuthException(message, cause)

    class UserNotFoundException(
        message: String = "No account found with this email",
        cause: Throwable? = null,
    ) : AuthException(message, cause)

    class UserCollisionException(
        message: String = "An account already exists with this email address",
        cause: Throwable? = null,
    ) : AuthException(message, cause)

    class WeakPasswordException(
        message: String = "The provided password does not meet security requirements",
        cause: Throwable? = null,
    ) : AuthException(message, cause)

    class NetworkException(
        message: String = "Network connection failed. Please check your internet connection",
        cause: Throwable? = null,
    ) : AuthException(message, cause)

    class TooManyRequestsException(
        message: String = "Too many requests. Please try again later",
        cause: Throwable? = null,
    ) : AuthException(message, cause)

    class UnknownAuthException(
        message: String = "An unexpected authentication error occurred",
        cause: Throwable? = null,
    ) : AuthException(message, cause)
}
