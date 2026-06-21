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
import io.github.jan.supabase.auth.minimalSettings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    // Regular client (publishable key) — used for all authenticated-user API calls.
    // RLS policies on each table enforce what this client can read/write.
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
        }

    // Admin client (secret key) — bypasses RLS; used only for SUPERUSUARIO
    // operations such as creating users. Keep this client isolated to admin use cases.
    // TODO: move createUser / updateUser / deleteUser / banUser to Supabase Edge Functions
    //   so the secret key never ships inside the APK. Safe to defer while the app is
    //   distributed only to trusted internal users.
    @Provides
    @Singleton
    @AdminClient
    fun provideAdminSupabaseClient(): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_SECRET_KEY,
        ) {
            install(Auth) {
                minimalSettings()
            }
            install(Postgrest)
        }
}
