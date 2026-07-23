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
  └── "Entrar con otra cuenta" — accent text link → switchToOtherAccount()
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
  └── "Entrar con otra cuenta" — accent text link → switchToOtherAccount()
```

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
        → getBiometricEnabledUser() → sessionManager.setCurrentUser → navigate(HomeRoute)

Enrolled user taps "Entrar con contraseña":
  viewModel.switchToPasswordLogin() → showPasswordLogin = true → password sub-state shown

Enrolled user types password + taps "Iniciar sesión" (password sub-state):
  viewModel.onBiometricPasswordLogin(onLoginSuccess)
    → validates password against the enrolled user (stub: password == "admin")
    → getBiometricEnabledUser() → sessionManager.setCurrentUser → navigate(HomeRoute)

Enrolled user taps "Entrar con huella" (password sub-state):
  triggerBiometric → BiometricPrompt.authenticate() → same as default biometric path

User taps "Entrar con otra cuenta":
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
| `enrolledUserName` | Name shown in the welcome-back card |
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

`SessionManagerImpl` distinguishes the startup clear from an explicit logout using a `startupDone` flag:

```kotlin
is SessionStatus.NotAuthenticated -> {
    _currentUser.value = null
    removeUserId()
    if (startupDone) wipeLocalDataIfNeeded()   // skip on startup, run on explicit logout
    startupDone = true
    _isLoaded.value = true
}
```

The local Room cache (mercados, clientes, productos, pedidos, sync queue) survives restarts. Only explicit logout triggers the table wipe.

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

`SessionManager.restoreBiometricSession()` (fire-and-forget, runs on `@ApplicationScope` so it survives `LoginViewModel` being cleared right after navigation) uses this mirror:

```
LoginScreen: fingerprint tap succeeds
  → LoginViewModel.onBiometricSuccess
      1. sessionManager.setCurrentUser(cachedUser)   // Room read only — always works, even offline
      2. onSuccess()                                  // navigate to Home immediately
      3. sessionManager.restoreBiometricSession()      // best-effort, backgrounded, never blocks the UI
           if offline                                  → no-op
           if the mirrored token's user id doesn't
             match the enrolled user                   → no-op (guards against a different
                                                           account's later password login on
                                                           the same device overwriting the mirror)
           else → supabase.auth.refreshSession(token)  → supabase.auth.importSession(newSession)
                    → sessionStatus emits Authenticated → RLS-protected sync/writes now work
```

If the refresh fails or times out (offline, revoked token), the failure is swallowed — the user is already in and working from the local Room cache; queued writes simply stay queued until a real session is restored on a later launch or an explicit password login.

### Logout / disabling biometrics clears the mirror

`SessionManagerImpl.signOut()` and `PerfilViewModel.disableBiometric()` both call `SessionManager.clearBiometricSession()`, deleting the mirrored token. After an explicit logout, the next fingerprint tap still logs the user in locally (offline-first, unaffected), but silent Supabase re-authentication won't happen again until a real password login re-populates the mirror.

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
See "Offline-capable biometric login (Phase 12)" above. The biometric prompt verifies the user
locally and always succeeds offline; a real Supabase session is silently restored in the
background, best-effort, when the device is online.
