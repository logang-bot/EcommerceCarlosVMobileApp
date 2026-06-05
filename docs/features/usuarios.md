# Feature: Usuarios & Perfil

Design reference: `docs/design/screens-profile.jsx`, `docs/design/screens-users.jsx`

---

## Roles

| Role | Value | Capabilities |
|------|-------|--------------|
| Superusuario | `UserRole.SUPERUSUARIO` | Full access + user management (Gestión de Usuarios) |
| Usuario | `UserRole.USUARIO` | Standard access to Mercados, Clientes, Pedidos |

Role is stored as a `String` (enum name) in Room and transmitted as a plain string in the Supabase DTO.

---

## Model layers

| Layer | Class | Location |
|-------|-------|----------|
| Room entity | `UserEntity` | `data/local/entity/UserEntity.kt` |
| Supabase DTO | `UserDto` | `data/remote/dto/UserDto.kt` |
| Domain model | `AppUser` | `domain/model/AppUser.kt` |
| UI model | `UserUiModel` | `ui/screen/usuario/UserUiModel.kt` |

Mapping is handled by `UserMapper.kt` (`data/mapper/`). `UserUiModel` is purely presentational — it carries `initials`, `isCurrentUser`, `displayRole`, and `lastSeenLabel` so ViewModels don't need to compute these in the UI layer.

### AppUser fields

| Field | Type | Notes |
|-------|------|-------|
| `id` | `String` | UUID |
| `email` | `String` | |
| `name` | `String` | |
| `role` | `UserRole` | Enum stored as string in Room |
| `phone` | `String?` | Nullable; editable from `EditarPerfilScreen` |
| `photoUrl` | `String?` | Reserved for future phase |
| `isActive` | `Boolean` | |
| `createdAt` | `Long` | Epoch millis |
| `lastSeenAt` | `Long?` | |
| `biometricEnabledAt` | `Long?` | Epoch millis when enrolled, null if not enrolled |

**DB version**: 4 (bumped when `phone` column was added; uses `fallbackToDestructiveMigration`).

---

## UserRepository interface

```kotlin
interface UserRepository {
    fun getAll(): Flow<List<AppUser>>
    suspend fun getById(id: String): AppUser?
    suspend fun save(user: AppUser)
    suspend fun delete(id: String)
    suspend fun setActive(id: String, active: Boolean)
    suspend fun setBiometricEnabled(id: String, enabledAt: Long?)
    suspend fun hasBiometricEnabled(): Boolean
    suspend fun getBiometricEnabledUser(): AppUser?       // used by LoginViewModel
    suspend fun updateProfile(id: String, name: String, email: String, phone: String?)
}
```

---

## Session

`SessionManager` interface (`domain/session/SessionManager.kt`) exposes a `StateFlow<AppUser?>` and two mutation methods.

Current implementation (`SessionManagerImpl`) is **in-memory only** — process kill clears the session.

### Session persistence (Phase 9)

Replace `SessionManagerImpl` with a `DataStore<Preferences>`-backed implementation:

```kotlin
// In SessionManagerImpl:
private val dataStore: DataStore<Preferences>   // inject via Hilt
// Key:
val USER_ID_KEY = stringPreferencesKey("current_user_id")
// On setCurrentUser: save user.id to DataStore, store full AppUser in memory
// On init: read id from DataStore, load AppUser from UserRepository
```

---

## Screens

### PerfilScreen (`ui/screen/perfil/`)

- **Route**: `PerfilRoute` (no arguments)
- Reads current user from `SessionManager.currentUser`
- **Pencil icon** (top-right) → navigates to `EditarPerfilRoute`
- Sections:
  - **Cuenta**: email (with subtitle), phone (with subtitle, empty if not set) — both rows tap to `EditarPerfilRoute`
  - **Seguridad**: biometric toggle card (fully functional — see Biometric section), change password row
  - **Equipo** *(SUPERUSUARIO only)*: team summary count, navigates to `GestionUsuariosRoute`
- Logout: calls `sessionManager.clearSession()` then navigates to `LoginRoute` (popping all back stack)
- **Removed**: `businessName` field — no longer shown in the identity header or Cuenta section

### EditarPerfilScreen (`ui/screen/perfil/`)

- **Route**: `EditarPerfilRoute` (no arguments)
- **Reached from**: pencil icon in `PerfilScreen`
- Shows `ProfileAvatar` (104dp initials, with photo hint below)
- Fields: Nombre (required), Correo (required), Teléfono (optional)
- Role field: read-only, shows `RoleBadge` + "Solo un super usuario puede cambiarlo" hint
- Sticky bottom bar: "Guardar cambios" CTA
- On save: calls `userRepository.updateProfile(...)`, updates `SessionManager`, pops back

### GestionUsuariosScreen (`ui/screen/usuario/`)

