# Mi Perfil

Screen accessible from the Home bottom bar. Shows identity info, security settings, team management (superusers), and appearance settings (all users).

## Files

| File | Purpose |
|------|---------|
| `ui/screen/perfil/PerfilScreen.kt` | `PerfilScreen` (ViewModel + biometric prompt wiring) + `PerfilContent` (state-driven, previewable); Cuenta/Seguridad/Equipo/Logout inline; `SectionHeader` (internal, shared by the section files below) |
| `ui/screen/perfil/AjustesSection.kt` | Apariencia card (theme selector) + Umbrales de estado row; own light/dark previews |
| `ui/screen/perfil/MantenimientoSection.kt` | Depuración de datos row; own light/dark previews |
| `ui/screen/perfil/BiometricCard.kt` | Biometric enroll/disable card |

## Sections

### Cuenta
- **Correo** (`Icons.Default.Email`) — taps to `EditarPerfilRoute`
- **Teléfono** (`Icons.Default.Phone`) — taps to `EditarPerfilRoute`

### Seguridad
- **BiometricCard** — toggle to enroll/disable biometric auth. Uses `BiometricPrompt` from `androidx.biometric`. State persisted via `UserRepository.setBiometricEnabled(userId, timestamp)`.
- **Cambiar contraseña** (`Icons.Default.Lock`) — visible only to `SUPERUSUARIO`. Navigates to `CambiarContrasenaRoute(userId = state.userId, isSelf = true)`. See [Cambiar contraseña screen](#cambiar-contraseña-screen) below.

### Equipo *(superuser only)*
- **Gestión de usuarios** — navigates to `GestionUsuariosRoute`. Subtitle shows total users + superuser count.

### Ajustes *(all users)* — `AjustesSection.kt`
- **Apariencia** — in-card segment selector with three options: Claro / Oscuro / Sistema. Active option has accent background + white text/icon; inactive options are transparent. Persisted via `ThemeManager`; change takes effect instantly app-wide including system status bar and navigation bar icons.
- **Umbrales de estado** (`Icons.Default.BarChart`) *(superuser only, `showUmbrales` param)* — navigates to `UmbralesRoute`. Subtitle is dynamic: updates reactively whenever `UmbralesRepository.getUmbrales()` emits a new value.

### Mantenimiento *(superuser only)* — `MantenimientoSection.kt`
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
**Persistence**: Supabase-synced, Room-backed — `UmbralesRepository` / `UmbralesRepositoryImpl` (see `docs/db-schema.md` → `umbrales` table). Previously local-only (`SharedPreferences` via a now-deleted `UmbralesManager`), which meant each device kept its own thresholds with no way to keep a team consistent; that's why it was moved onto the same delta-sync/queue infrastructure as every other entity (`EntityType.UMBRALES`, `UmbralesSyncer`, `UmbralesDao`).

### Fields

| Field | Key | Type | Default |
|-------|-----|------|---------|
| Monto máximo | `monto_maximo` | `float8` (Supabase) / `Double` (domain) | `200.0` |
| Días máximos | `dias_maximos` | `int4` (Supabase) / `Int` (domain) | `30` |

### Write access

The Supabase `umbrales_insert_superusuario`/`umbrales_update_superusuario` RLS policies restrict writes to `SUPERUSUARIO` — matching the app UI, which only shows the Umbrales row/screen to that role (`showUmbrales` in `AjustesSection`). All roles can read, since client status must compute identically everywhere.

### Reactivity

`UmbralesRepository.getUmbrales()` returns a `Flow<Umbrales>` backed by Room (`UmbralesDao.getFlow()`). All ViewModels that inject it (`DetalleClienteViewModel`, `ClientesViewModel`, `PerfilViewModel`) recompute automatically whenever the local row changes — either from `save()` (writes to Room immediately, then queues a push to Supabase) or from a background pull-sync picking up another user's edit. Client status tags update as soon as the user navigates back to any list.

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

Injects `UmbralesRepository` and `ThemeManager`. On `init` it launches:
- A `collect` on `umbralesRepository.getUmbrales()` → updates `state.umbralesSummary`.
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

1. **API call** — `isSelf = true`: `supabase.auth.signInWith(Email)` to verify current password, then `supabase.auth.updateUser { password = newPassword }` (regular client). `isSelf = false`: `adminUserService.resetPassword(userId, newPassword)`, which invokes the `reset-user-password` Edge Function server-side (service role) — the app no longer holds the secret key. See `docs/supabase-setup.md` §9.
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
