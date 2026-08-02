# Feature: Auth — Login

## Status: ✅ Done (Phase 2d)

---

## Spec summary

Single entry point, no registration flow visible to end users. The login screen has **two states** depending on whether the current device has a user with biometric login enrolled:

1. **Regular state** — email + password form
2. **Enrolled-user state** ("usuario recurrente") — shown when `BiometricManager.canAuthenticate` returns `BIOMETRIC_SUCCESS` **and** `userRepository.hasBiometricEnabled()` is true

Only account owners can log in. Roles (Usuario / Superusuario) are enforced post-login.

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Login | `LoginRoute` | `ui/screen/auth/LoginScreen.kt` |

After successful login, navigation goes to `HomeRoute` (popping `LoginRoute` inclusive).

---

## Files

| File | Responsibility |
|------|---------------|
| `ui/screen/auth/LoginUiState.kt` | `LoginFormState` — all login form state including enrolled-user fields |
| `ui/screen/auth/LoginViewModel.kt` | `@HiltViewModel` — biometric availability check, password login, biometric login, account switching |
| `ui/screen/auth/LoginScreen.kt` | Thin router: reads state, wires `BiometricPrompt`, wraps with `LoadingOverlay`, delegates to content composables |
| `ui/screen/auth/LoginContent.kt` | Regular login state UI + `BrandSection` + previews |
| `ui/screen/auth/LoginBiometricoContent.kt` | Enrolled-user login state UI + `WelcomeBackCard` + `BrandSectionCompact` + previews |
| `ui/screen/auth/LoginComponents.kt` | Shared internal composables: `BrandMark`, `LoginTextField`, `PrimaryLoginButton`, `DividerOr` |
| `ui/common/LoadingOverlay.kt` | Reusable scrim overlay with centered spinner; wraps any content |
| `ui/screen/auth/OlvidarUsuarioDialog.kt` | "Olvidar este dispositivo" confirmation + light/dark previews |
| `domain/usecase/BiometricLoginUseCase.kt` | Turns a fingerprint prompt into a real server-backed session; owns the is-active / role rules |
| `domain/usecase/ForgetEnrolledUserUseCase.kt` | Wipes the remembered user: flush, revoke globally, clear enrolment |
| `domain/session/SessionManager.kt` / `SessionManagerImpl` | `ensureValidSession`, `sessionRecovered`, `sessionEnded`, two-tier sign-out |
| `domain/session/SessionResult.kt` | `VALID` / `OFFLINE` / `DEFERRED` / `REVOKED` |
| `domain/session/DeviceDataCleaner.kt` | Wipes cached business data, but only once the write queue has drained |
| `data/session/DataStoreGoTrueSessionManager.kt` | supabase-kt `SessionManager`: startup wipe + the surviving refresh-token mirror |

---

## State: Regular login

```
Column (horizontal padding 26dp)
  ├── Spacer (weight 1f)
  ├── BrandSection — BrandMark (img_logo.png, 80dp) + "Comercializadora Carlos V" headlineMedium + subtitle
  ├── Spacer 40dp
  ├── LoginTextField — "Correo"
  ├── Spacer 12dp
  ├── LoginTextField — "Contraseña" (password, eye toggle)
  ├── [error text — shown when errorMessage != null]
  ├── Spacer 20dp
  ├── PrimaryLoginButton — "Iniciar sesión"
  ├── Spacer (weight 1.3f)
  └── "Acceso exclusivo para titulares de cuenta" — labelSmall, text4
```

---

## State: Enrolled user ("usuario recurrente")

Shown when `isBiometricEnabled == true`. Has two internal sub-states controlled by `showPasswordLogin`.

### Sub-state A — default (`showPasswordLogin == false`)

