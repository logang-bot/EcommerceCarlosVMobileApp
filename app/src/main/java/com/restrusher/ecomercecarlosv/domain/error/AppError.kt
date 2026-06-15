package com.restrusher.ecomercecarlosv.domain.error

sealed class AppError(val message: String, val cause: Throwable? = null) {
    class Network(message: String, cause: Throwable? = null) : AppError(message, cause)
    class Database(message: String, cause: Throwable? = null) : AppError(message, cause)
    class Sync(message: String, cause: Throwable? = null) : AppError(message, cause)
    class Queue(message: String, cause: Throwable? = null) : AppError(message, cause)
    class Unknown(message: String, cause: Throwable? = null) : AppError(message, cause)
}
