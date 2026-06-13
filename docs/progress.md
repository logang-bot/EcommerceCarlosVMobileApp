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
| 2h | Splash screen, app icon, app rename, real logo in Login, biometric screen redesign, file splits | ✅ Done |
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

## ✅ Resolved post-Phase 7

### ⚖️ Client status decoupled from saldo-extra balance

`computeStatus()` now uses a separate `statusBalance` (only `PARTIAL && !isSaldoExtra` pedidos) instead of the full display balance. The displayed "Saldo pendiente total" still includes saldo-extra entries, but the red/amber/green badge and row gradient are driven exclusively by real unpaid orders.

**Before:** a client with only saldo-extra debt would show as ADVERTENCIA/CRITICO.  
**After:** that client shows as AL_DIA in color/badge; the extra amount is still visible in the balance block.

Applied identically in both `DetalleClienteViewModel` and `ClientesViewModel`. See **`docs/features/clientes.md → Client Status Thresholds`** for the full rule.

---

### 🚫 Lista Negra — row navigation, balance redesign, unblacklist resolution sheet, filter chips

Four improvements applied to the lista negra / detalle cliente flow:

1. **Row navigation in `ListaNegraScreen`** — `BlacklistRow` is now clickable and navigates to `DetalleClienteRoute(clienteId)`, allowing users to view the client detail and remove them from the blacklist.

2. **`DetalleClienteScreen` balance redesign** — `BalanceBlock` is now a unified component with three states:
   - *Normal*: status-based gradient, "Saldo pendiente total", 32sp, `ClienteStatusBadge`
   - *AUTO blacklisted*: same as normal + full-width "Lista Negra" `BalanceCard` in the breakdown
   - *MANUAL blacklisted*: red gradient, "Saldo en Lista Negra", 29sp `redText`, circular "⊘ Manual" badge + `BalanceCaption` info line + breakdown cards shown as frozen/inactive
   - `BalanceBreakdown` always rendered: side-by-side "Pedidos" (blue) + "Saldo extra" (amber) `BalanceCard`s; plus full-width LN card when AUTO blacklisted

3. **Unblacklist resolution sheet (`QuitarListaNegraSheet`)** — When `blacklistIsManualAmount == true`, tapping "Quitar de Lista Negra" opens a `ModalBottomSheet` (mockup-faithful design: centered green header icon, radio-style option cards with icon tile + radio circle, amber info banner). Two options:
   - *Restaurar pedidos y saldos* — clears the blacklist; pedidos are unchanged. Pre-selected by default.
   - *Marcar todo como pagado* — marks all non-PAID pedidos as PAID. If `blacklistBalance > (pedidosBalance + extraBalance)`, a saldo extra for the difference is created via `CreateSaldoExtraUseCase` before the client is unblacklisted. Option is disabled when `blacklistIsManualAmount == false`.
   When `blacklistIsManualAmount == false` (AUTO), the client is unblacklisted immediately with no sheet.

4. **"Cuenta" section with filter chips** — The pedidos section is renamed "Cuenta". A three-dot `PedidosMenuButton` in the section header lets the user filter by Pendiente / Parcial / Pagado (toggle, multi-select). Active filters appear as colored status chips. Section header shows "N de M" count. ViewModel computes `pedidosBalance`, `extraBalance`, `unpaidPedidosCount`, `unpaidExtraCount` from the unfiltered list.

**DB:** Room migration 11→12 adds `blacklistIsManualAmount INTEGER NOT NULL DEFAULT 0` to `clientes`. Existing blacklisted rows default to `false` (treated as AUTO). DB version bumped to 12.

### 💥 FK cascade data loss on mercado/cliente save — fixed

**Root cause:** Room 2.7+ enables `PRAGMA foreign_keys = ON` by default. All parent-table DAOs (`MercadoDao`, `ClienteDao`) used `@Insert(onConflict = REPLACE)`, which internally DELETEs the old row before inserting the replacement — firing `ON DELETE CASCADE` and wiping all child rows (clientes, pedidos, detalle_pedido).

**Symptom:** Editing a mercado's location field deleted all clientes and pedidos belonging to that mercado.

**Fix:** All parent-table DAOs switched to `@Insert(onConflict = IGNORE)` returning `Long`. Repositories now fall through to `@Update` when `insert()` returns `-1`. `PedidoDao` and `DetallePedidoDao` also switched to `IGNORE` as a precaution. See `docs/db-schema.md → Data integrity` for the canonical pattern.

### 💾 `fallbackToDestructiveMigration` removed

Removed `.fallbackToDestructiveMigration(dropAllTables = true)` from `DatabaseModule`. Room now throws on a missing migration instead of silently dropping all tables.

### 🖼️ Profile photo not rendering — fixed

`ProfileAvatar` composable updated to accept an optional `photoUrl` parameter and delegate to `PhotoThumbnail` (initials as fallback). Wired through `PerfilUiState`, `PerfilViewModel`, `LoginFormState`, and `LoginViewModel` so both the profile screen and the biometric login welcome card show the user's photo when set.

### 📍 DetalleMercadoScreen — UBICACIÓN section always visible

