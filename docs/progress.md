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
| 8 | Reportes tab (Diario + Por cliente modes, PDF export, Reporte de Pedidos) | ✅ Done |
| 9 | Supabase auth + sync layer, DataStore session persistence | 🔄 In Progress |

---

## ✅ Phase 9 — Auth + Supabase wiring (completed)

### 🔐 Supabase Authentication — real auth implemented
`LoginViewModel` now calls `supabase.auth.signInWith(Email)`. Three error paths:
- **Invalid credentials** → inline red error banner above fields (bold title + body text); both fields show red border + error hint below password.
- **Account disabled/banned** → blocking card replaces form (ban icon, "Tu cuenta está desactivada", "Contactar al administrador" + "Entrar con otra cuenta" buttons). Detected via `"banned"` in `RestException.message`.
- **No connection** → generic error message in the banner.

### 💾 Session persistence — DataStore-backed
`SessionManagerImpl` persists the `userId` in DataStore and the JWT in `DataStoreGoTrueSessionManager`.
On app restart, supabase-kt auto-loads the JWT from DataStore; `SessionManagerImpl` listens to
`auth.sessionStatus` and restores `currentUser` from Room (or fetches from Supabase on first device).
- `SessionStatus.LoadingFromStorage` → renamed `Initializing` in supabase-kt 3.1.4
- `SessionStatus.NetworkError` → renamed `RefreshFailure` in supabase-kt 3.1.4

### 🔗 Create user — wired to Supabase admin API
`CrearUsuarioViewModel` calls `adminClient.auth.admin.createUserWithEmail(...)` (renamed in supabase-kt 3.1.4), inserts a row in the `users` table, and upserts into local Room. Requires `STAGING/PRODUCTION_SECRET_KEY` in `local.properties`.

Admin operations use `banDuration`:
- Deactivate user → `banDuration = "876000h"` (~100 years; Supabase has no permanent-ban boolean)
- Reactivate user → `banDuration = "none"`

### 👥 Three-role schema (client requirement)
| Role | Access |
|------|--------|
| SUPERUSUARIO | Full CRUD on all tables, manage users and roles |
| USUARIO | Full CRUD on business tables (mercados/clientes/productos/pedidos); read/edit own profile only |
| INVITADO | Read-only on all tables; read own profile only |

**App changes:** `UserRole.INVITADO` enum added. `RoleBadge` updated (accent-tint/primary for USUARIO, blue-tint/blueText/eye icon for INVITADO). Role picker shows 3 cards; only the selected one expands its permissions list. `GestionUsuariosScreen` now groups users into 3 sections (Super usuarios / Usuarios / Invitados). Activate/Deactivate button toggles based on `user.isActive`.

### 🔏 Role-gated UI (canWrite pattern)

All business screens enforce the three-tier role matrix at the UI level via a `canWrite: Boolean` field in each UiState (computed as `user?.role != UserRole.INVITADO`).

**INVITADO users see all data but cannot:**
- Create mercados (FAB hidden in MercadosScreen)
- Edit or delete mercados (edit icon + danger zone hidden in DetalleMercadoScreen)
- Create clients (FAB + empty-state action hidden in ClientesScreen)
- Edit clients, create pedidos, blacklist/unblacklist, or add saldo extra (all hidden in DetalleClienteScreen)
- Edit pedidos or record payments (edit icon + bottom bar hidden in DetallePedidoScreen)
- Create or edit products; product rows are non-clickable with no chevron (CatalogoScreen)

User management (`GestionUsuariosScreen`) is gated separately in `PerfilScreen` by checking `state.role == UserRole.SUPERUSUARIO` — unchanged from Phase 9 auth wiring.

**Files changed (role-gating):**
`ui/screen/mercado/MercadosUiState.kt`, `MercadosViewModel.kt`, `MercadosScreen.kt`,
`ui/screen/mercado/DetalleMercadoUiState.kt`, `DetalleMercadoViewModel.kt`, `DetalleMercadoScreen.kt`,
`ui/screen/cliente/ClientesUiState.kt`, `ClientesViewModel.kt`, `ClientesScreen.kt`,
`ui/screen/cliente/DetalleClienteUiState.kt`, `DetalleClienteViewModel.kt`, `DetalleClienteScreen.kt`, `DetalleClienteActions.kt`,
`ui/screen/pedido/DetallePedidoUiState.kt`, `DetallePedidoViewModel.kt`, `DetallePedidoScreen.kt`,
`ui/screen/producto/CatalogoUiState.kt`, `CatalogoViewModel.kt`, `CatalogoScreen.kt`

