# Feature: Clientes

## Status: ✅ Done — CRUD, Saldo Extra, Lista Negra, Detalle UI complete

---

## Spec summary

Each Mercado contains a list of Clientes. The client row is fully colored by status (red/amber/green wash + left accent bar). Tapping a client opens their detail: balance header, pedidos list, and action buttons including "Agregar a Lista Negra" and "Agregar saldo extra".

---

## Screens

| Screen | Route | File | Status |
|--------|-------|------|--------|
| Lista de Clientes | `ClientesRoute(mercadoId)` | `ui/screen/cliente/ClientesScreen.kt` | ✅ Done |
| Detalle de Cliente | `DetalleClienteRoute(clienteId)` | `ui/screen/cliente/DetalleClienteScreen.kt` | ✅ Done |
| Crear / Editar Cliente | `CreateClienteRoute(mercadoId, clienteId?)` | `ui/screen/cliente/CreateClienteScreen.kt` | ✅ Done |
| Saldo Extra | `SaldoExtraRoute(clienteId)` | `ui/screen/cliente/SaldoExtraScreen.kt` | ✅ Done |

---

## Data layer

| File | Status |
|------|--------|
| `domain/model/Cliente.kt` | ✅ |
| `domain/model/ClientStatus.kt` (enum: AL_DIA, ADVERTENCIA, CRITICO) | ✅ |
| `domain/repository/ClienteRepository.kt` | ✅ |
| `data/local/entity/ClienteEntity.kt` | ✅ |
| `data/local/dao/ClienteDao.kt` | ✅ |
| `data/remote/dto/ClienteDto.kt` | ✅ |
| `data/mapper/ClienteMapper.kt` | ✅ |
| `data/repository/impl/ClienteRepositoryImpl.kt` | ✅ |
| Room migration 5→6 (`MIGRATION_5_6`) | ✅ |
| Room migration 7→8 (`MIGRATION_7_8`) — adds `blacklistBalance` column | ✅ |

---

## UI notes (from mockup)

**Client row (inside Mercado) — Fuerte style**:
- Full-row background wash: status tint at 30% alpha (dark) / 22% alpha (light) via `isSystemInDarkTheme()`
- Left accent bar: 6dp wide, solid status color (`redTint α=1`, `amber`, `green`)
- Balance text is colored by status (redText / amberText) when non-zero; `text3` when zero
- Circular avatar (initials, hue-deterministic background via name hash)
- Name + description + status badge + total balance

**Detalle de Cliente header**:
- 76dp circular avatar, name, description
- TopBar action: edit (pencil) icon → navigates to `CreateClienteRoute(mercadoId, clienteId)` for editing
- Tappable phone chip → `Intent(ACTION_DIAL)`
- Location chip → `Intent(ACTION_VIEW, Uri.parse(mapsUrl))` — no lat/lng stored
- BalanceBlock: `Brush.linearGradient` tinted by status, monospace balance amount, label "Saldo pendiente total"
- Status badge (Al día / Advertencia / Crítico)
- "Agregar a Lista Negra" button (red-tint, shown only if not blacklisted) → navigates to `AgregarListaNegraRoute`
- "Quitar de Lista Negra" button (surface2, green check icon, shown only if blacklisted) → calls `DetalleClienteViewModel.unblacklist()`
- "Agregar saldo extra" button — disabled (alpha 0.5, non-clickable) when blacklisted
- FAB "Nuevo Pedido" is hidden when client is blacklisted

**Detalle de Cliente — En Lista Negra state**:
- Banner at top of scrollable content (red-tint bg, ban icon, "En Lista Negra" title + "Vetado desde el [date] · no se pueden crear pedidos nuevos." subtitle)
- Avatar gets a 28dp red circular ban-badge overlay at bottom-right (3dp background-color border ring)
- "Agregar a Lista Negra" button replaced by "Quitar de Lista Negra" (surface2, green CheckCircle icon)
- "Agregar saldo extra" button is `alpha(0.5f)` and non-clickable