```
Column (horizontal padding 26dp, centered)
  ├── Spacer (weight 1f)
  ├── BrandSectionCompact — BrandMark (img_logo.png, 64dp) + company name titleMedium
  ├── Spacer 24dp
  ├── WelcomeBackCard — surfaceVariant card:
  │     "Bienvenido de nuevo" labelMedium · ProfileAvatar (68dp) · name titleLarge · email bodySmall · RoleBadge
  ├── Spacer 20dp
  ├── Button — Fingerprint icon + "Entrar con huella" (primary filled) → triggers BiometricPrompt
  ├── Spacer 10dp
  ├── OutlinedButton — "Entrar con contraseña" → switchToPasswordLogin()
  ├── Spacer (weight 1.1f)
  └── ForgetUserRow — "¿No eres X?" (text3) + "Entrar con otra cuenta" (accent, SemiBold)
        → onForgetUserClick() → OlvidarUsuarioDialog
```

### Sub-state B — password mode (`showPasswordLogin == true`)

```
  ├── WelcomeBackCard (same as above)
  ├── Spacer 20dp
  ├── LoginTextField — "Contraseña" (password, eye toggle)
  ├── [error text — shown when errorMessage != null]
  ├── Spacer 16dp
  ├── PrimaryLoginButton — "Iniciar sesión" → onBiometricPasswordLogin
  ├── Spacer 12dp
  ├── BackToFingerprintRow — Fingerprint icon + "Entrar con huella" → triggers BiometricPrompt
  │     └── only when canUseFingerprint; hidden after a revocation, when it could only fail again
  ├── Spacer (weight 1.1f)
  └── ForgetUserRow — same as sub-state A
```

Sub-state B is reached three ways: the user taps "Usar contraseña"; a fingerprint tap returns
`PasswordRequired`; or `checkBiometricAvailability()` finds the stored token already gone on arrival
(the revoked-session case — see "When the session cannot be restored"). Only the third clears
`canUseFingerprint`.

### Overlay — OlvidarUsuarioDialog

Rendered by `LoginScreen` over either sub-state when `showForgetDialog == true`. Amber person badge,
"¿Olvidar a {nombre} en este dispositivo?", body explaining the saved session and fingerprint are
deleted, a shield note clarifying the account itself is untouched, then `Cancelar` / `Olvidar` (error
colour). Confirming runs `ForgetEnrolledUserUseCase` and resets state to a blank `LoginFormState`, i.e.
the regular email/password screen.

### Overlay — CambioDeUsuarioDialog

Rendered over **either** face of the login screen (it is keyed off `state.handover`, outside the
`isBiometricEnabled` branch) when a different user signs in over unsynced writes. Amber cloud-off
badge, "¿Entrar como {nuevo}?", body naming the previous user and how many changes would be lost, an
info note suggesting they sign in and sync first, then `Cancelar` / `Continuar y borrar` (error
colour). See "Device ownership" below for the decision that raises it.

---

## Data flow

```
App start:
  LoginViewModel.init → checkBiometricAvailability
    → if device ready AND getBiometricEnabledUser() != null:
        populate enrolledUser* fields, isBiometricEnabled = true
        → if !canRestoreSession(enrolled.id):        ← token was rejected; huella cannot work
            requirePassword() → showPasswordLogin = true, canUseFingerprint = false,
                                login_error_session_expired
    → LoginScreen shows enrolled-user state (fingerprint or password sub-state accordingly)

Enrolled user taps "Entrar con huella":
  BiometricPrompt.authenticate()
    → onAuthenticationSucceeded → LoginViewModel.onBiometricSuccess(onLoginSuccess)
        → BiometricLoginUseCase (blocks — see "Offline-capable biometric login" below)
            ensureValidSession(verifiedUserId = enrolled.id)
            VALID              → syncFromRemote → isActive check → setCurrentUser → navigate(HomeRoute)
            OFFLINE / DEFERRED → setCurrentUser from Room cache → navigate(HomeRoute)
            REVOKED            → showPasswordLogin = true + login_error_session_expired

Enrolled user taps "Entrar con contraseña":
  viewModel.switchToPasswordLogin() → showPasswordLogin = true → password sub-state shown

Enrolled user types password + taps "Iniciar sesión" (password sub-state):
  viewModel.onBiometricPasswordLogin(onLoginSuccess)
    → deviceOwnerUserId()                       ← read BEFORE signing in
    → signInWith(Email) using the enrolled user's stored email + the typed password
    → finishPasswordLogin(enrolled.id, previousOwner, onSuccess)   ← shared funnel, see below

Enrolled user taps "Entrar con huella" (password sub-state):
  triggerBiometric → BiometricPrompt.authenticate() → same as default biometric path

Enrolled user taps "¿No eres X? Entrar con otra cuenta":
  viewModel.onForgetUserClick() → showForgetDialog = true
    → confirm → ForgetEnrolledUserUseCase
                  1. flush queue + wipe cached data (only if the queue drained)  ← needs a session
                  2. forgetDevice() — global revoke, erase token
                  3. clear biometricEnabledAt
                → blank LoginFormState

Regular / account-disabled screen taps "Entrar con otra cuenta":
  viewModel.switchToOtherAccount() → isBiometricEnabled = false → regular login shown

Regular login:
  onLoginClick
    → deviceOwnerUserId()                       ← read BEFORE signing in
    → signInWith(Email) with the typed email + password
    → finishPasswordLogin(userId, previousOwner, onSuccess)

finishPasswordLogin (the single funnel for BOTH password paths):
  syncFromRemote → fall back to Room → isActive check (signOut + disabled card if false)
    → ResolveDeviceHandoverUseCase(user, previousOwner)
        Proceed              → enterApp: clearBiometricEnabledExcept, claimDevice,
                               setCurrentUser, resetStaleness, navigate(HomeRoute)
        ConfirmationRequired → state.handover set → CambioDeUsuarioDialog
                                 confirm → wipeCachedDataForNewUser → re-read profile → enterApp
                                 cancel  → clearSession → blank LoginFormState
```

