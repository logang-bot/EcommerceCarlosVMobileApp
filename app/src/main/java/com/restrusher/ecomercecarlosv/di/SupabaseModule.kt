package com.restrusher.ecomercecarlosv.di

import com.restrusher.ecomercecarlosv.BuildConfig
import com.restrusher.ecomercecarlosv.data.session.DataStoreGoTrueSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    // Single client (publishable key) — used for all API calls.
    // RLS policies on each table enforce what this client can read/write.
    //
    // Privileged operations that need the service role (creating/updating/deleting
    // auth users) are NOT done here — they run in Supabase Edge Functions, invoked
    // via the Functions plugin (which forwards the current session JWT). This keeps
    // the service-role secret off the device entirely. See `data/remote/AdminUserService`.
    @Provides
    @Singleton
    fun provideSupabaseClient(sessionManager: DataStoreGoTrueSessionManager): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            install(Auth) {
                this.sessionManager = sessionManager
                this.codeVerifierCache = MemoryCodeVerifierCache()
                autoLoadFromStorage = true
                autoSaveToStorage = true
            }
            install(Postgrest)
            install(Storage)
            install(Functions)
        }
}
