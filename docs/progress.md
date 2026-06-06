# Project Progress

High-level phase tracker. Details for each feature live in `docs/features/`.

---

## Phases

| Phase | Scope | Status |
|-------|-------|--------|
| 1 | Navigation scaffold, theme, Login screen | ✅ Done |
| 2 | Mercados list, Detalle de Mercado, Create Mercado | ✅ Done |
| 2b | Perfil y Seguridad, Gestión de Usuarios (roles, biometric toggle, crear usuario) | ✅ Done |
| 2c | Biometric fully functional, empty states redesign, UI polish | ✅ Done |
| 2d | Login two-state (enrolled user screen), bottom navigation, Editar Perfil screen | ✅ Done |
| 2e | User management screens redesign: Gestión de Usuarios, UsuarioDetalle, CrearUsuario | ✅ Done |
| 2f | Mercados: long-press selection + edit, contextual action bar, Búsqueda Global screen stub, MercadoDto | ✅ Done |
| 3 | Detalle de Cliente, Crear Cliente, Saldo Extra | ✅ Done |
| 4 | Creación de Pedido (cart flow) | ✅ Done |
| 5 | Detalle de Pedido, Historial de Pagos | ✅ Done |
| 6 | Catálogo de Productos, Crear/Editar Producto | ✅ Done |
| 7 | Lista Negra, Agregar a Lista Negra | ✅ Done |
| 8 | Reporte Diario (Búsqueda Global UI already done in 2f — wire results in Phase 3) | 🔲 Pending |
| 9 | Supabase auth + sync layer, DataStore session persistence | 🔲 Pending |

---

## ⚠️ Critical blockers

> These items make the Login screen non-functional in production. Phase 9 must address both.

### 🔐 Supabase Authentication — stub active, no real auth
The "Iniciar sesión" button currently does a fake 800ms delay and always succeeds.
See **`docs/features/auth.md → Supabase Authentication`** for the full implementation checklist.
Prerequisite: add credentials to `local.properties`:
```
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
```

### 💾 Session persistence — in-memory only
`SessionManagerImpl` stores the current user in a `MutableStateFlow`. Session is lost on process kill.
Phase 9 must persist it via `DataStore<Preferences>`. See `docs/features/usuarios.md → Session persistence`.

### 🔗 Create user — saves locally, no Supabase sync
`CrearUsuarioViewModel` validates a temp password but only creates an `AppUser` in Room.
Phase 9 must wire this to the Supabase admin create-user API. See `docs/features/usuarios.md → Create User API`.

---

## ✅ Resolved in Phase 2c

### 👆 Biometric Authentication — now fully functional
`BiometricPrompt` is wired to the device's native authentication dialog. Enrollment date is persisted to Room (`biometricEnabledAt` column on `users` table). The login screen hides the biometric button unless the feature is enabled. See `docs/features/usuarios.md → Biometric`.

**Root cause of original failure**: `MainActivity` extended `ComponentActivity`, which does not extend `FragmentActivity`. `BiometricPrompt` requires a `FragmentActivity`. Fix: `MainActivity` now extends `AppCompatActivity` (which IS a `FragmentActivity`).

---

## Other open action items

- **Fonts**: ✅ Geist variable fonts added (`geist_variable.ttf`, `geist_mono_variable.ttf`)
- **Icons**: `ic_shield_check.xml` replaced by `ic_admin_panel.xml` (imported). `ic_users.xml` imported. Both used via `painterResource(R.drawable.*)` at all call sites. `PedidosIcons.kt` removed.

---

## Phase 3 (completion) — implemented

- `SaldoExtraScreen` + `SaldoExtraViewModel` + `SaldoExtraUiState` — form with locked category, description, amount, `DatePickerDialog`
- `SaldoExtraRoute(clienteId)` wired in `AppRoutes` + `AppNavigation`
- `CreateSaldoExtraUseCase` — creates a `Pedido` with `isSaldoExtra=true`, no line items, `notes` = description
- `isSaldoExtra: Boolean` flag added to `PedidoEntity` / `Pedido` / `PedidoMapper` / `PedidoDto`
- DB migration 9→10 (adds `isSaldoExtra` column); Room version bumped to 10
- `PedidoRow` updated: amber Tag icon + "Manual" badge for saldo-extra rows

## Phase 4 — implemented

- `PedidoEntity`, `DetallePedidoEntity` — Room tables with FK cascade from `clientes`
- `PedidoDao`, `DetallePedidoDao` — full CRUD
- `PedidoMapper`, `DetallePedidoMapper` — entity ↔ domain
- `PedidoDto`, `DetallePedidoDto` — Supabase-ready (Phase 9)
- `PedidoRepository` interface + `PedidoRepositoryImpl`
- `CreatePedidoUseCase` — creates pedido + line items atomically
- `CreacionPedidoScreen` — 3-column product grid, active search bar, CartPanel, LineEditSheet, PagoSheet
- `DetalleClienteScreen` — now shows live pedido list; balance/status computed from real pedido data
- `PayChip` — shared composable for PAID/PARTIAL/PENDING status
- DB migration 8→9

## Phase 5 — implemented

- `DetallePedidoScreen` — Scaffold with date subtitle + `PayChip` in top bar; saldo-extra branch (shows notes) vs normal branch (`LineItemsSection`)
- `TotalBlock` — total / paid (green) / saldo restante (amber) rows with divider
- `DetallePedidoBottomBar` — "Registrar pago parcial" + "Marcar como pagado" (hidden when PAID/isSaving)
- `PagoParacialSheet` — `ModalBottomSheet` with decimal amount input; amount clamped to remaining balance
- `DetallePedidoViewModel` — 4-flow `combine`; `onMarcarPagado` / `onRegistrarPago` with PARTIAL/PAID status logic
- `DetallePedidoLineItem` — `LineItemRow` with strikethrough catalog price when overridden, `PriceModifiedHint`
- `DetalleClienteScreen` `onPedidoClick` wired to `DetallePedidoRoute(pedidoId)` (TODO resolved)

## MercadosScreen live stats — implemented

- `PedidoRepository.getAllUnpaid()` — new DAO + repo query for all non-PAID pedidos
- `MercadosViewModel` now combines mercados + all clients + unpaid pedidos to compute `MercadoStat` per mercado
- `MercadoStat(activeClientCount, hasWarning, hasCritical)` drives `MercadoStatRow` in each mercado row
- Status dot: 6dp amber circle (ADVERTENCIA), red circle (CRITICO), hidden when AL_DIA; text color follows status

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
| AppCompat | 1.7.0 |
| Biometric | 1.1.0 |
| Room | 2.8.4 |
