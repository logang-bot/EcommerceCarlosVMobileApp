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
| `ui/screen/auth/LoginScreen.kt` | Two-state composable UI + `BiometricPrompt` wiring |

---

## State: Regular login

```
Column (horizontal padding 26dp)
  ├── Spacer (weight 1f)
  ├── BrandMark — 64dp rounded square, gradient #6E9BF5→#4878DD, "CV" label
  ├── "Comercializadora Carlos V" — headlineMedium
  ├── "Pedidos & Cuentas" — bodyMedium, text2
  ├── Spacer 40dp
  ├── LoginTextField — "Correo"
  ├── Spacer 12dp
  ├── LoginTextField — "Contraseña"
  ├── [error text — shown when errorMessage != null]
  ├── Spacer 20dp
  ├── PrimaryLoginButton — "Iniciar sesión"
  ├── Spacer (weight 1.3f)
  └── "Acceso exclusivo para titulares de cuenta" — labelSmall, text4
```

---

## State: Enrolled user ("usuario recurrente")

Shown when `isBiometricEnabled == true && showPasswordLogin == false`.

```
Column (horizontal padding 26dp, centered)
  ├── Spacer (weight 1f)
  ├── BrandSectionCompact — 64dp brand mark + company name subtitle
  ├── Spacer 30dp
  ├── WelcomeBackCard — surface2 card:
  │     "Bienvenido de nuevo" label · ProfileAvatar (68dp) · name · email · RoleBadge
  ├── Spacer 18dp
  ├── BiometricLoginButton — "Entrar con huella" (primary, filled)
  ├── Spacer 11dp
  ├── "Usar contraseña" — outlined secondary button → sets showPasswordLogin = true
  ├── Spacer (weight 1.1f)
  └── "Entrar con otra cuenta" — accent text link → resets isBiometricEnabled = false
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

Enrolled user taps "Usar contraseña":
  viewModel.switchToPasswordLogin() → showPasswordLogin = true → regular login shown

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
| `email`, `password` | Regular login form inputs |
| `isLoading`, `errorMessage` | Loading/error UI state |
| `isBiometricEnabled` | Whether to show the enrolled-user screen |
| `enrolledUserName` | Name shown in the welcome-back card |
| `enrolledUserEmail` | Email shown in the welcome-back card |
| `enrolledUserRole` | Role shown as `RoleBadge` in the welcome-back card |
| `enrolledUserInitials` | Initials for `ProfileAvatar` in the welcome-back card |
| `showPasswordLogin` | True when enrolled user taps "Usar contraseña" |

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

## ⚠️ Critical TODOs (app non-functional without these)

### 🔐 Supabase Authentication
The login button currently fakes success with a 300ms delay. **No real authentication happens.**

- [ ] Add `supabase-kt` Auth plugin to dependencies
- [ ] Add `SUPABASE_URL` and `SUPABASE_ANON_KEY` to `local.properties`
- [ ] Replace `delay(300)` stub in `LoginViewModel.onLoginClick` with Supabase `signInWith(Email)`
- [ ] Handle `AuthException` → map to `LoginFormState.errorMessage`
- [ ] On app launch, check `currentSessionOrNull()` and skip login if valid

### 👆 Biometric login
Biometric authentication is fully wired end-to-end for **enabling from Perfil** and for **logging in** from the enrolled-user screen. No remaining TODOs for the biometric login path.