- **Route**: `GestionUsuariosRoute` (no arguments — superuser gate enforced at navigation call site in PerfilScreen)
- Title: "Gestión de usuarios"; subtitle shows user/superuser counts
- Scope banner with shield icon + bold "super usuarios" in message (built via `buildAnnotatedString`)
- Two sections: "Super usuarios · N" and "Usuarios · N"
- Active user row subtitle: "Activo · lastSeenLabel" (or plain "Activo" if no timestamp)
- FAB "Crear" → `CrearUsuarioRoute`
- Reactive: uses `combine(userRepository.getAll(), sessionManager.currentUser)` to split list and mark current user

### UsuarioDetalleScreen (`ui/screen/usuario/`)

- **Route**: `UsuarioDetalleRoute(userId: String)`
- Shows user header, role selector (two `RoleOption` cards), activity rows
- Save button appears only when role changes
- Activity section: "Última sesión" row only (no "Pedidos creados")
- Inline action buttons at bottom of scroll:
  - `OutlinedButton` "Desactivar usuario" (red outline, stub — Phase 9)
  - `Button` "Eliminar usuario" (red filled, calls `userRepository.delete`)

### CrearUsuarioScreen (`ui/screen/usuario/CrearUsuarioScreen.kt`)

- **Route**: `CrearUsuarioRoute` (no arguments)
- Composable function: `CrearUsuarioScreen` (backed by `CrearUsuarioViewModel`)
- Fields: Nombre, Correo, Contraseña temporal (with hint "El usuario podrá cambiarla luego desde su perfil."), Rol
- Password validated as non-blank; value stored locally only — not persisted to `AppUser` until Phase 9 Supabase wiring
- CTA: "Crear usuario"
- On submit: creates `AppUser` with `UUID.randomUUID()` and saves to Room

---

## Create User API (Phase 9)

Replace `CrearUsuarioViewModel.onCreate()` with a Supabase call using the temp password:

```kotlin
// Using supabase-kt auth-kt admin API:
supabaseClient.auth.admin.createUserWithEmailAndPassword(
    email = state.email,
    password = state.password,
    data = buildJsonObject {
        put("name", state.name)
        put("role", state.role.name)
    }
)
```

---

## Biometric

Biometric login is fully implemented end-to-end.

### How it works

1. **Device check** (`PerfilViewModel.loadProfile`): `BiometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)` determines if the device has enrolled biometrics. If not available, the toggle is shown at 55% opacity and is non-interactive.

2. **Enabling** (PerfilScreen toggle → ON): `BiometricPrompt.authenticate()` is called directly from the click handler. On success, `PerfilViewModel.onBiometricAuthSuccess()` saves `System.currentTimeMillis()` to `users.biometricEnabledAt` in Room and updates the session.

3. **Disabling** (PerfilScreen toggle → OFF): `PerfilViewModel.disableBiometric()` sets `biometricEnabledAt = null` in Room and session.

4. **Login screen**: `LoginViewModel.init` calls `checkBiometricAvailability()` — if device is ready AND `hasBiometricEnabled()`, it also calls `getBiometricEnabledUser()` to populate the enrolled-user fields and shows the "usuario recurrente" state.

5. **Biometric login**: `BiometricPrompt` in `LoginScreen` calls `viewModel.onBiometricSuccess(onLoginSuccess)` → loads user from Room → sets session → navigates to `HomeRoute`.

### PromptInfo configuration

```kotlin
BiometricPrompt.PromptInfo.Builder()
    .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
    .setNegativeButtonText(...)
    .build()
```

`setAllowedAuthenticators` must match the `canAuthenticate` check exactly. Without it, `BiometricPrompt` defaults to `BIOMETRIC_STRONG` only, which causes a silent `IllegalArgumentException` on devices that only offer Class 2 biometrics (face unlock).

### AppCompatActivity requirement

`BiometricPrompt` from `androidx.biometric:biometric:1.1.0` requires a `FragmentActivity`. Therefore `MainActivity` extends `AppCompatActivity`. Both `PerfilScreen` and `LoginScreen` obtain the `FragmentActivity` via `Context.findFragmentActivity()`.

---

## Key design tokens used

| Token | Usage |
|-------|-------|
| `extendedColors.banana` | Superuser `RoleBadge` background |
| `extendedColors.bananaText` | Superuser badge text + "Equipo" row icon tint |
| `extendedColors.bananaTint` | Superuser scope banner bg in GestionUsuariosScreen |
| `extendedColors.accentSoft` | BiometricCard background when enrolled; hint pill background in EmptyState; selected nav item indicator |
| `extendedColors.accentTint` | BiometricCard icon box background when enrolled |
| `extendedColors.redTint` | Logout button bg; "Eliminar usuario" button bg |
| `extendedColors.redText` | Danger text color; "Desactivar usuario" outline + text |
