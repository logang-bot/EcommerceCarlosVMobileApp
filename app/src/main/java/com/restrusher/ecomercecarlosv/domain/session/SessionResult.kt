package com.restrusher.ecomercecarlosv.domain.session

/**
 * Outcome of [SessionManager.ensureValidSession].
 *
 * Callers must branch on all three: without a real Supabase session, supabase-kt silently falls back
 * to the publishable key and every RLS-protected request is rejected as anonymous instead of failing
 * loudly — so "no session" must never be treated as "probably fine".
 */
enum class SessionResult {
    /** A usable access token is in place; RLS-protected reads and writes will work. */
    VALID,

    /** No connectivity. Callers should fall back to local-only behaviour and retry later. */
    OFFLINE,

    /** The stored refresh token was rejected. Only a password login can recover from this. */
    REVOKED,
}