---

## LoginFormState fields

| Field | Purpose |
|-------|---------|
| `email`, `password` | Form inputs; `password` is also used by the biometric screen |
| `isLoading`, `errorMessage` | Loading/error UI state |
| `isBiometricEnabled` | Whether to show the enrolled-user screen |
| `showPasswordLogin` | True when enrolled user has tapped "Entrar con contraseña", or the session expired |
| `canUseFingerprint` | False once the stored refresh token is gone — hides the "Entrar con huella" row, which could only fail |
| `showForgetDialog` | True while the `OlvidarUsuarioDialog` confirmation is open |
| `handover` | Non-null while `CambioDeUsuarioDialog` is open; carries the incoming/previous names and the pending-write count |
| `enrolledUserName` | Name shown in the welcome-back card, and in the forget dialog title |
| `enrolledUserFirstName` | First name only — used by the "¿No eres X?" row |
| `enrolledUserEmail` | Email shown in the welcome-back card |
| `enrolledUserRole` | Role shown as `RoleBadge` in the welcome-back card |
| `enrolledUserInitials` | Initials for `ProfileAvatar` in the welcome-back card |

---

## BiometricPrompt configuration

```kotlin
BiometricPrompt.PromptInfo.Builder()
    .setTitle(R.string.login_biometric_prompt_title)
    .setSubtitle(R.string.login_biometric_prompt_subtitle)
    .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
    .setNegativeButtonText(R.string.common_cancelar)
    .build()
```

The prompt is constructed in `LoginScreen` (requires `FragmentActivity` context obtained via `Context.findFragmentActivity()`).

---

## Design tokens used

| Element | Token |
|---------|-------|
| Screen background | `colorScheme.background` |
| Field container | `colorScheme.surfaceVariant` |
| Field border (empty) | `extendedColors.border` |
| Field border (filled) | `extendedColors.border2` |
| Primary button | `colorScheme.primary` |
| Welcome-back card | `colorScheme.surfaceVariant` |
| "Usar contraseña" border | `extendedColors.border2` |
| Footer / muted text | `extendedColors.text3`, `extendedColors.text4` |
| "Otra cuenta" link | `colorScheme.primary` |

---

## ✅ Phase 9 — Supabase Authentication (implemented)

