# Feature: Usuarios & Perfil

Design reference: `docs/design/screens-profile.jsx`, `docs/design/screens-users.jsx`

---

## Roles

| Role | Value | Capabilities |
|------|-------|--------------|
| Superusuario | `UserRole.SUPERUSUARIO` | Full access to all tables + user management; only role that can create/edit/delete other users and change roles |
| Usuario | `UserRole.USUARIO` | Can read and write Mercados, Clientes, Productos, Pedidos — cannot access Gestión de Usuarios |
| Invitado | `UserRole.INVITADO` | Read-only access to all business tables; cannot modify anything |

Role is stored as a `String` (enum name) in Room and transmitted as a plain string in the Supabase DTO.
RLS enforces these permissions server-side — see `docs/sql/rls.sql`.

### canWrite pattern (UI enforcement)

Each business screen's UiState carries a `canWrite: Boolean` field:

```kotlin
val canWrite: Boolean = true   // default — safe for previews
```

Computed in every ViewModel that touches a business screen:
```kotlin
canWrite = user?.role != UserRole.INVITADO
```

`user` comes from `sessionManager.currentUser` (a `StateFlow<AppUser?>`) combined alongside the screen's data flows.

**What `canWrite = false` hides:**

| Screen | Hidden elements |
|--------|----------------|
| MercadosScreen | FAB "Mercado" |
| DetalleMercadoScreen | Edit icon, Danger zone (delete button) |
| ClientesScreen | FAB "Nuevo cliente", empty-state action button |
| DetalleClienteScreen | Edit icon, FAB "Nuevo Pedido", all `ActionButtons` (blacklist / saldo extra) |
| DetallePedidoScreen | Edit icon, payment bottom bar |
| CatalogoScreen | FAB "Producto", row tap + chevron (rows become non-clickable) |

User management (`GestionUsuariosScreen`) is gated separately: `PerfilScreen` only shows the Equipo navigation row when `state.role == UserRole.SUPERUSUARIO` — so USUARIO and INVITADO never see the link.