See `docs/features/usuarios.md → canWrite pattern` for the full element table.

### 🌍 Environments
Two product flavors: **staging** and **production**. Each reads its own Supabase URL + keys from
`local.properties`. Build variants: `stagingDebug`, `stagingRelease`, `productionDebug`, `productionRelease`.
Staging fully wired and tested. Production keys TBD.

### 📋 SQL docs
`docs/sql/schema.sql` — all CREATE TABLE statements with inline `ENABLE ROW LEVEL SECURITY`.
`docs/sql/rls.sql` — 3-tier RLS policies (SUPERUSUARIO / USUARIO / INVITADO).
`docs/sql/storage.sql` — Storage bucket creation + policies.

**Files changed (Phase 9 + this session):**
`gradle/libs.versions.toml`, `app/build.gradle.kts`, `local.properties`,
`di/AppQualifiers.kt`, `di/DataStoreModule.kt`, `di/SupabaseModule.kt`,
`data/session/DataStoreGoTrueSessionManager.kt`, `data/session/SessionManagerImpl.kt`,
`domain/session/SessionManager.kt`, `domain/model/UserRole.kt`,
`domain/repository/UserRepository.kt`, `data/remote/dto/UserDto.kt`, `data/mapper/UserMapper.kt`,
`data/repository/impl/UserRepositoryImpl.kt`,
`presentation/navigation/AppNavigation.kt`, `presentation/navigation/AppViewModel.kt`,
`MainActivity.kt`,
`ui/screen/auth/LoginUiState.kt`, `ui/screen/auth/LoginViewModel.kt`,
`ui/screen/auth/LoginScreen.kt`, `ui/screen/auth/LoginContent.kt`, `ui/screen/auth/LoginComponents.kt`,
`ui/screen/usuario/CrearUsuarioViewModel.kt`, `ui/screen/usuario/UsuarioDetalleViewModel.kt`,
`ui/screen/usuario/GestionUsuariosViewModel.kt`, `ui/screen/usuario/GestionUsuariosUiState.kt`,
`ui/screen/usuario/GestionUsuariosScreen.kt`, `ui/screen/usuario/UsuarioDetalleScreen.kt`,
`ui/screen/usuario/CrearUsuarioScreen.kt`, `ui/screen/usuario/UserUiModel.kt`,
`ui/screen/perfil/PerfilViewModel.kt`, `ui/screen/perfil/EditarPerfilViewModel.kt`,
`ui/common/RoleBadge.kt`,
`docs/sql/schema.sql`, `docs/sql/rls.sql`, `docs/supabase-setup.md`,
`docs/features/auth.md`, `docs/progress.md`

---

## ✅ Post-Phase 8 improvements

### 📱 Primary phone for clients (DB v13)

Users can now mark one phone number as **primary** in the Create/Edit Cliente form. The primary phone is the one displayed in `DetalleClienteScreen`.

**PhoneListField redesign** (`CreateClienteComponents.kt`):
- Each `PhoneRow` is a custom card (52dp min height, 14dp corners, `surface2` bg). Left to right: radio circle (24dp, filled `primary`+Check when primary, outlined `text3` when not), phone icon, `BasicTextField` (15.5sp Monospace), "PRINCIPAL" badge (primary text, `accentTint` bg, only on primary row), call button (edit mode + non-empty only), delete button (dimmed when only one phone).
- Primary row has a 1.5dp `primary` border; non-primary has 1dp `border2`.
- Info hint below the label: "El teléfono **principal** es el que aparece en el detalle del cliente."
- "Agregar otro teléfono" is a bordered pill row.

**Tap-to-call:**
- `DetalleClienteScreen` — primary phone chip launches `Intent(ACTION_DIAL)` (already existed; now uses primary phone instead of `firstOrNull()`).
- `EditarClienteScreen` — each phone row shows a `Call` icon button that dials that specific number.

