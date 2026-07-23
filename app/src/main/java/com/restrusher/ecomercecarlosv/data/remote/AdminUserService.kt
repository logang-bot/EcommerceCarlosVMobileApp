package com.restrusher.ecomercecarlosv.data.remote

import com.restrusher.ecomercecarlosv.data.remote.dto.UserDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thrown when an admin Edge Function returns a non-2xx response.
 * [message] carries the server's Spanish error string when available.
 */
class AdminOperationException(message: String) : Exception(message)

/**
 * Client-side wrapper around the privileged admin Edge Functions.
 *
 * These replace the old `@AdminClient` (service-role) direct calls: creating,
 * updating and deleting Supabase Auth users now happens server-side, so the
 * service-role secret never ships in the APK. The regular [SupabaseClient]
 * forwards the current session's JWT with each invocation; the functions verify
 * it and enforce the SUPERUSUARIO check before acting.
 */
@Singleton
class AdminUserService @Inject constructor(
    private val supabase: SupabaseClient,
) {
    /** Creates the auth user + `users` row server-side; returns the created profile. */
    suspend fun createUser(email: String, password: String, name: String, role: String): UserDto {
        val response = supabase.functions.invoke(
            function = "create-user",
            body = CreateUserRequest(email = email, password = password, name = name, role = role),
        )
        return response.decodeOrThrow()
    }

    suspend fun updateRole(userId: String, role: String) {
        supabase.functions.invoke(
            function = "update-user-role",
            body = UpdateRoleRequest(userId = userId, role = role),
        ).ensureSuccess()
    }

    suspend fun setActive(userId: String, isActive: Boolean) {
        supabase.functions.invoke(
            function = "set-user-active",
            body = SetActiveRequest(userId = userId, isActive = isActive),
        ).ensureSuccess()
    }

    suspend fun resetPassword(userId: String, newPassword: String) {
        supabase.functions.invoke(
            function = "reset-user-password",
            body = ResetPasswordRequest(userId = userId, newPassword = newPassword),
        ).ensureSuccess()
    }

    suspend fun deleteUser(userId: String) {
        supabase.functions.invoke(
            function = "delete-user",
            body = DeleteUserRequest(userId = userId),
        ).ensureSuccess()
    }

    private suspend fun HttpResponse.ensureSuccess() {
        if (!status.isSuccess()) throw AdminOperationException(errorMessage())
    }

    private suspend inline fun <reified T> HttpResponse.decodeOrThrow(): T {
        if (!status.isSuccess()) throw AdminOperationException(errorMessage())
        return body()
    }

    private suspend fun HttpResponse.errorMessage(): String =
        runCatching { body<ErrorResponse>().error }.getOrNull()
            ?: runCatching { bodyAsText() }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: "Error del servidor (${status.value})"

    // ─── Request/response payloads ───────────────────────────────
    @Serializable
    private data class CreateUserRequest(
        val email: String,
        val password: String,
        val name: String,
        val role: String,
    )

    @Serializable
    private data class UpdateRoleRequest(val userId: String, val role: String)

    @Serializable
    private data class SetActiveRequest(val userId: String, val isActive: Boolean)

    @Serializable
    private data class ResetPasswordRequest(val userId: String, val newPassword: String)

    @Serializable
    private data class DeleteUserRequest(val userId: String)

    @Serializable
    private data class ErrorResponse(val error: String)
}
