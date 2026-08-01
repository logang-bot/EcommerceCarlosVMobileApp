package com.restrusher.ecomercecarlosv.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreGoTrueSessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SessionManager {

    companion object {
        private val SESSION_KEY = stringPreferencesKey("supabase_jwt_session")

        // Mirrors the refresh token of the last successful login, tagged with its owning user id.
        // Unlike SESSION_KEY this survives the startup wipe below, so it is the only thing the app
        // can present to Supabase to obtain a fresh access token without a password — which is what
        // makes fingerprint login work. See SessionManagerImpl.ensureValidSession.
        // The stored key strings still say "biometric" for backwards compatibility with installs
        // written before this token was used for non-enrolled users too — do not rename them.
        private val LAST_REFRESH_TOKEN_KEY = stringPreferencesKey("biometric_refresh_token")
        private val LAST_REFRESH_USER_ID_KEY = stringPreferencesKey("biometric_refresh_user_id")

        private val json = Json { ignoreUnknownKeys = true }
    }

    override suspend fun saveSession(session: UserSession) {
        dataStore.edit { prefs ->
            prefs[SESSION_KEY] = json.encodeToString(session)
            val userId = session.user?.id
            if (session.refreshToken.isNotBlank() && userId != null) {
                prefs[LAST_REFRESH_TOKEN_KEY] = session.refreshToken
                prefs[LAST_REFRESH_USER_ID_KEY] = userId
            }
        }
    }

    // On the very first load (app startup), wipe any persisted session so the user is always
    // required to log in. Subsequent calls (token refresh mid-session) load normally.
    @Volatile private var firstLoad = true

    override suspend fun loadSession(): UserSession? {
        if (firstLoad) {
            firstLoad = false
            deleteSession()
            return null
        }
        val prefs = dataStore.data.first()
        val raw = prefs[SESSION_KEY] ?: return null
        return try {
            json.decodeFromString<UserSession>(raw)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun deleteSession() {
        dataStore.edit { prefs -> prefs.remove(SESSION_KEY) }
    }

    // Returns the mirrored refresh token only if it belongs to [userId] — guards against reusing
    // a token left behind by a different account that later logged in on the same device.
    suspend fun getLastRefreshToken(userId: String): String? {
        val prefs = dataStore.data.first()
        return if (prefs[LAST_REFRESH_USER_ID_KEY] == userId) prefs[LAST_REFRESH_TOKEN_KEY] else null
    }

    /**
     * Stores a rotated refresh token. Must be called the moment a refresh returns and before the
     * new session is imported: Supabase invalidates the spent token after a short reuse window, so
     * a crash in between would leave a dead token stored, and replaying it later is treated as a
     * token-reuse attack that revokes every descendant token — locking the user out for good.
     */
    suspend fun saveLastRefreshToken(userId: String, refreshToken: String) {
        if (refreshToken.isBlank()) return
        dataStore.edit { prefs ->
            prefs[LAST_REFRESH_TOKEN_KEY] = refreshToken
            prefs[LAST_REFRESH_USER_ID_KEY] = userId
        }
    }

    suspend fun clearLastRefreshToken() {
        dataStore.edit { prefs ->
            prefs.remove(LAST_REFRESH_TOKEN_KEY)
            prefs.remove(LAST_REFRESH_USER_ID_KEY)
        }
    }
}