**DB:** `MIGRATION_12_13` adds `primaryPhoneIndex INTEGER NOT NULL DEFAULT 0` to `clientes`. Room bumped to **v13**.

**Files changed:** `Cliente.kt`, `ClienteEntity.kt`, `ClienteMapper.kt`, `AppDatabase.kt`, `DatabaseModule.kt`, `CreateClienteFormState.kt`, `CreateClienteViewModel.kt`, `CreateClienteComponents.kt`, `CreateClienteScreen.kt`, `DetalleClienteHeader.kt`, `docs/db-schema.md`, `docs/features/clientes.md`.

---

### 📥 Reports saved to Downloads folder

Report export no longer opens the print dialog. Files are now written directly to the device's **Downloads** folder and a `Toast` confirms the result.

**`ReporteSaver.kt`** (`ui/screen/reporte/`):
- `saveReportToDownloads(context, html, fileName): SaveResult` — coroutine (Dispatchers.IO).
  - **API 29+**: `MediaStore.Downloads` — no storage permission required.
  - **API 24–28**: `Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)` + `File.writeText`.
- `SaveResult`: `Success(fileName)` / `NoSpace` / `Error(cause)`.
- `showSaveToast(context, result)` — shows `Toast.LENGTH_LONG` in Spanish.
- Error detection: IOException message checked for `ENOSPC`/`No space left` → `NoSpace`; all others → `Error`.

**Filenames:** `Reporte_Diario_YYYYMMDD_HHmm.html`, `Reporte_PorCliente_YYYYMMDD_HHmm.html`, `Reporte_{ClientName}_YYYYMMDD_HHmm.html`.

**Files changed:** `ReporteScreen.kt`, `ReporteClienteScreen.kt`, `ReporteSaver.kt` (new), `strings.xml` (3 new toast strings).

---

### 🔄 Report generation status screen (`ReporteStatusScreen`)

Tapping the export action now navigates to a dedicated status screen instead of saving directly.

**Generating state:**
- Shimmer skeleton document thumbnail (150×197dp, infinite `Brush.linearGradient` animation).
- "Creando tu reporte…" title + item-count description (e.g. "Reuniendo 23 pedidos").
- `LinearProgressIndicator` animated in three steps (0→12%→62%→95% over ~950ms total).
- Three `GenStep` rows: step 0 "Pedidos/Movimientos reunidos" → step 1 "Generando el documento" → step 2 "Listo para descargar". Each step has a circle badge (active: spinning sync icon on `accentTint`; done: check on `greenTint`; todo: dot on `surface3`).
- AppBar: "Generando reporte" + "Cancelar" text button.

**Ready state:**
- Green check badge (56dp, 18dp corners).
- File meta card: red-tint doc icon + filename + "HTML · N KB".
- AppBar: "Reporte listo".
- Bottom bar:
  - **Compartir** → `Intent.ACTION_SEND` with cached file via `FileProvider` (Android native share sheet).
  - **Descargar** → `saveReportToDownloads` → toast. Turns green ("Descargado" + Check) after first tap.

**`ReporteExportHolder`** (singleton): calling screen sets `pending = PendingExport(html, fileName, itemCount, isMovimientosVariant)` before navigating to `ReporteStatusRoute`.

**`file_paths.xml`**: added `<cache-path name="reports" path="reports/" />` for `FileProvider`.

**Files changed:** `ReporteStatusScreen.kt` (new), `ReporteExportHolder.kt` (new), `AppRoutes.kt` (+ `ReporteStatusRoute`), `AppNavigation.kt` (+ composable), `HomeScreen.kt` (pass navController to ReporteScreen), `ReporteScreen.kt` (navigate instead of direct save), `ReporteClienteScreen.kt` (navigate instead of direct save), `file_paths.xml`.

---

### 🎨 Reportes UI polish (same session as Phase 8)

