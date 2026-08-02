package com.restrusher.ecomercecarlosv.data.remote

import android.util.Log
import com.restrusher.ecomercecarlosv.data.remote.dto.UserDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thrown when an admin Edge Function call fails.
 *
 * [message] is developer-facing and goes to the log only. [serverMessage] is the Edge
 * Function's own Spanish text, present only when the server sent one — it is the *only* part
 * of this exception that may reach the UI. The underlying [RestException.message] must never
 * be shown: it embeds the request URL and headers, including the session's bearer token.
 */
class AdminOperationException(
    val serverMessage: String?,
    cause: Throwable? = null,
) : Exception("admin operation failed: ${serverMessage ?: "no server message"}", cause)

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
    suspend fun createUser(email: String, password: String, name: String, role: String): UserDto =
        adminCall("create-user") {
            supabase.functions.invoke(
                function = "create-user",
                body = CreateUserRequest(email = email, password = password, name = name, role = role),
            ).body()
        }

    suspend fun updateRole(userId: String, role: String) = adminCall("update-user-role") {
        supabase.functions.invoke(
            function = "update-user-role",
            body = UpdateRoleRequest(userId = userId, role = role),
        )
        Unit
    }

    suspend fun setActive(userId: String, isActive: Boolean) = adminCall("set-user-active") {
        supabase.functions.invoke(
            function = "set-user-active",
            body = SetActiveRequest(userId = userId, isActive = isActive),
        )
        Unit
    }

    suspend fun resetPassword(userId: String, newPassword: String) = adminCall("reset-user-password") {
        supabase.functions.invoke(
            function = "reset-user-password",
            body = ResetPasswordRequest(userId = userId, newPassword = newPassword),
        )
        Unit
    }

    suspend fun deleteUser(userId: String) = adminCall("delete-user") {
        supabase.functions.invoke(
            function = "delete-user",
            body = DeleteUserRequest(userId = userId),
        )
        Unit
    }

    /**
     * `functions.invoke` already throws [RestException] on any non-2xx, so there is no status to
     * inspect after it returns — every failure arrives here as an exception. This funnels all of
     * them into [AdminOperationException] so callers never touch the raw message.
     */
    private suspend fun <T> adminCall(function: String, block: suspend () -> T): T = try {
        block()
    } catch (e: RestException) {
        Log.w(TAG, "$function failed with ${e.statusCode}", e)
        throw AdminOperationException(parseServerError(e.error), e)
    } catch (e: CancellationException) {
        // The screen went away mid-call — not a failure, and swallowing it would break the
        // cancellation of whatever scope launched us.
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "$function failed", e)
        throw AdminOperationException(null, e)
    }

    // The Edge Functions answer with {"error": "<Spanish text>"}. Anything else — a gateway 401,
    // an undeployed function's 404 page — leaves the caller to use its own fallback string.
    private fun parseServerError(body: String): String? =
        runCatching { json.decodeFromString<ErrorResponse>(body).error }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }

    companion object {
        private const val TAG = "AdminUserService"
        private val json = Json { ignoreUnknownKeys = true }
    }

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