### Supabase wiring
- [x] `supabase-kt` 3.1.4 (auth-kt + postgrest-kt + ktor-okhttp) added to `libs.versions.toml` and `build.gradle.kts`
- [x] Staging/production credentials in `local.properties` (`STAGING_SUPABASE_URL`, `STAGING_SUPABASE_PUBLISHABLE_KEY`, and production equivalents); file is gitignored. The secret/service-role key is **no longer used by the app** — privileged Auth-Admin operations run in Edge Functions (`data/remote/AdminUserService`, `supabase/functions/`); see `docs/supabase-setup.md` §9
- [x] `LoginViewModel.onLoginClick` calls `supabase.auth.signInWith(Email)` — stub removed
- [x] `RestException` caught and mapped: "banned" → `isAccountDisabled = true`; invalid credentials → Spanish `errorMessage`; other → generic auth error
- [x] After auth succeeds, `syncFromRemote(userId)` is always called to get the freshest `is_active` value; if `isActive == false` → `signOut()` + `isAccountDisabled = true`
- [x] `LoadingOverlay` (scrim + centered spinner) shown at `LoginScreen` level during `state.isLoading`, blocking both login paths
- [x] `SessionManagerImpl` subscribes to `auth.sessionStatus`
- [x] JWT persisted via `DataStoreGoTrueSessionManager`; userId in DataStore
- [x] `MainActivity` keeps splash screen visible until `SessionManager.isLoaded = true`
- [x] `AppNavigation` auto-navigates to `HomeRoute` if a session is active at startup — **now disabled; session is always wiped on startup (see below)**
- [x] On `NotAuthenticated` (explicit logout): all Room tables wiped via `database.clearAllTables()` unless a biometric-enrolled user exists; splash is held until wipe completes

### supabase-kt 3.1.4 API changes

| Old symbol | New symbol |
|------------|------------|
| `io.github.jan.supabase.auth.SessionStatus` | `io.github.jan.supabase.auth.status.SessionStatus` |
| `SessionStatus.LoadingFromStorage` | `SessionStatus.Initializing` |
| `SessionStatus.NetworkError` | `SessionStatus.RefreshFailure` |
| `import io.github.jan.supabase.auth.signOut` (bogus top-level) | removed; call `supabase.auth.signOut()` directly |
| `admin.createUser { }` | `admin.createUserWithEmail { }` |
| `emailConfirm = true` | `autoConfirm = true` |
| `userMetadata = buildJsonObject { }` | `userMetadata { }` (lambda DSL) |
| `banned = true` | `banDuration = "876000h"` (no permanent-ban field exists) |

### Login error / disabled states

Two extra visual states in `LoginContent.kt` (toggled by `LoginUiState`):

**Credentials error** (`errorMessage != null`)
```
  ├── red-tint banner (13dp radius) — Warning icon + annotated text (bold + body)
  ├── LoginTextField "Correo"    [isError = true]
  └── LoginTextField "Contraseña" [isError = true, errorText = login_error_password_hint]
```

**Account disabled** (`isAccountDisabled = true`) — replaces the form entirely
```
  └── red-tint card (18dp radius)
        ├── 52dp icon box (16dp radius, 0.16α red bg, Block icon)
        ├── "Tu cuenta está desactivada" — 17sp bold
        ├── body text
        ├── Button [primary] — Phone icon + "Contactar al administrador"
        └── OutlinedButton — "Entrar con otra cuenta" → switchToOtherAccount()
```

`isAccountDisabled` is set when `RestException.message` contains "banned".
`switchToOtherAccount()` resets both `errorMessage` and `isAccountDisabled = false`.

---

## Session behaviour on app restart

**The user is always required to log in on every app start.** No session is restored from storage after a process kill.

### How it works

`DataStoreGoTrueSessionManager` implements supabase-kt's `SessionManager` interface. It stores the JWT in DataStore so the auth plugin can refresh tokens during the active session. On the first call to `loadSession()` (always the startup call), a `firstLoad` flag is checked:

```kotlin
@Volatile private var firstLoad = true

override suspend fun loadSession(): UserSession? {
    if (firstLoad) {
        firstLoad = false
        deleteSession()   // wipe any persisted JWT before supabase-kt sees it
        return null
    }
    // subsequent calls (token refresh mid-session) load normally
    ...
}
```

Returning `null` on the first load means supabase-kt immediately emits `SessionStatus.NotAuthenticated` — no `Authenticated` state is ever reached at startup, so `AppNavigation`'s auto-navigate `LaunchedEffect` never fires. The user sees Login.

