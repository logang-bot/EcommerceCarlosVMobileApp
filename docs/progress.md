# Project Progress

High-level phase tracker. Details for each feature live in `docs/features/`.

---

## Phases

| Phase | Scope | Status |
|-------|-------|--------|
| 1 | Navigation scaffold, theme, Login screen | ✅ Done |
| 2 | Mercados list, Detalle de Mercado, Create Mercado | ✅ Done |
| 2b | Perfil y Seguridad, Gestión de Usuarios (roles, biometric toggle, invite) | ✅ Done |
| 3 | Detalle de Cliente, Saldo Extra | 🔲 Next |
| 4 | Creación de Pedido (cart flow) | 🔲 Pending |
| 5 | Detalle de Pedido, Historial de Pagos | 🔲 Pending |
| 6 | Catálogo de Productos, Crear/Editar Producto | 🔲 Pending |
| 7 | Lista Negra, Agregar a Lista Negra | 🔲 Pending |
| 8 | Búsqueda Global, Reporte Diario | 🔲 Pending |
| 9 | Supabase auth + sync layer, DataStore session persistence | 🔲 Pending |

---

## ⚠️ Critical blockers

> These two items make the Login screen non-functional in production. Phase 9 must address both.

### 🔐 Supabase Authentication — stub active, no real auth
The "Iniciar sesión" button currently does a fake 800ms delay and always succeeds.
See **`docs/features/auth.md → Supabase Authentication`** for the full implementation checklist.
Prerequisite: add credentials to `local.properties`:
```
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
```

### 👆 Biometric Authentication — button is a no-op
"Entrar con huella" renders correctly but its `onClick` does nothing.
See **`docs/features/auth.md → Biometric Authentication`** for the full implementation checklist.
Requires: `androidx.biometric:biometric` dependency.

### 💾 Session persistence — in-memory only
`SessionManagerImpl` stores the current user in a `MutableStateFlow`. Session is lost on process kill.
Phase 9 must persist it via `DataStore<Preferences>`. See `docs/features/usuarios.md → Session persistence`.

### 🔗 Invite flow — saves locally, no email sent
`InvitarUsuarioViewModel` creates an `AppUser` in Room but does not call Supabase or send any email.
Phase 9 must wire this to Supabase user-invite API. See `docs/features/usuarios.md → Invite API`.

---

## Other open action items

- **Fonts**: ✅ Geist variable fonts added (`geist_variable.ttf`, `geist_mono_variable.ttf`)

---

## Build config snapshots

| Tool | Version |
|------|---------|
| Kotlin | 2.2.10 |
| AGP | 9.2.1 |
| KSP | 2.2.10-2.0.2 |
| Compose BOM | 2026.02.01 |
| Hilt | 2.59.2 |
| Navigation Compose | 2.8.4 |
