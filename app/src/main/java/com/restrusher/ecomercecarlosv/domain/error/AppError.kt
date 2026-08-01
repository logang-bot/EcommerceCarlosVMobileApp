package com.restrusher.ecomercecarlosv.domain.error

import androidx.annotation.StringRes
import com.restrusher.ecomercecarlosv.R

/**
 * An error worth telling the user about.
 *
 * [message] is developer-facing and goes to the log only — it may carry entity ids, HTTP bodies and
 * stack context. Anything shown on screen must come from [userMessageRes] instead, so technical
 * detail can never leak into the UI.
 */
sealed class AppError(
    val message: String,
    @StringRes val userMessageRes: Int,
    val cause: Throwable? = null,
) {
    class Network(message: String, cause: Throwable? = null) :
        AppError(message, R.string.error_network_generic, cause)

    class Database(message: String, cause: Throwable? = null) :
        AppError(message, R.string.error_generic, cause)

    class Sync(message: String, cause: Throwable? = null) :
        AppError(message, R.string.error_sync_generic, cause)

    class Queue(message: String, cause: Throwable? = null) :
        AppError(message, R.string.error_queue_generic, cause)

    class Unknown(message: String, cause: Throwable? = null) :
        AppError(message, R.string.error_generic, cause)
}