**RLS note:** hiding UI buttons is the UX layer. The Supabase RLS policies in `docs/sql/rls.sql` independently enforce that INVITADO users cannot execute INSERT/UPDATE/DELETE via direct API calls either.

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
| `photoUrl` | `String?` | Local FileProvider URI; set from `EditarPerfilScreen` |
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
    suspend fun updateProfile(id: String, name: String, email: String, phone: String?, photoUrl: String?)
}
```

---

## Session

`SessionManager` interface (`domain/session/SessionManager.kt`) exposes a `StateFlow<AppUser?>` and two mutation methods.

`SessionManagerImpl` subscribes to `supabase.auth.sessionStatus`. On `Authenticated`, it reads
the userId from the JWT, loads the `AppUser` from Room, and emits it. The Supabase JWT itself is
persisted by `DataStoreGoTrueSessionManager` so sessions survive process death without an extra
network call.

### Local data wipe on sign-out

> **Superseded in Phase 16.** `SessionStatus.NotAuthenticated` no longer wipes anything. It also fires
> for the startup session clear and for a mid-session refresh failure, so wiping on it destroyed cached
> business data — including unsynced `sync_operations` rows — behind the user's back. The wipe now lives
> only in `signOut()`, which is the one place an explicit intent exists.

| Scenario | Trigger | Outcome |
|----------|---------|---------|
| Explicit logout, fingerprint enrolled | `signOut()` → `discardAccessToken()` | Data **kept**; refresh token kept for the next fingerprint tap |
| Explicit logout, not enrolled | `signOut()` → `revokeAndWipe()` | Refresh token revoked server-side; data wiped **unless writes are still queued** (Phase 16c) |
| Startup session clear / cold start | `NotAuthenticated` from the first-load wipe | Nothing wiped — only the session is gone |
| `RefreshFailure` (offline) | supabase-kt retry loop alive | Nothing wiped; cached data serves the offline session |
| Refresh token rejected | `endSession()` → `sessionEnded` | Nothing wiped; the user is returned to Login to re-authenticate |

`database.clearAllTables()` (Room built-in) is used — no custom DAO methods needed.

See `docs/features/auth.md` § "Sign-out is two-tier" and § "Device ownership" for the full rules,
including what happens when a **different** user signs in over another's queued writes.

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
- **Photo section**: 104dp circle — shows actual photo (`BitmapFactory` via `LaunchedEffect`) when `photoUri` is set, falls back to `ProfileAvatar` (initials); 34dp accent camera button overlay at bottom-right; tapping either opens a bottom sheet (camera / gallery). Gallery picks are copied to `cacheDir/images/` via `copyImageToCache()` for persistence.
- Fields: Nombre (required), Correo (required), Teléfono (optional)
- Role field: read-only, shows `RoleBadge` + "Solo un super usuario puede cambiarlo" hint
- Sticky bottom bar: "Guardar cambios" CTA
- On save: calls `userRepository.updateProfile(...)` (now includes `photoUrl`), updates `SessionManager`, pops back
- `EditarPerfilUiState` exposes `photoUri: Uri?`; restored from `user.photoUrl` on load

### GestionUsuariosScreen (`ui/screen/usuario/`)

- **Route**: `GestionUsuariosRoute` (no arguments — superuser gate enforced at navigation call site in PerfilScreen)
- Title: "Gestión de usuarios"; subtitle: "X miembros · Y super usuarios"
- Scope banner with shield icon + bold "super usuarios" in message (built via `buildAnnotatedString`)
- Three sections: "Super usuarios · N", "Usuarios · N", "Invitados · N" (each shown only if non-empty)
- Inactive user row: rendered at 55% opacity
- Active user row subtitle: "Activo · lastSeenLabel" (or plain "Activo" if no timestamp)
- FAB "Crear" → `CrearUsuarioRoute`
- Reactive: uses `combine(userRepository.getAll(), sessionManager.currentUser)` to split list and mark current user

### UsuarioDetalleScreen (`ui/screen/usuario/`)

- **Route**: `UsuarioDetalleRoute(userId: String)`
- Shows user header, role selector (three `RoleOption` cards), activity rows
- Save button appears only when `selectedRole != user.role`
- Permissions list in a `RoleOption` is shown **only when that card is selected** (collapsed otherwise)
- Activity section: "Última sesión" row only
- Inline action buttons at bottom of scroll (all privileged actions call `AdminUserService`,
  which invokes a server-side Edge Function — see `docs/supabase-setup.md` §9):
  - Role change (Save) → `onSaveRole` → `update-user-role` fn (`ban_duration` unaffected)
  - If `user.isActive`: `OutlinedButton` "Desactivar usuario" (red outline) → `onDeactivate` → `set-user-active` fn (`ban_duration = "876000h"` + `is_active = false`)
  - If `!user.isActive`: `OutlinedButton` "Activar usuario" (green outline, Check icon) → `onActivate` → `set-user-active` fn (`ban_duration = "none"` + `is_active = true`)
  - `Button` "Eliminar usuario" (red filled) → `onDelete` → `delete-user` fn (removes from Supabase auth + `users` table), then Room

### CrearUsuarioScreen (`ui/screen/usuario/CrearUsuarioScreen.kt`)

- **Route**: `CrearUsuarioRoute` (no arguments)
- Fields: Nombre, Correo, Contraseña temporal (with hint "El usuario podrá cambiarla luego desde su perfil."), Rol
- Role picker: three cards (SUPERUSUARIO, USUARIO, INVITADO); permissions list expands only for the selected card
- CTA: "Crear usuario"
- On submit: calls `adminUserService.createUser(...)` (the `create-user` Edge Function) + upserts to Room

---

## Create User API (Phase 9; hardened later — server-side)

`CrearUsuarioViewModel.onCreate()` calls `AdminUserService.createUser(...)`, which invokes the
`create-user` Edge Function. The function runs with the service role **on the server** (the app
no longer bundles the secret key — see `docs/supabase-setup.md` §9), creates the Supabase Auth
user and inserts the `users` profile row atomically, and returns the created row:

```kotlin
// CrearUsuarioViewModel
val created = adminUserService.createUser(
    email = state.email, password = state.password,
    name = state.name, role = state.role.name,
)
// → POST /functions/v1/create-user  (carries the caller's SUPERUSUARIO JWT)
```

Then upserts an `AppUser` (with `created.id`) to Room so the local database stays in sync.
The function maps a duplicate email to the Spanish message "Ya existe un usuario con ese correo".

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
| `extendedColors.bananaText` | Superuser badge text + scope banner icon tint |
| `extendedColors.bananaTint` | Superuser scope banner bg; SUPERUSUARIO `RoleOption` icon box bg |
| `extendedColors.accentSoft` | BiometricCard background when enrolled; selected `RoleOption` card bg |
| `extendedColors.accentTint` | BiometricCard icon box when enrolled; USUARIO `RoleBadge` bg |
| `colorScheme.primary` | USUARIO `RoleBadge` text; selected card border + radio fill |
| `extendedColors.blueTint` | INVITADO `RoleBadge` bg + `RoleOption` icon box bg |
| `extendedColors.blueText` | INVITADO `RoleBadge` text + icon tint |
| `extendedColors.redTint` | Logout button bg; "Eliminar usuario" button bg |
| `extendedColors.redText` | Danger text color; "Desactivar usuario" outline + text |
| `extendedColors.greenText` | "Activar usuario" outline + text |