**Pedidos list** (Phase 4 — ✅ done): `PedidoRow` composable per pedido. Shows icon tile, product count, date, total (mono), `PayChip`. Empty state shown when no pedidos exist.

**Search**: `OutlinedTextField` bar revealed via `AnimatedVisibility` when the search icon is tapped. `searchQuery` lives in `ClientesViewModel`; filtering is applied inside the `combine` block before the list reaches the UI.

**Filter menu**:
- A → Z (default), Críticos primero, Mayor saldo, Solo con deuda, Restablecer
- Implemented with Material3 `DropdownMenu` (Popup-backed) so it renders outside the layout hierarchy and is never clipped by the top bar's actions slot

---

## Implementation notes

- `phones` stored as pipe-separated string in Room (`"0414-123|0424-456"`); mapped to `List<String>` in domain.
- Location is **only** a URL (`mapsUrl`) — no lat/lng. User pastes the link; tapping opens the device map app.
- Row color uses the **Fuerte** variant from the design: `bgAlpha = if (isDark) 0.30f else 0.22f`, bar 6dp, balance colored by status.
- Balance and status are now computed live in `DetalleClienteViewModel` by `combine`-ing `clienteFlow + pedidosFlow`. Balance = sum of pending amounts across non-PAID pedidos. See **Client Status Thresholds** section below.
- `ClienteAvatar` accepts an optional `photoUrl`; when set it renders the photo (via `PhotoThumbnail`) while preserving the status ring. Falls back to initials with deterministic `hsl(nameHash % 360, 32%, 26%)` bg.
- `CirclePhotoPicker` (in `CreateClienteComponents.kt`): 96dp circle picker used in create/edit forms. Create mode shows a `Person` icon placeholder; edit mode shows the existing `ClienteAvatar` when no new photo is picked; both modes use `BitmapFactory`/`LaunchedEffect` to render the actual photo once selected. Camera button (32dp) always present at bottom-right.
- `CreateClienteViewModel`: `init` block restores `photoUri` from `c.photoUrl` when editing; `onSave` includes `photoUrl = s.photoUri?.toString()` when saving the `Cliente`.
- Gallery picks are copied to `cacheDir/images/` via `copyImageToCache()` on selection (same as other photo flows).
- `formatBalance` is `internal fun` defined in `ClientesScreen.kt`, shared with `DetalleClienteScreen.kt` via module scope.
- `ClienteRepository` exposes `getBlacklisted()`, `blacklist(id, reason, balance, at)`, and `unblacklist(id)`. All implemented in `ClienteRepositoryImpl` and delegated to `ClienteDao`.
- `ClienteDao.unblacklist` resets `isBlacklisted=0`, `blacklistReason=NULL`, `blacklistBalance=0`, `blacklistedAt=NULL`.
- `DetalleClienteViewModel` stores `clienteRepository` as a field (needed for `unblacklist()`). Exposes `fun unblacklist()` which launches a coroutine.
- `DetalleClienteViewModel` uses `clienteRepository.getByIdFlow(clienteId).stateIn(...)` instead of a one-shot `getById`. This means `DetalleClienteScreen` reacts to any DB change for that client — including blacklisting/unblacklisting — without manual refresh.
- `DetalleClienteScreen.onListaNegraClick` navigates to `AgregarListaNegraRoute(clienteId)`. After confirming blacklist, navigation pops back to `DetalleClienteScreen`, which reactively switches to the blacklisted state.
- `DetalleClienteScreen.onQuitarListaNegraClick` calls `viewModel.unblacklist()` directly (no confirmation screen — design shows a direct action button).
- `ClientesScreen` and `MercadosScreen` "Lista Negra" buttons navigate to `ListaNegraRoute`.

---

## SaldoExtra screen

Split across two files: `SaldoExtraScreen.kt` (scaffold, save bar, previews) + `SaldoExtraFields.kt` (all field composables, previews).

