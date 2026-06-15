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
        private val json = Json { ignoreUnknownKeys = true }
    }

    override suspend fun saveSession(session: UserSession) {
        dataStore.edit { prefs ->
            prefs[SESSION_KEY] = json.encodeToString(session)
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
}
