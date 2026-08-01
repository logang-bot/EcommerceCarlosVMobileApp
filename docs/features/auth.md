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
  ├── Row (clickable) — Fingerprint icon + "Entrar con huella" → triggers BiometricPrompt
  ├── Spacer (weight 1.1f)
  └── ForgetUserRow — same as sub-state A
```

### Overlay — OlvidarUsuarioDialog

Rendered by `LoginScreen` over either sub-state when `showForgetDialog == true`. Amber person badge,
"¿Olvidar a {nombre} en este dispositivo?", body explaining the saved session and fingerprint are
deleted, a shield note clarifying the account itself is untouched, then `Cancelar` / `Olvidar` (error
colour). Confirming runs `ForgetEnrolledUserUseCase` and resets state to a blank `LoginFormState`, i.e.
the regular email/password screen.

---

## Data flow

```
App start:
  LoginViewModel.init → checkBiometricAvailability
    → if device ready AND hasBiometricEnabled():
        getBiometricEnabledUser() → populate enrolledUser* fields, isBiometricEnabled = true
    → LoginScreen shows enrolled-user state

Enrolled user taps "Entrar con huella":
  BiometricPrompt.authenticate()
    → onAuthenticationSucceeded → LoginViewModel.onBiometricSuccess(onLoginSuccess)
        → BiometricLoginUseCase (blocks — see "Offline-capable biometric login" below)
            VALID    → syncFromRemote → isActive check → setCurrentUser → navigate(HomeRoute)
            OFFLINE  → setCurrentUser from Room cache → navigate(HomeRoute)
            REVOKED  → showPasswordLogin = true + login_error_session_expired

Enrolled user taps "Entrar con contraseña":
  viewModel.switchToPasswordLogin() → showPasswordLogin = true → password sub-state shown

Enrolled user types password + taps "Iniciar sesión" (password sub-state):
  viewModel.onBiometricPasswordLogin(onLoginSuccess)
    → validates password against the enrolled user (stub: password == "admin")
    → getBiometricEnabledUser() → sessionManager.setCurrentUser → navigate(HomeRoute)

Enrolled user taps "Entrar con huella" (password sub-state):
  triggerBiometric → BiometricPrompt.authenticate() → same as default biometric path

Enrolled user taps "¿No eres X? Entrar con otra cuenta":
  viewModel.onForgetUserClick() → showForgetDialog = true
    → confirm → ForgetEnrolledUserUseCase (revoke globally, erase token, clear enrolment,
                 wipe cached data if the write queue drained) → blank LoginFormState

Regular / account-disabled screen taps "Entrar con otra cuenta":
  viewModel.switchToOtherAccount() → isBiometricEnabled = false → regular login shown

Regular login:
  onLoginClick → delay(300) → check "admin"/"admin" stub
    → load user from Room → sessionManager.setCurrentUser → navigate(HomeRoute)
```

---

## LoginFormState fields

| Field | Purpose |
|-------|---------|
| `email`, `password` | Form inputs; `password` is also used by the biometric screen |
| `isLoading`, `errorMessage` | Loading/error UI state |
| `isBiometricEnabled` | Whether to show the enrolled-user screen |
| `showPasswordLogin` | True when enrolled user has tapped "Entrar con contraseña" |
| `showForgetDialog` | True while the `OlvidarUsuarioDialog` confirmation is open |
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
           already has a session                       → VALID
           offline                                     → OFFLINE
           no stored token for this user               → REVOKED
           else → supabase.auth.refreshSession(token)
                    → saveLastRefreshToken(rotated)     // persisted BEFORE importSession
                    → supabase.auth.importSession(...)  → VALID
                  RestException (revoked/banned/reused) → REVOKED
      2. VALID    → syncFromRemote(user.id), reject if !isActive, then navigate
         OFFLINE  → local-only login from the Room cache, staleness thresholds preserved
         REVOKED  → stay on Login, switch to the password field
```

`ensureValidSession()` is guarded by a `Mutex`: parallel refreshes presenting the same rotating token
would themselves look like a token-reuse attack.

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
| cached data | kept | wiped |
| next fingerprint tap | mints a fresh session, no password | n/a |

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
dialog. Confirming runs `ForgetEnrolledUserUseCase`: a global sign-out (so a token copied off the device
is dead), the stored token erased, `biometricEnabledAt` cleared, and the cached business data wiped —
the last step only once the write queue has drained, since unsynced pedidos exist nowhere else.
`DeviceDataCleaner.wipeCachedDataIfFullySynced()` returns false and keeps the data when writes are still
pending.
