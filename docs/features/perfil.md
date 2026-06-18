# Mi Perfil

Screen accessible from the Home bottom bar. Shows identity info, security settings, team management (superusers), and appearance settings (all users).

## Sections

### Cuenta
- **Correo** (`Icons.Default.Email`) — taps to `EditarPerfilRoute`
- **Teléfono** (`Icons.Default.Phone`) — taps to `EditarPerfilRoute`

### Seguridad
- **BiometricCard** — toggle to enroll/disable biometric auth. Uses `BiometricPrompt` from `androidx.biometric`. State persisted via `UserRepository.setBiometricEnabled(userId, timestamp)`.
- **Cambiar contraseña** (`Icons.Default.Lock`) — placeholder; password change flow is a future phase.

### Equipo *(superuser only)*
- **Gestión de usuarios** — navigates to `GestionUsuariosRoute`. Subtitle shows total users + superuser count.

### Ajustes *(all users)*
- **Apariencia** — in-card segment selector with three options: Claro / Oscuro / Sistema. Active option has accent background + white text/icon; inactive options are transparent. Persisted via `ThemeManager`; change takes effect instantly app-wide including system status bar and navigation bar icons.
- **Umbrales de estado** (`Icons.Default.BarChart`) *(superuser only)* — navigates to `UmbralesRoute`. Subtitle is dynamic: updates reactively whenever `UmbralesManager` emits a new value.

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