- **Mode toggle**: selected tab now uses `primary` bg + `onPrimary` text (was `surface`/`onSurface`); unselected uses `text2` (was `text3`).
- **Chip rows**: `DiarioDateChips`, `ClienteDateChips`, `PresetChipsRow` changed from `Row + horizontalScroll` → `FlowRow` so chips wrap instead of being cut off.
- **Resolved date bar** (`ReporteResolvedDateBar`): shown below chip rows for non-Personalizado presets. 42dp pill, `surface2`+`border`, calendar icon + formatted date text. `formatDiarioBarText` / `formatClienteBarText` helpers in `ReporteDateChips.kt`.
- **`ReporteStatCard` redesign**: added 34×34dp icon container (10dp corners, tinted bg + matching icon) above the value; value bumped to 22sp SemiBold Monospace; removed `bgColor` param, added `icon` + `iconBgColor`.

---

## ✅ Phase 8 — Reportes tab

Full **Reportes** screen (tab 3 of bottom nav) with two modes and PDF export:

**Diario mode:** segmented toggle → date chip bar (Hoy/Ayer/Semana/Personalizado) → "Cobrado hoy" hero card (green gradient, 38sp mono amount) → two stat cards (Pedidos creados / Pendiente del día) → Movimientos list (8dp dot, name, type·mercado·time, amount).

**Por cliente mode:** date chip bar (Este mes/Trimestre/Año/Personalizado) → client selector card with "Cambiar" → `ClienteSelectorSheet` (ModalBottomSheet, alphabetical client list) → three stat cards (Facturado/Pagado/Saldo) → Historial list (36dp icon tile, title, status subtext, amount).

**"Personalizado":** both modes show two `DateField` composables (Desde/Hasta) that open Material3 `DatePickerDialog`.

**Report export:** top-bar `Description` icon (Reportes tab) and "Generar PDF" button (`ReporteClienteScreen`) → saves `.html` file to **Downloads** via `ReporteSaver.kt` (MediaStore on API 29+, File API on 24–28). Toast on success/error. *(Originally WebView+PrintManager; replaced post-Phase 8 — see "Post-Phase 8 improvements" above.)*

**Data layer:** `PedidoRepository.getAll(): Flow<List<Pedido>>` added (no DB migration — reads existing `pedidos` table).

**Home logo + Búsqueda Global (also Phase 8):**
- `LogoMark()` composable added as `leading` in `MercadosScreen` top bar (34dp circle, white bg, border2, `img_logo.png`).
- `BusquedaScreen` now has three sections: Clientes / Lista Negra / Mercados. `BlacklistResultRow` shows a 18dp red ban badge overlay on the avatar + "En Lista Negra" red pill chip.

**Reporte de Pedidos (also Phase 8):** full PDF-preview screen accessible from the "Generar reporte" menu in `DetalleClienteScreen`. Shows client info, resume summary cards, and full pedidos list. Exports via the same `WebView + PrintManager` pattern.

See `docs/features/reporte.md`, `docs/features/busqueda.md`, `docs/features/clientes.md`.

---

## ✅ Resolved post-Phase 7

### ⚖️ Client status decoupled from saldo-extra balance

`computeStatus()` now uses a separate `statusBalance` (only `PARTIAL && !isSaldoExtra` pedidos) instead of the full display balance. The displayed "Saldo pendiente total" still includes saldo-extra entries, but the red/amber/green badge and row gradient are driven exclusively by real unpaid orders.

**Before:** a client with only saldo-extra debt would show as ADVERTENCIA/CRITICO.  
**After:** that client shows as AL_DIA in color/badge; the extra amount is still visible in the balance block.

Applied identically in both `DetalleClienteViewModel` and `ClientesViewModel`. See **`docs/features/clientes.md → Client Status Thresholds`** for the full rule.

---

### 📦 Expandable `PedidoRow` with product lines

Regular pedido rows now display a **chevron button** (42×42dp, 12dp radius) in place of the left icon tile. Tapping the chevron expands an inline product-list panel below the row with a 180° animated caret rotation. Saldo-extra rows are unchanged (amber Tag tile, amber bg tint).

**Domain:** `PedidoLineItem(productName, quantity)` added. `Pedido.lines: List<PedidoLineItem>` field added (default empty).

**Data layer:** `PedidoWithLines` Room POJO (`@Embedded PedidoEntity` + `@Relation List<DetallePedidoEntity>`). `PedidoDao.getByClienteWithLines()` (`@Transaction` query). `PedidoMapper.toDomain(PedidoWithLines)` overload maps `product_name` + `quantity` into `PedidoLineItem` list. `PedidoRepository.getByClienteWithLines()` added; `PedidoRepositoryImpl` implements it.

