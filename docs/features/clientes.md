# Feature: Clientes

## Status: 🔄 In Progress (Phase 3) — CRUD done, Saldo Extra pending

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
| Saldo Extra | `SaldoExtraRoute(clienteId)` | `ui/screen/cliente/SaldoExtraScreen.kt` | 🔲 Pending |

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
- Tappable phone chip → `Intent(ACTION_DIAL)`
- Location chip → `Intent(ACTION_VIEW, Uri.parse(mapsUrl))` — no lat/lng stored
- BalanceBlock: `Brush.linearGradient` tinted by status, monospace balance amount
- Status badge (Al día / Advertencia / Crítico)
- "Agregar a Lista Negra" button (shown only if not blacklisted) — Phase 7
- "Agregar saldo extra" button — Phase 3 (Saldo Extra screen)

**Pedidos list** (Phase 4): empty state shown until pedidos implemented.

**Search**: `OutlinedTextField` bar revealed via `AnimatedVisibility` when the search icon is tapped. `searchQuery` lives in `ClientesViewModel`; filtering is applied inside the `combine` block before the list reaches the UI.

**Filter menu**:
- A → Z (default), Críticos primero, Mayor saldo, Solo con deuda, Restablecer
- Implemented with Material3 `DropdownMenu` (Popup-backed) so it renders outside the layout hierarchy and is never clipped by the top bar's actions slot

---

## Implementation notes

- `phones` stored as pipe-separated string in Room (`"0414-123|0424-456"`); mapped to `List<String>` in domain.
- Location is **only** a URL (`mapsUrl`) — no lat/lng. User pastes the link; tapping opens the device map app.
- Row color uses the **Fuerte** variant from the design: `bgAlpha = if (isDark) 0.30f else 0.22f`, bar 6dp, balance colored by status.
- Phase 3 balance/status defaults: all clients default to `AL_DIA` / `0.0` until Phase 4 wires pedidos.
- `ClienteAvatar` accepts an optional `photoUrl`; when set it renders the photo (via `PhotoThumbnail`) while preserving the status ring. Falls back to initials with deterministic `hsl(nameHash % 360, 32%, 26%)` bg.
- `CirclePhotoPicker` (in `CreateClienteComponents.kt`): 96dp circle picker used in create/edit forms. Create mode shows a `Person` icon placeholder; edit mode shows the existing `ClienteAvatar` when no new photo is picked; both modes use `BitmapFactory`/`LaunchedEffect` to render the actual photo once selected. Camera button (32dp) always present at bottom-right.
- `CreateClienteViewModel`: `init` block restores `photoUri` from `c.photoUrl` when editing; `onSave` includes `photoUrl = s.photoUri?.toString()` when saving the `Cliente`.
- Gallery picks are copied to `cacheDir/images/` via `copyImageToCache()` on selection (same as other photo flows).
- `formatBalance` is `internal fun` defined in `ClientesScreen.kt`, shared with `DetalleClienteScreen.kt` via module scope.

---

## Open TODOs

- [ ] Implement `SaldoExtraScreen` (pre-filled category "Saldo", description, amount, date)
- [ ] Add `SaldoExtraRoute` to `AppRoutes.kt` and `AppNavigation.kt`
- [ ] Phase 4: compute real balance and status from pedidos in `ClientesViewModel` and `DetalleClienteViewModel`
- [ ] Phase 7: wire "Agregar a Lista Negra" action in `DetalleClienteScreen`
