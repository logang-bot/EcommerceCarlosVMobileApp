package com.restrusher.ecomercecarlosv.domain.session

/**
 * Outcome of [SessionManager.ensureValidSession].
 *
 * Callers must branch on all four: without a real Supabase session, supabase-kt silently falls back
 * to the publishable key and every RLS-protected request is rejected as anonymous instead of failing
 * loudly — so "no session" must never be treated as "probably fine".
 */
enum class SessionResult {
    /** A usable access token is in place; RLS-protected reads and writes will work. */
    VALID,

    /** No connectivity. Callers should fall back to local-only behaviour and retry later. */
    OFFLINE,

    /**
     * Someone else is already restoring this session — supabase-kt's own retry loop is alive and
     * holds the refresh token. Callers must **wait**, never refresh: a second refresh would replay
     * a token the loop still intends to use, and Supabase treats that as a stolen-token replay and
     * revokes the entire token family. Wait on [SessionManager.sessionRecovered] instead.
     */
    DEFERRED,

    /** The stored refresh token was rejected. Only a password login can recover from this. */
    REVOKED,
}
