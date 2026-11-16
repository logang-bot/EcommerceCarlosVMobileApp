package com.restrusher.ecomercecarlosv.screenshots.di

import com.restrusher.ecomercecarlosv.di.SupabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

/**
 * A client that can never reach a real project: the URL is on the reserved `.invalid` domain and
 * the session lives only in memory. The screenshot suite then depends on neither the Supabase
 * secrets in `local.properties` nor the backend being up, and never touches the DataStore session
 * the installed app keeps.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [SupabaseModule::class])
object TestSupabaseModule {

    private const val OFFLINE_URL = "https://screenshots.invalid"
    private const val PLACEHOLDER_KEY = "screenshots-placeholder-key"

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient =
        createSupabaseClient(supabaseUrl = OFFLINE_URL, supabaseKey = PLACEHOLDER_KEY) {
            install(Auth) {
                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()
                autoLoadFromStorage = false
                autoSaveToStorage = false
            }
            install(Postgrest)
            install(Storage)
            install(Functions)
        }
}