After the user logs in, `saveSession()` stores the new JWT. Any subsequent `loadSession()` call (`firstLoad` is now `false`) returns the current JWT so token refresh works correctly during the active session.

### Room data is preserved across restarts

`SessionManagerImpl.onNotAuthenticated()` **never** wipes local data. `NotAuthenticated` fires for three
different reasons — the startup session clear, an explicit sign-out, and a mid-session refresh failure
when supabase-kt gives up on a revoked token — and only one of them is the user's intent. Deciding from
inside the collector meant a revoked token silently destroyed cached business data, unsynced
`sync_operations` included.

The wipe therefore lives in `signOut()`, where the intent is unambiguous:

```kotlin
override suspend fun signOut() {
    if (userRepository.hasBiometricEnabled()) discardAccessToken() else revokeAndWipe()
    _currentUser.value = null
    removeUserId()
}
```

The local Room cache (mercados, clientes, productos, pedidos, sync queue) survives restarts and
refresh failures. Only an explicit sign-out on a device with no fingerprint enrolled wipes the tables.

### Token refresh during the session

supabase-kt calls `loadSession()` internally when the JWT approaches expiry to get the stored refresh token. Because `firstLoad` is `false` after startup, these calls read the DataStore normally and refresh proceeds transparently.

---

## Offline-capable biometric login (Phase 12)

