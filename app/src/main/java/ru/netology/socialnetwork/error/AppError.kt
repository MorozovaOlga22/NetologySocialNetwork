package ru.netology.socialnetwork.error

sealed class AppError(var code: String) : RuntimeException() {
    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            else -> UnknownError
        }
    }
}

class ApiError(val status: Int, code: String) : AppError(code)
object UnknownError : AppError("error_unknown")