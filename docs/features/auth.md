# Feature: Auth — Login

## Status: ✅ Done (Phase 1)

---

## Spec summary

Single screen, no registration flow visible to end users. Two sign-in paths:
1. Email + password
2. Biometric (fingerprint / face)

Only account owners can log in. Roles (Usuario / Superusuario) are enforced post-login.

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Login | `LoginRoute` | `ui/screen/auth/LoginScreen.kt` |

---

## Files

| File | Responsibility |
|------|---------------|
| `ui/screen/auth/LoginUiState.kt` | `LoginFormState` — email, password, isLoading, errorMessage |
| `ui/screen/auth/LoginViewModel.kt` | `@HiltViewModel` — exposes `StateFlow<LoginFormState>`, handles field changes and login action |
| `ui/screen/auth/LoginScreen.kt` | Full composable UI + light/dark previews |

---

## UI layout

```
StatusBar (system)
Column (horizontal padding 26dp, systemBarsPadding)
  ├── Spacer (weight 1f)
  ├── BrandMark — 64dp rounded square, gradient #6E9BF5→#4878DD, "CV" label
  ├── "Comercializadora Carlos V" — headlineMedium, bold
  ├── "Pedidos & Cuentas" — bodyMedium, text2 color
  ├── Spacer 40dp
  ├── LoginTextField — "Correo" (email keyboard)
  ├── Spacer 12dp
  ├── LoginTextField — "Contraseña" (password masking)
  ├── Spacer 20dp
  ├── PrimaryLoginButton — "Iniciar sesión" (52dp, accent, 14dp rounded)
  ├── DividerOr — "o" between hairlines
  ├── BiometricButton — "Entrar con huella" + custom FingerprintIcon
  ├── Spacer (weight 1.3f)
  └── "Acceso exclusivo para titulares de cuenta" — labelSmall, text4 color
GestureNav (system)
```

---

## Data flow

```
User types → ViewModel.onEmailChange / onPasswordChange → LoginFormState update
User taps "Iniciar sesión" → ViewModel.onLoginClick
  → isLoading = true
  → [TODO] Supabase auth call
  → isLoading = false → onSuccess() → navigate(MercadosRoute)
```

---

## Design tokens used

| Element | Token |
|---------|-------|
| Screen background | `colorScheme.background` |
| Field container | `colorScheme.surfaceVariant` |
| Field border (empty) | `extendedColors.border` |
| Field border (filled) | `extendedColors.border2` |
| Field placeholder text | `extendedColors.text3` |
| Primary button | `colorScheme.primary` |
| Biometric button border | `extendedColors.border2` |
| Footer text | `extendedColors.text4` |
| Fingerprint icon tint | `colorScheme.primary` |

---

## ⚠️ Critical TODOs (app non-functional without these)

### 🔐 Supabase Authentication
The login button currently fakes success with an 800ms delay. **No real authentication happens.**

- [ ] Add `supabase-kt` Auth plugin to dependencies (`io.github.jan-tennert.supabase:auth-kt`)
- [ ] Add `SUPABASE_URL` and `SUPABASE_ANON_KEY` to `local.properties` and expose via `BuildConfig`
- [ ] Create `SupabaseModule.kt` (Hilt) providing a `SupabaseClient` singleton with Auth + Postgrest + Storage plugins
- [ ] Replace the `delay(800)` stub in `LoginViewModel.onLoginClick` with `supabaseClient.auth.signInWith(Email) { email = ...; password = ... }`
- [ ] Handle `AuthException` — map to `LoginFormState.errorMessage` and show it below the fields
- [ ] On app launch, check `supabaseClient.auth.currentSessionOrNull()` and skip login if session is still valid (navigate directly to `MercadosRoute`)
- [ ] Session persistence is handled automatically by `supabase-kt` via DataStore — wire `sessionSaving = SessionSaving.SHARED_PREFERENCES` or DataStore in `SupabaseModule`

### 👆 Biometric Authentication
The fingerprint button renders correctly but its `onClick` is a no-op TODO.

- [ ] Add `androidx.biometric:biometric` dependency
- [ ] Create `BiometricAuthManager` (injectable) wrapping `BiometricPrompt` — exposes a `suspend fun authenticate(activity): BiometricResult`
- [ ] In `LoginScreen.kt`, obtain the `Activity` via `LocalContext.current` and call `BiometricAuthManager.authenticate()`
- [ ] On biometric success, retrieve stored credentials from `EncryptedSharedPreferences` (or use Supabase token directly) and complete the session
- [ ] Show the biometric button only if `BiometricManager.canAuthenticate()` returns `BIOMETRIC_SUCCESS`

---

## Other TODOs

- [ ] Handle login error state — show `errorMessage` from `LoginFormState` as a red hint below the password field
- [ ] Add `remember me` / session persistence via DataStore (coordinate with Supabase session saving above)