Removed the `if (!mercado.mapsUrl.isNullOrBlank())` guard around the UBICACIÓN section. `MapsLinkField` already handles blank values gracefully (shows placeholder, hides "Abrir" chip).

### 🔄 DetalleMercadoScreen — stale data after editing

`DetalleMercadoViewModel` was a one-shot `init` loader. Replaced with a reactive `stateIn` over `MercadoRepository.getByIdFlow(mercadoId)`. Added `getByIdFlow(id)` to `MercadoDao` (Flow-returning query), `MercadoRepository` interface, and `MercadoRepositoryImpl`. The screen now updates automatically when any edit is saved.

### 👤 Profile state not reflecting edits immediately

`PerfilViewModel.loadProfile()` was reading `sessionManager.currentUser.value` once in an `init` coroutine. Changed to `sessionManager.currentUser.collect { … }` so the UI reacts to session updates without a navigate-back/navigate-in cycle.

### 🏠 Home screen avatar not showing profile photo

`MercadosUiState` and `MercadosViewModel` now thread `currentUserPhotoUrl` from `SessionManager`. `MercadosScreen` passes it to `ProfileAvatar` in the top-bar action slot.

### 🧾 DetallePedidoScreen — overflow menu, button color, payment validation

Three UI fixes applied to `DetallePedidoScreen`:

1. **Three-dot overflow menu** — `PedidoOverflowMenu` composable added (matches `ClientesFilterMenu` design: `DropdownMenu` with `elevated` container, `border2`, `RoundedCornerShape(16.dp)`). Two actions:
   - *Modificar fecha* → Material3 `DatePickerDialog` pre-filled with the pedido's current `createdAt`.
   - *Eliminar pedido* → `AlertDialog` confirmation → calls `pedidoRepository.delete()` and pops back.
   - Supporting: `PedidoDao.updateDate()` query, `PedidoRepository.updateDate()`, `PedidoRepositoryImpl` impl, `showDeleteConfirm` + `showDatePicker` flags in `DetallePedidoUiState`, and corresponding ViewModel handlers.

2. **"Marcar pagado" button color** — changed from `MaterialTheme.extendedColors.greenText` to `MaterialTheme.colorScheme.primary` to match the standard button color pattern.

3. **Partial payment validation** — `PagoParcialSheetContent` now has the same guards as `PagoSheet`: `showError` flag, `LaunchedEffect(amountText)` reset, `isAmountEmpty`/`isAmountTooHigh`/`canConfirm` checks, `isError` on the text field, error text below it, and alpha-dimmed button. Reuses existing `pedidos_pago_parcial_error_vacio` and `pedidos_pago_parcial_error_maximo` strings.

### 📅 Date picker off-by-one when modifying pedido date

`DatePicker` returns UTC midnight for the selected date. `SimpleDateFormat` renders it in the device's local timezone, showing the previous day for UTC-negative zones. Fix: `selectedDateMillis - TimeZone.getDefault().getOffset(selectedDateMillis)` converts UTC midnight to local midnight before saving.

### 💰 Partial payment showing pedido creation date instead of payment date

`onRegistrarPago` only set `paidAt = System.currentTimeMillis()` when the pedido became fully `PAID`; for `PARTIAL` it passed `null`, causing `PagosSection` to fall back to `createdAt`. Fixed: `paidAt` is now always set to `System.currentTimeMillis()` regardless of resulting status.

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

## Phase 2h — implemented

- **Splash screen** — `androidx.core:core-splashscreen 1.0.1`; `ic_splash.xml` (`<layer-list>` + `<bitmap android:src="@drawable/img_logo">`, 30dp insets); `Theme.EcomerceCarlosV.Splash` in `themes.xml`; `installSplashScreen()` called before `enableEdgeToEdge()` in `MainActivity`
- **App icon** — Image Asset Studio with `img_logo.png` as foreground; adaptive icon XMLs in `mipmap-anydpi-v26/`; background color `#FFFFFF` in `values/ic_launcher_background.xml`
- **App name** — renamed from "Pedidos & Cuentas" to "CarlosVCommerce" in `strings.xml`
- **BrandMark** — replaced placeholder gradient box with `Image(painterResource(R.drawable.img_logo))` (80dp default, 64dp compact)
- **Biometric screen redesign** — removed "Usar contraseña" sub-state; enrolled-user screen now always shows password field + "Iniciar sesión" + "Entrar con huella" row; `showPasswordLogin` removed from `LoginFormState`; `onBiometricPasswordLogin()` added to `LoginViewModel`
- **LoginScreen split** — `LoginScreen.kt` (thin router) · `LoginContent.kt` (regular state) · `LoginBiometricoContent.kt` (enrolled-user state) · `LoginComponents.kt` (shared: `BrandMark`, `LoginTextField`, `PrimaryLoginButton`, `DividerOr`)
- **PerfilScreen split** — `BiometricCard` + `BiometricToggle` extracted to `BiometricCard.kt`
- **UmbralesScreen** — `UmbralesScreen.kt` wired to `UmbralesRoute` in `AppNavigation`; `PerfilScreen` Ajustes section navigates to it

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
| SplashScreen | 1.0.1 |
