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

        // Mirrors the refresh token of the last successful login, tagged with its owning user
        // id. Unlike SESSION_KEY, this survives the startup wipe below — it is the only way a
        // biometric-only login (no password) can silently re-establish a real Supabase session
        // (needed for RLS) once the device is back online. See SessionManagerImpl.
        private val BIOMETRIC_REFRESH_TOKEN_KEY = stringPreferencesKey("biometric_refresh_token")
        private val BIOMETRIC_REFRESH_USER_ID_KEY = stringPreferencesKey("biometric_refresh_user_id")

        private val json = Json { ignoreUnknownKeys = true }
    }

    override suspend fun saveSession(session: UserSession) {
        dataStore.edit { prefs ->
            prefs[SESSION_KEY] = json.encodeToString(session)
            val userId = session.user?.id
            if (session.refreshToken.isNotBlank() && userId != null) {
                prefs[BIOMETRIC_REFRESH_TOKEN_KEY] = session.refreshToken
                prefs[BIOMETRIC_REFRESH_USER_ID_KEY] = userId
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
    suspend fun getBiometricRefreshToken(userId: String): String? {
        val prefs = dataStore.data.first()
        return if (prefs[BIOMETRIC_REFRESH_USER_ID_KEY] == userId) prefs[BIOMETRIC_REFRESH_TOKEN_KEY] else null
    }

    suspend fun clearBiometricRefreshToken() {
        dataStore.edit { prefs ->
            prefs.remove(BIOMETRIC_REFRESH_TOKEN_KEY)
            prefs.remove(BIOMETRIC_REFRESH_USER_ID_KEY)
        }
    }
}
