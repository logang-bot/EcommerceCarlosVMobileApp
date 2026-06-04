# Feature: Clientes

## Status: 🔲 Pending (Phase 3)

---

## Spec summary

Each Mercado contains a list of Clientes. The client row is fully colored by status (red/amber/green wash + left accent bar). Tapping a client opens their detail: balance header, pedidos list, and action buttons including "Agregar a Lista Negra" and "Agregar saldo extra".

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Lista de Clientes (inside Mercado) | `DetalleMercadoRoute` | part of mercados feature |
| Detalle de Cliente | `DetalleClienteRoute(clienteId)` | `ui/screen/cliente/DetalleClienteScreen.kt` — TODO |
| Crear / Editar Cliente | `CreateClienteRoute(mercadoId, clienteId?)` | `ui/screen/cliente/CreateClienteScreen.kt` — TODO |
| Saldo Extra | `SaldoExtraRoute(clienteId)` | `ui/screen/cliente/SaldoExtraScreen.kt` — TODO |

---

## UI notes (from mockup)

**Client row (inside Mercado)**:
- Full-row background wash: `redTint` / `amberTint` / `greenTint` based on status
- Left accent bar: 3dp wide, `red` / `amber` / `green`
- Circular avatar (initials, hue-deterministic background)
- Name + description + status badge + total balance

**Detalle de Cliente header**:
- Avatar, name, description, tappable phone numbers
- Location pin → opens map
- Large total balance (prominent)
- Status badge (Al día / Advertencia / Crítico)
- "Agregar a Lista Negra" button (shown only if not blacklisted)
- "Agregar saldo extra" button (below Lista Negra button)

**Pedidos list**:
- Sorted by date (newest first)
- Saldo Extra rows: amber tile, "MANUAL" tag, tinted row
- Tap to open Detalle de Pedido

**Filter menu (⋯)**:
- Orden alfabético A–Z (default)
- Críticos primero
- Mayor saldo primero
- Solo con deuda
- Restablecer (normal)

---

## Data models needed

- `Cliente` domain model: `id, mercadoId, name, description, photoUrl, phones, location, status, totalBalance, isBlacklisted`
- `ClienteRepository` interface + impl
- Room entity: `ClienteEntity`

---

## Open TODOs

- [ ] Define `ClienteEntity`, `ClienteDao`, `ClienteDto`, `ClienteMapper`
- [ ] Add routes to `AppRoutes.kt`
- [ ] Implement `DetalleClienteScreen` with balance header and action buttons
- [ ] Implement `CreateClienteScreen` with multi-phone input and map pin
- [ ] Implement `SaldoExtraScreen` (pre-filled category "Saldo", description, amount, date)
- [ ] Status computation logic: Al día / Advertencia / Crítico based on balance thresholds (define thresholds with Carlos)