Only the biometric ("Entrar con huella") path can complete a login while the device is offline — a normal email/password login (regular state, or the enrolled-user's "Entrar con contraseña" sub-state) always requires calling `supabase.auth.signInWith(Email)`, so it needs network by definition. Biometrics are the only mechanism that can verify identity locally, so they're the only path allowed to skip the network entirely.

### Why biometric login previously looked "online-only" in practice

`LoginViewModel.onBiometricSuccess` never called Supabase — it only read the enrolled user from Room and called `sessionManager.setCurrentUser(user)`. That part already worked offline. But because `DataStoreGoTrueSessionManager.loadSession()` wipes the persisted JWT on every app start (`firstLoad`, see above), a fingerprint-only login never obtained a real Supabase Auth session either. Every table's RLS policy requires the `authenticated` role (`docs/sql/rls.sql`), so **any** sync read or queued write made after a fingerprint login was silently rejected — online or offline. In practice only the password-based paths (which do call `signInWith`) actually kept syncing, which is why biometric login looked like it "needed" credentials/network to really work.

### Fix: a token mirror that survives the startup wipe

`DataStoreGoTrueSessionManager` now also mirrors the refresh token of the *last successful login* into a second DataStore key, tagged with the owning user id (`biometric_refresh_token` / `biometric_refresh_user_id`). Unlike the main session key, this mirror is **not** touched by the `firstLoad` wipe, so it survives app restarts.

```
Any successful login (password or restored biometric session)
  → supabase-kt calls sessionManager.saveSession(session)
  → DataStoreGoTrueSessionManager writes SESSION_KEY (as before)
  → …and mirrors refreshToken + user.id into the biometric-only keys
```

### The refresh is blocking, not fire-and-forget

The original Phase 12 design navigated to Home first and restored the session afterwards in the
background, swallowing failures. That is exactly what allowed a fingerprint login to land in the app
with **no** session: supabase-kt does not throw when a session is missing — `AccessToken.kt` falls back
to the publishable key — so every request went out anonymous and was silently declined by RLS. The user
saw stale data, missing images and failed writes, with nothing indicating they were unauthenticated.

`SessionManager.ensureValidSession()` replaces `restoreBiometricSession()` and **gates navigation**:

```
LoginScreen: fingerprint tap succeeds
  → BiometricLoginUseCase
      1. sessionManager.ensureValidSession()          // blocks — one HTTP round trip when online
      2. VALID              → syncFromRemote(user.id), reject if !isActive, then navigate
         OFFLINE / DEFERRED → local-only login from the Room cache, staleness thresholds preserved
         REVOKED            → stay on Login, switch to the password field
```

### ensureValidSession() — who owns the refresh

The same helper gates the queue and the synchronizer (see `infrastructure.md`), so it must never
refresh a token supabase-kt is already refreshing. Its retry loop captures the session **by value** and
replays the same refresh token every 10s; spending that token ourselves means the replay lands right at
Supabase's 10s reuse interval, and outside it the **entire token family is revoked** — permanent
lockout. So the decision is made from `supabase.auth.sessionStatus`:

| status | meaning | result |
|---|---|---|
| `Authenticated` | usable token | `VALID` |
| `RefreshFailure` | a retry loop is alive and holds the token | `DEFERRED` — wait, do not touch it |
| `Initializing` | startup in flight | `DEFERRED` |
| `NotAuthenticated` | `clearSession()` already tore the loop down (`sessionJob = null`) | refresh from the stored token — no competitor |

The manual refresh path is therefore only ever reached when supabase-kt has given up, which includes
the login screen (the startup wipe leaves `NotAuthenticated`). A `Mutex` coalesces concurrent callers
onto a single attempt.

**Who the session is restored *for* is a security boundary.** At the login screen the app has
deliberately forgotten the signed-in user — `onNotAuthenticated()` clears `current_user_id` during the
startup wipe — so nothing in the session layer can resolve an id there. `ensureValidSession` therefore
takes an optional `verifiedUserId`, which both names the account and asserts the caller has just
verified that person locally:

- `BiometricLoginUseCase` passes `enrolled.id`, having had a successful fingerprint prompt.
- Everyone else (queue flush, synchronizer, reconnect collector) omits it and is resolved from the
  active session.

Resolving the enrolled user implicitly instead would let a background queue flush authenticate at
startup before the user proved anything, defeating always-require-login-on-start.

### Two ways a session comes back

`SessionManager.sessionRecovered: SharedFlow<Unit>` emits whenever a usable token returns; the queue
flushes on it and the synchronizer marks its data stale.

| user was… | `sessionStatus` on reconnect | restored by | latency |
|---|---|---|---|
| online, token expired mid-session | `RefreshFailure` (loop alive) | supabase-kt → `sessionRecovered` | ≤10s |
| offline fingerprint login | `NotAuthenticated` (no loop) | `SessionManagerImpl`'s `isOnlineFlow` collector → manual refresh | immediate |

The second row is why that reconnect collector exists: after an offline fingerprint login there is no
session and no retry loop, so `sessionRecovered` would never fire — a renewal cannot be announced for a
session that never existed. A successful manual refresh also re-reads the profile, since the cached row
can predate a role change made while the device was offline (`onAuthenticated` only fetches remotely
when the local row is missing entirely).

### When the session cannot be restored

`REVOKED` means the stored token was rejected outright — a ban, a password changed elsewhere, or reuse
detection. `SessionManagerImpl` clears the local session and emits `SessionManager.sessionEnded`;
`AppNavigation` collects it and returns the user to Login (guarded so the queue's repeated retries
don't re-navigate once already there), while `AppError.Session` shows "Tu sesión expiró. Vuelve a
iniciar sesión". The navigation is not optional: the message asks for a password login, and without it
the app offers no way to perform one.

`AppError.Session` is emitted by **`endSession()` itself**, not by its callers. The queue and the
synchronizer used to emit it, which left the paths with no caller silent — notably the reconnect
collector in `init`, whose `ensureValidSession()` speaks for nobody. That case teleported the user to
Login with no explanation at all. Emitting at the source covers every detection exactly once:
`endSession` clears `current_user_id`, so a repeat call resolves no user and returns `DEFERRED`
instead of `REVOKED`.

**Landing on Login is not the same as being able to log in.** `LoginScreen` routes on
`biometricEnabledAt` in Room, which a revocation never touches — so an enrolled user arrived at the
fingerprint card, and the fingerprint is exactly what cannot work: the stored token has just been
cleared. `LoginViewModel.checkBiometricAvailability()` therefore asks
`SessionManager.canRestoreSession(userId)` on arrival and calls `requirePassword()` when it is false,
opening the password sub-state immediately and hiding the fingerprint row (`canUseFingerprint`).

That check is exact rather than heuristic: `clearLastRefreshToken()` runs on rejection, and its only
other call sites (`revokeAndWipe`, `forgetDevice`) also drop the enrolment. So **enrolled *and* no
stored token** can only mean the token was rejected. It is answered from DataStore, so it is also
correct offline.

The enrolment is deliberately **kept**. Erasing it would force the full email form and silently leave
the fingerprint off after the user recovers — they would have to re-enable it from Perfil without
being told. A stale enrolment cannot grant access (the password login simply fails for a banned or
deleted account) and "¿No eres X?" already clears it. After a successful password login `saveSession`
rewrites the token mirror, so the fingerprint works again on its own.

Not knowing *who* the user is — a fresh install, or before the first login — is `DEFERRED`, not
`REVOKED`. Otherwise startup finding orphaned queue rows would tell someone their session expired when
they never had one.

A **5xx** from the refresh endpoint is `OFFLINE`, not `REVOKED`: the token is kept and the next attempt
retries, matching what supabase-kt does in `tryImportingSession`. Signing a user out over a Supabase
outage would strand them behind a password prompt for something that was never their problem.

### What actually revokes a token

Refresh tokens do not expire on their own **by default** — but *Time-box user sessions* and *Inactivity
timeout* under Auth → Sessions change that if enabled, and both should stay off. Otherwise `REVOKED`
means a deliberate revocation:

| cause | notes |
|---|---|
| Superuser deactivates or deletes the account | `set-user-active` bans with `ban_duration = 876000h`; a GoTrue ban invalidates the user's refresh tokens |
| "Olvidar este dispositivo" on **another** device | `forgetDevice()` uses `SignOutScope.GLOBAL`, which revokes every session for that user — the likeliest real-world trigger |
| Password changed from another device | |
| Reuse detection fired | a spent token replayed outside the ~10s window revokes the whole family — what the `DEFERRED` rule exists to prevent |
| Staging ↔ production flavour swap | the token belongs to a different project; development only |

In normal operation this is "the account genuinely lost access", which is exactly when a password login
is the correct answer.

**An offline fingerprint login deliberately holds no token at all.** Reusing the last access token was
considered and rejected: it is deleted at every app start, importing an expired session starts a 10s
polling loop for the whole offline period, and Supabase rejects an expired token exactly as it rejects
an anonymous one.

**Token rotation ordering matters.** Supabase rotates the refresh token on every use and invalidates the
spent one after a short reuse window. Persisting the rotated token only after `importSession()` left a
crash window in which the stored token was already dead — and replaying a dead token is treated as a
reuse attack that revokes the entire token family, locking the user out permanently. The rotated token
is therefore written the instant `refreshSession()` returns.

### Sign-out is two-tier

| | fingerprint enrolled | not enrolled |
|---|---|---|
| access token | discarded locally (`auth.clearSession()`) | discarded |
| refresh token | **kept** — no `/logout` call | revoked server-side |
| cached data | kept | wiped, **unless writes are still queued** |
| next fingerprint tap | mints a fresh session, no password | n/a |

The not-enrolled wipe is guarded on `pendingCount()`, mirroring `DeviceDataCleanerImpl`. It used to
run unconditionally, so a user without the fingerprint who signed out while offline destroyed every
queued pedido — data that exists nowhere else. Keeping it does strand the queue behind a sign-out
with no way back in, but the next login resolves that: the same user flushes it, a different one is
asked first (see below).

A fingerprint is not a credential — it unlocks the device, it proves nothing to Supabase. The stored
refresh token is the only thing the app can present, so revoking it on sign-out is what previously left
the fingerprint button working but useless. Keeping it means an enrolled user is never asked for a
password again; "Olvidar este dispositivo" on the login screen is the hard sign-out that revokes
everything (`ForgetEnrolledUserUseCase`).

`PerfilViewModel.disableBiometric()` no longer clears the token — the user is still signed in and that
token is what keeps their session alive. Signing out afterwards takes the not-enrolled branch and
revokes it properly.

A deactivated account loses its enrolment: `BiometricLoginUseCase` calls `forgetDevice()` and clears
`biometricEnabledAt`, so the fingerprint stops offering a way in until a superuser reactivates the
account and the user signs in with their password.

### Device ownership — who the cached data belongs to

Queued operations carry **no author**, `getPending()` has **no filter**, and RLS only checks the
signed-in user's *role* (`docs/sql/rls.sql`: `get_my_role() IN ('SUPERUSUARIO','USUARIO')`) — `pedidos`
has no owner column at all. So flushing one user's queue under another's session does not fail; it
**succeeds**, filing their pedidos under the wrong account. The cached tables leak the same way, just
visibly.

Nothing durable recorded who the device belonged to: `current_user_id` is cleared by the startup wipe
*and* by `endSession()`, which is exactly when the answer is needed. `DEVICE_OWNER_KEY`
(`device_owner_user_id` in DataStore) fills that gap.

It is **not** written when a session merely becomes authenticated. `onAuthenticated` fires the instant
`signInWith` returns, which would overwrite the owner before the handover could be detected. Instead
`claimDevice()` is called only once a login has been allowed to keep the cache, and the login flow
reads `deviceOwnerUserId()` **before** authenticating.

`ResolveDeviceHandoverUseCase` then decides, from the single funnel `finishPasswordLogin()`:

| incoming vs. owner | queued writes | outcome |
|---|---|---|
| same, or no owner | any | `Proceed` — queue flushes as normal |
| different | 0 | cache wiped silently, `Proceed` |
| different | > 0 | `ConfirmationRequired` → `CambioDeUsuarioDialog` |

The dialog names the previous user and the count, and suggests they sign in and sync instead.
Cancelling calls `auth.clearSession()` to back the sign-in out, leaving their work untouched.
Confirming calls `DeviceDataCleaner.wipeCachedDataForNewUser()` — which, unlike
`wipeCachedDataIfFullySynced()`, deliberately does **not** flush first: those writes belong to the
previous user.

`QueueProcessor.flush()` independently refuses when `deviceOwnerUserId()` and the signed-in user
disagree. That covers the window between `signInWith` succeeding and the dialog being answered, in
which `SyncWorker`'s retry backoff could otherwise fire. A fingerprint login skips the check entirely —
it can only ever resolve to the enrolled user, who by definition already owns the cache.

---

### Indefinite ban / activate
Supabase Auth has no permanent-ban boolean. Handled by the `set-user-active` Edge Function
(triggered from `UsuarioDetalleViewModel` via `AdminUserService.setActive`):
- **Deactivate**: `ban_duration = "876000h"` (~100 years) + set `is_active = false` in users table
- **Activate**: `ban_duration = "none"` + set `is_active = true` in users table

All privileged user-management operations (create / role change / activate-deactivate /
password reset of another user / delete) now run server-side in Edge Functions using the
service role, instead of an admin client bundled in the APK. See `docs/supabase-setup.md` §9.

### 👆 Biometric login
See "Offline-capable biometric login (Phase 12)" above. When online, the fingerprint trades the stored
refresh token for a brand-new Supabase session **before** navigating, and re-reads the profile so role
and is-active changes apply exactly as they would on a password login. Offline it still completes from
the Room cache.

### 🗑️ Olvidar este dispositivo
The recurring-user login screen offers "¿No eres X? Entrar con otra cuenta", which opens a confirmation
dialog. Confirming runs `ForgetEnrolledUserUseCase`, **in this order**:

1. `DeviceDataCleaner.wipeCachedDataIfFullySynced()` — pushes anything still queued, then wipes the
   cached tables only if the queue drained. Returns false and keeps the data when writes are pending,
   since unsynced pedidos exist nowhere else.
2. `sessionManager.forgetDevice()` — global sign-out, so a token copied off the device is dead.
3. `biometricEnabledAt` cleared.

**The order is load-bearing.** Step 1 needs a live session to push, so revoking first would strand
those writes on a device that is about to forget them.

`forgetDevice()` uses `SignOutScope.GLOBAL`, which revokes **every** session for that account — so
forgetting device A also ends the session on device B, which will bounce to Login on its next sync.
That is the most likely real-world cause of a `REVOKED` result.