**AppBar**: `PedidosTopBar` — back arrow + title "Agregar saldo extra" + client name subtitle + Close (×) icon action (both back and close pop the stack).

**Content column**: `Arrangement.spacedBy(18.dp)`, `padding(horizontal = 20.dp, top = 8.dp, bottom = 24.dp)`. Fields in order:

| Field | Composable | Notes |
|-------|-----------|-------|
| Categoría | `SaldoExtraCategoryField` | Locked row: 30×30 amber-tint tile (`amberTint` bg, 9dp radius, `Tag` icon 17dp) + "Saldo" (15.5sp SemiBold) + `Check` icon + "Fijo" (11.5sp, text3); `surface` bg, `border` inset |
| Monto | `SaldoExtraAmountHero` | Centered hero: "Monto" label (13sp, text2) → `BasicTextField(wrapContentWidth)` with "Bs." (22sp, text2) in `decorationBox` + 46sp mono Bold letterSpacing −1.5sp; 2dp accent underline (180dp, centered); turns error-red when `amountError` |
| Descripción | `SaldoExtraDescriptionField` | Label "Descripción *" (required asterisk in accent); `OutlinedTextField`, `surface2` bg, `border2` inset when non-empty, `88dp` min height |
| Fecha | `SaldoExtraDateField` | Tappable `surface2` row; date formatted `"d 'de' MMMM 'de' yyyy"` (Spanish locale); opens `DatePickerDialog` |

**Bottom bar**: `SaldoExtraSaveBar` — "Registrar saldo" CTA (52dp, 15dp radius); spinner while saving; disabled until `canSave`.

Each composable gets its own label above (13sp, `FontWeight.Medium`, `text2`) via the private `SaldoExtraFieldLabel` helper; required fields get an accent ` *` suffix.

---

## Client Status Thresholds

Computed live from `clienteFlow + pedidosFlow + umbralesFlow` (three-way `combine`). Thresholds are configurable by superusers from **Mi Perfil → Ajustes → Umbrales de estado**.

**Balance** = sum of `pending` amounts for pedidos where:
- `status == PARTIAL` (partial payment made but not settled), OR
- `status == PENDING && isSaldoExtra == true` (deliberate debt records)

> Regular `PENDING` pedidos are excluded — they represent unconfirmed orders, not real debt.

**Status rules:**

| Status | Condition | Color |
|--------|-----------|-------|
| `AL_DIA` | `balance == 0` | green |
| `CRITICO` | `balance > montoMaximo` OR any balance-contributing pedido (`PARTIAL` or saldo-extra `PENDING`) has `createdAt` older than `diasMaximos` days | red |
| `ADVERTENCIA` | `balance > 0` and neither CRITICO condition applies | amber |

> The days check only runs on pedidos that already count toward the balance. A regular `PENDING` order is never flagged as old debt no matter how old it is.

**Threshold defaults**: `montoMaximo = 200.0 Bs`, `diasMaximos = 30 days`.

**"Older than N days"**: `(System.currentTimeMillis() - pedido.createdAt) > days.toLong() * 24 * 60 * 60 * 1000`

**Implementation**:
- `DetalleClienteViewModel.kt` — `computeStatus(balance, pedidos, umbrales)` + `isOlderThan(createdAt, days)`
- `ClientesViewModel.kt` — same logic applied over `getAllUnpaid()` grouped by `clienteId`
- `UmbralesManager.kt` — `@Singleton` `StateFlow<Umbrales>` backed by `SharedPreferences`; all injecting ViewModels recompute automatically on threshold change

---

## Open TODOs

- [x] Implement `SaldoExtraScreen` (pre-filled category "Saldo", description, amount, date)
- [x] Add `SaldoExtraRoute` to `AppRoutes.kt` and `AppNavigation.kt`
- [x] Phase 4: real balance/status computed from pedidos in `DetalleClienteViewModel`
- [x] Phase 7: Lista Negra state in `DetalleClienteScreen` — banner, avatar badge, button swap, FAB hidden, "Quitar de Lista Negra" action
