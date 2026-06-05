package com.restrusher.ecomercecarlosv.data.session

import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// TODO (Phase 9): Replace this in-memory implementation with a DataStore-backed one:
//   1. Inject DataStore<Preferences> (add `datastore-preferences` dependency)
//   2. Define key: val USER_ID_KEY = stringPreferencesKey("current_user_id")
//   3. setCurrentUser → write user.id to DataStore AND keep in-memory for fast reads
//   4. init block → read USER_ID_KEY from DataStore; if present, load AppUser from
//      UserRepository (or fetch from Supabase) and populate _currentUser
//   5. clearSession → remove USER_ID_KEY from DataStore and set _currentUser to null
//   Also consider storing the Supabase JWT refresh token alongside the user id so
//   supabaseClient.auth can restore the session without a re-login.
//   See docs/features/usuarios.md → Session persistence
@Singleton
class SessionManagerImpl @Inject constructor() : SessionManager {

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    override val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    override fun setCurrentUser(user: AppUser) {
        _currentUser.value = user
    }

    override fun clearSession() {
        _currentUser.value = null
    }
}
