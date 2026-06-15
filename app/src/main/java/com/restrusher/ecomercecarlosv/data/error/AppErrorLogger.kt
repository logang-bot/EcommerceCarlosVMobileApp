package com.restrusher.ecomercecarlosv.data.error

import android.util.Log
import com.restrusher.ecomercecarlosv.domain.error.AppError

object AppErrorLogger {

    fun log(error: AppError, tag: String = "AppError") {
        val label = error::class.simpleName ?: "Error"
        when (error) {
            is AppError.Network  -> Log.e(tag, "[$label] ${error.message}", error.cause)
            is AppError.Database -> Log.e(tag, "[$label] ${error.message}", error.cause)
            is AppError.Sync     -> Log.w(tag, "[$label] ${error.message}", error.cause)
            is AppError.Queue    -> Log.w(tag, "[$label] ${error.message}", error.cause)
            is AppError.Unknown  -> Log.e(tag, "[$label] ${error.message}", error.cause)
        }
    }

    fun logThrowable(throwable: Throwable, context: String, tag: String = "AppError") {
        Log.e(tag, "[Unexpected] $context", throwable)
    }
}
