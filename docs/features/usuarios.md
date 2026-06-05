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
- Sections:
  - **Cuenta**: business name, email, phone (chevron rows, Phase 3 edit flow TBD)
  - **Seguridad**: biometric toggle (`onBiometricToggle` → stub, Phase 9), change password row
  - **Equipo** *(SUPERUSUARIO only)*: team summary count, navigates to `GestionUsuariosRoute`
- Logout: calls `sessionManager.clearSession()` then pops back to `LoginRoute`

### GestionUsuariosScreen (`ui/screen/usuario/`)

- **Route**: `GestionUsuariosRoute` (no arguments — superuser gate enforced at navigation call site in PerfilScreen)
- Two sections: "Super usuarios · N" and "Usuarios · N"
- FAB "Invitar" → `InvitarUsuarioRoute`
- Reactive: uses `combine(userRepository.getAll(), sessionManager.currentUser)` to split list and mark current user

### UsuarioDetalleScreen (`ui/screen/usuario/`)

- **Route**: `UsuarioDetalleRoute(userId: String)`
- Shows user header, role selector (two `RoleOption` cards), activity rows
- Save button appears only when role changes
- "Reenviar acceso" and "Desactivar usuario" actions (stub — Phase 9)

### InvitarUsuarioScreen (`ui/screen/usuario/`)

- **Route**: `InvitarUsuarioRoute` (no arguments)
- Name + email fields with validation; role picker
- On submit: creates `AppUser` with `UUID.randomUUID()` and saves to Room
- **Does not send any email or call Supabase** — see Invite API below

---

## Invite API (Phase 9)

Replace `InvitarUsuarioViewModel.sendInvite()` with a Supabase call:

```kotlin
// Using supabase-kt auth-kt:
supabaseClient.auth.admin.inviteUserByEmail(
    email = state.email,
    data = buildJsonObject {
        put("name", state.name)
        put("role", state.role.name)
    }
)
```

The Supabase project must have **email invites enabled** in the Auth settings dashboard.

---

## Biometric toggle (Phase 9)

`PerfilViewModel.onBiometricToggle()` currently flips an in-memory boolean. Full implementation:

```kotlin
val biometricManager = BiometricManager.from(context)
if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS) {
    val prompt = BiometricPrompt(activity, executor, callback)
    prompt.authenticate(promptInfo)
}
```

Requires `androidx.biometric:biometric` dependency in `app/build.gradle.kts`.

---

## Key design tokens used

| Token | Usage |
|-------|-------|
| `extendedColors.banana` | Superuser `RoleBadge` background |
| `extendedColors.bananaText` | Superuser badge text + "Equipo" row icon tint |
| `extendedColors.bananaTint` | Superuser scope banner bg in GestionUsuariosScreen |
| `extendedColors.accentSoft` | BiometricCard background when enrolled |
| `extendedColors.accentTint` | BiometricCard icon box background |
| `extendedColors.redTint` | Logout button bg, "Desactivar" button bg |
| `extendedColors.redText` | Danger action text color |
