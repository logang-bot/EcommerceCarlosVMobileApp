package com.restrusher.ecomercecarlosv.data.error

import com.restrusher.ecomercecarlosv.domain.error.AppError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalErrorHandler @Inject constructor() {

    private val _errors = MutableSharedFlow<AppError>(extraBufferCapacity = 8)
    val errors: SharedFlow<AppError> = _errors.asSharedFlow()

    fun emit(error: AppError) {
        AppErrorLogger.log(error)
        _errors.tryEmit(error)
    }

    fun emit(throwable: Throwable, context: String) {
        val error = AppError.Unknown("$context: ${throwable.message}", throwable)
        emit(error)
    }
}
