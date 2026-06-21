# Mi Perfil

Screen accessible from the Home bottom bar. Shows identity info, security settings, team management (superusers), and appearance settings (all users).

## Sections

### Cuenta
- **Correo** (`Icons.Default.Email`) — taps to `EditarPerfilRoute`
- **Teléfono** (`Icons.Default.Phone`) — taps to `EditarPerfilRoute`

### Seguridad
- **BiometricCard** — toggle to enroll/disable biometric auth. Uses `BiometricPrompt` from `androidx.biometric`. State persisted via `UserRepository.setBiometricEnabled(userId, timestamp)`.
- **Cambiar contraseña** (`Icons.Default.Lock`) — visible only to `SUPERUSUARIO`. Navigates to `CambiarContrasenaRoute(userId = state.userId, isSelf = true)`. See [Cambiar contraseña screen](#cambiar-contraseña-screen) below.

### Equipo *(superuser only)*
- **Gestión de usuarios** — navigates to `GestionUsuariosRoute`. Subtitle shows total users + superuser count.

### Ajustes *(all users)*
- **Apariencia** — in-card segment selector with three options: Claro / Oscuro / Sistema. Active option has accent background + white text/icon; inactive options are transparent. Persisted via `ThemeManager`; change takes effect instantly app-wide including system status bar and navigation bar icons.
- **Umbrales de estado** (`Icons.Default.BarChart`) *(superuser only)* — navigates to `UmbralesRoute`. Subtitle is dynamic: updates reactively whenever `UmbralesManager` emits a new value.

### Mantenimiento *(superuser only)*
- **Depuración de datos** (`Icons.Default.Delete`, red icon) — navigates to `DepuracionRoute`. Two-phase destructive cleanup: exports old pedidos to CSV/XLSX then hard-deletes them from Supabase. See `docs/features/depuracion.md` for full details.

### Logout
- Red tinted button, clears session and navigates to `LoginRoute` with full backstack pop.

---

## Theme system

### `ThemeMode` enum
`domain/model/ThemeMode.kt` — three values: `LIGHT`, `DARK`, `SYSTEM`.

### `ThemeManager`
`data/prefs/ThemeManager.kt` — `@Singleton`. Persists the selected mode to `SharedPreferences("theme_prefs")` under key `theme_mode` (stored as `ThemeMode.ordinal`). Exposes `StateFlow<ThemeMode>`.

```kotlin
themeManager.setTheme(ThemeMode.DARK)
themeManager.themeMode // StateFlow<ThemeMode>
```

### `EcomerceCarlosVTheme` wiring
`Theme.kt` — accepts a `themeMode: ThemeMode` parameter (default `SYSTEM`). Resolves `darkTheme` from it (`SYSTEM` follows `isSystemInDarkTheme()`). A `SideEffect` calls `WindowCompat.getInsetsController` to flip `isAppearanceLightStatusBars` and `isAppearanceLightNavigationBars` so system bar icon colors track the active theme in real time.

### `MainActivity` wiring
`ThemeManager` is field-injected (`@Inject`). Inside `setContent`, `themeManager.themeMode` is collected via `collectAsStateWithLifecycle` and passed to `EcomerceCarlosVTheme(themeMode = ...)`. The entire app re-themes on every emission without recreating the Activity.

---

## Umbrales de estado screen (`UmbralesRoute`)

Allows superusers to change the thresholds that determine when a client's status becomes **Crítico**.

**File**: `UmbralesScreen.kt`  
**ViewModel**: `UmbralesViewModel.kt`  
**Persistence**: `UmbralesManager` (`@Singleton`, `SharedPreferences("umbrales_prefs")`)

### Fields

| Field | Key | Type | Default |
|-------|-----|------|---------|
| Monto máximo | `monto_maximo` | `Float` (stored) / `Double` (domain) | `200.0` |
| Días máximos | `dias_maximos` | `Int` | `30` |

### Reactivity

`UmbralesManager` exposes `StateFlow<Umbrales>`. All ViewModels that inject it (`DetalleClienteViewModel`, `ClientesViewModel`, `PerfilViewModel`) recompute automatically when `save()` is called — no DB round-trip needed. Client status tags update as soon as the user navigates back to any list.

### Validation

- **Guardar** button enabled only when monto parses as a valid `Double` and días parses as an `Int ≥ 1`.
- `diasText` filters to digits only on input; `montoText` accepts decimal separators (`,` normalized to `.`).

### Data model

```kotlin
// domain/model/Umbrales.kt
data class Umbrales(
    val montoMaximo: Double = 200.0,
    val diasMaximos: Int = 30,
)
```

---

## ViewModel: `PerfilViewModel`

Injects `UmbralesManager` and `ThemeManager`. On `init` it launches:
- A `collect` on `umbralesManager.umbrales` → updates `state.umbralesSummary`.
- A `collect` on `themeManager.themeMode` → updates `state.themeMode`.

`setTheme(mode: ThemeMode)` delegates directly to `themeManager.setTheme(mode)`.

---

## Cambiar contraseña screen

**Route**: `CambiarContrasenaRoute(userId: String, isSelf: Boolean)`  
**Files**: `CambiarContrasenaScreen.kt`, `CambiarContrasenaViewModel.kt`, `CambiarContrasenaUiState.kt`  
**Access**: `SUPERUSUARIO` only (entry points gated in `PerfilScreen` and `UsuarioDetalleScreen`).

### Entry points

| From | Route args | Behaviour |
|------|-----------|-----------|
| `PerfilScreen` → Seguridad → Cambiar contraseña | `userId = currentUser.id, isSelf = true` | User changes their own password. Requires current password to verify identity. |
| `UsuarioDetalleScreen` → Seguridad → Cambiar contraseña | `userId = targetUser.id, isSelf = false` | Superuser sets a new password for another user without needing the current one. |

### `isSelf` flag

Controls three things:

1. **API call** — `isSelf = true`: `supabase.auth.signInWith(Email)` to verify current password, then `supabase.auth.updateUser { password = newPassword }`. `isSelf = false`: `adminClient.auth.admin.updateUserById(userId) { password = newPassword }`.
2. **"Contraseña actual" field** — shown only when `isSelf = true`.
3. **Scope banner** — amber-tinted info banner shown only when `isSelf = false`: "Estableces una contraseña nueva para este usuario. Comunícasela de forma segura."

### Screen layout

- **PwTargetCard** — avatar (`ClienteAvatar`-style), full name, email, role badge. Gives context for who the password is being changed for.
- **Scope banner** *(non-self only)* — amber tinted, describes the admin-reset flow.
- **"Contraseña actual"** field *(self only)* — `Lock` leading icon, eye toggle.
- **"Nueva contraseña"** field — `Lock` leading icon, eye toggle.
- **"Confirmar nueva contraseña"** field — `Lock` leading icon, eye toggle.
- **PwRequirementsCard** — three requirement rows with animated `Check` / `Close` icon:
  - Al menos 8 caracteres
  - Incluye un número
  - Mayúscula y minúscula
- **Error message** — shown below requirements when API call fails.
- **Success overlay** — green-tinted full-screen overlay with `CheckCircle` icon + "Contraseña actualizada" title + context-specific body text. Replaces the form; tapping "Listo" pops back.
- **Bottom bar**: "Cancelar" `OutlinedButton` (pops back) + "Guardar / Actualizar contraseña" `Button` (disabled until `isValid`).

### Validation (`CambiarContrasenaUiState`)

| Property | Rule |
|----------|------|
| `meetsLength` | `newPassword.length >= 8` |
| `meetsNumber` | any digit in `newPassword` |
| `meetsCasing` | at least one uppercase and one lowercase |
| `passwordMismatch` | `confirmPassword.isNotEmpty() && newPassword != confirmPassword` |
| `isValid` | all three requirements met + passwords match + (if `isSelf`) current password non-empty |
