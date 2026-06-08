# Mi Perfil

Screen accessible from the Home bottom bar. Shows identity info, security settings, and (for superusers) team management and system configuration.

## Sections

### Cuenta
- **Correo** (`Icons.Default.Email`) — taps to `EditarPerfilRoute`
- **Teléfono** (`Icons.Default.Phone`) — taps to `EditarPerfilRoute`

### Seguridad
- **BiometricCard** — toggle to enroll/disable biometric auth. Uses `BiometricPrompt` from `androidx.biometric`. State persisted via `UserRepository.setBiometricEnabled(userId, timestamp)`.
- **Cambiar contraseña** (`Icons.Default.Lock`) — placeholder; password change flow is a future phase.

### Equipo *(superuser only)*
- **Gestión de usuarios** — navigates to `GestionUsuariosRoute`. Subtitle shows total users + superuser count.

### Ajustes *(superuser only)*
- **Umbrales de estado** (`Icons.Default.BarChart`) — navigates to `UmbralesRoute`. Subtitle is dynamic: updates reactively whenever `UmbralesManager` emits a new value (via `PerfilViewModel`).

### Logout
- Red tinted button, clears session and navigates to `LoginRoute` with full backstack pop.

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

Injects `UmbralesManager`. On `init` it launches a `collect` on `umbralesManager.umbrales` and updates `state.umbralesSummary` with a human-readable string (`"Crítico desde Bs. X o Y días sin pagar"`). This keeps the subtitle in the Perfil row live without any manual refresh.