**ViewModel:** `DetalleClienteViewModel` now uses `getByClienteWithLines` instead of `getByCliente`, so every emission carries line items.

**No DB migration required** — `product_name` was already present in `detalle_pedido` since Room v9 (Phase 4). `CreatePedidoUseCase` was already saving it from `CartItem.productName`.

**`PedidoRow.kt` redesign:** `CaretButton` composable (accentSoft bg + primary border when expanded, surface3 + border when collapsed; `animateFloatAsState` 180° rotation). `PedidoLinesPanel` composable (surface bg card, border inset, 10.5sp uppercase "N PRODUCTOS" header, `HorizontalDivider`-separated rows with product name + mono `×qty`). `AnimatedVisibility` with `expandVertically + fadeIn/Out(tween 180ms)` controls panel visibility. Panel left-padding aligns under row content (75dp = 20dp horizontal + 42dp tile + 13dp gap).

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

### 🏠 Home logo + Búsqueda Global — Lista Negra section

**Home screen (MercadosScreen):**
- `PedidosTopBar` gained a `leading: @Composable (() -> Unit)?` parameter (placed in the actions row left of the `Spacer`, only when `onBack == null`).
- `LogoMark()` composable added to `MercadosScreen.kt`: 34dp circle, white bg, 1dp `border2` inset, `img_logo.png` `ContentScale.Crop`.
- `MercadosScreen` passes `leading = { LogoMark() }` to show the Carlos V logo at the top-left of the home screen.

**Búsqueda Global — new "Lista Negra" section:**
- `BlacklistSearchResult(clienteId, name, photoUrl, mercadoName, balance)` added to `BusquedaUiState.kt`.
- `BusquedaUiState.blacklistResults` field added; `hasResults` updated to include it.
- `BusquedaViewModel` splits `clienteRepository.getAll()` into active (`!isBlacklisted`) → `clienteResults` and blacklisted (`isBlacklisted`) → `blacklistResults`. Balance comes from `cliente.blacklistBalance`.
- `BlacklistResultRow` composable: avatar with 18dp red ban badge (redText bg, white icon, 2dp bg-color ring) + name + mercado + "En Lista Negra" red pill chip + balance.
- Section inserted between Clientes and Mercados in the `LazyColumn`, with `Block` icon and `redText` label color.
- `SearchGroupLabel` accepts optional `labelColor: Color?` for the red variant.

---

### 📄 Reporte de Pedidos — PDF export from Detalle Cliente

Full "Reporte de pedidos" screen accessible from the "Generar reporte" menu item in `PedidosMenuButton`.

**Navigation**: `ReporteClienteRoute(clienteId)` added to `AppRoutes.kt` and wired in `AppNavigation.kt`. `PedidosMenuButton` gains `onGenerarReporte: () -> Unit` parameter. `DetalleClienteScreen` passes `onGenerarReporte = { state.cliente?.let { navController.navigate(ReporteClienteRoute(it.id)) } }`.

**Screen** (`ReporteClienteScreen.kt`, `ReporteClienteViewModel.kt`, `ReporteClienteUiState.kt`):
- Top bar: "Reporte de pedidos" + client name subtitle + back arrow.
- Four scrollable sections: **Encabezado** (accentSoft card, company name + date), **Cliente** (2×2 info grid), **Resumen** (3 stat cards — "Sin pagar" blue, "Saldo pendiente" amber, "Total pedidos" green), **Pedidos** (list of `ReportePedidoRow` composables with date, product names summary, amounts, `PayChip`).
- Bottom export bar: full-width "Exportar PDF" `Button`.

**HTML generation** (zero new dependencies):
1. `buildReporteHtml(state, date)` / `buildReporteClienteHtml(state, period, date)` generate self-contained HTML strings with inline CSS (tables, color chips, status badges).
2. On button tap: `saveReportToDownloads(context, html, fileName)` writes to the **Downloads** folder + `showSaveToast` confirms. *(See "Post-Phase 8 improvements" for the full saver implementation.)*

**Menu item redesign**: `ReporteMenuItem` now has `accentSoft` bg, `Description` icon in `accentTint` tile, full-contrast label, and primary-tinted chevron. Previously non-interactive.

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
