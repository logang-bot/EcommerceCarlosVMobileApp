# Feature: Pedidos

## Status: 🔲 Pending (Phase 4–5)

---

## Spec summary

Two sub-features:
1. **Creación de Pedido** — "add to cart" flow: product grid, inline quantity controls, cart panel, price-override warning, confirm + payment sheet.
2. **Detalle de Pedido** — view line items, payment history, mark as paid or register partial payment.

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Creación de Pedido | `CreacionPedidoRoute(clienteId)` | `ui/screen/pedido/CreacionPedidoScreen.kt` — TODO |
| Detalle de Pedido | `DetallePedidoRoute(pedidoId)` | `ui/screen/pedido/DetallePedidoScreen.kt` — TODO |

---

## UI notes — Creación de Pedido (most complex screen)

**Two persistent regions:**

**Top — Product grid (3 columns, scrollable):**
- First cell always: "Buscar producto" (search icon)
  - Tap → search bar slides in below app bar, grid filters live
  - Cart panel stays visible during search
  - Back/clear → dismisses search, restores full grid
- Product cells: photo/glyph + name + price
- In-cart state: highlighted border + quantity badge `×N` + inline − / + controls
- Quantity → 0 removes from cart

**Bottom — Cart panel (always visible, not scrolled away):**
- Compact rows: name + qty + unit price + subtotal
- Running total
- Remove (×) per row
- Tap row → expands / bottom sheet with:
  - Quantity stepper (large tap targets)
  - Unit price field (editable, pre-filled from catalogue)
  - **Amber disclaimer banner** if price modified: "El precio ha sido modificado — El precio estándar es Bs. X.XX. Estás registrando Bs. Y.YY."
  - Notes field (optional, multiline)
- Empty cart: "Agrega productos al pedido" placeholder
- "Confirmar Pedido" CTA — disabled when cart empty

**On "Confirmar Pedido":**
Bottom sheet with three options:
- "Marcar como pagado"
- "Dejar pendiente"
- "Pago parcial" → reveals amount input field

---

## UI notes — Detalle de Pedido

- Date, line items (name / qty / unit price / subtotal)
- Amber indicator + original catalogue price if price was overridden
- Notes per line item (muted text below item)
- Total + payment status chip
- Payments section (if partial): list of payments made + "Registrar pago parcial" button
- "Marcar como pagado" button (if not fully paid)

---

## Data models needed

- `Pedido` domain model: `id, clienteId, mercadoId, date, status(paid/pending/partial), total, lines[]`
- `LineaPedido`: `productId?, productName, qty, unitPrice, cataloguePrice, notes`
- `Pago`: `id, pedidoId, amount, date`
- `PedidoRepository`, `PagoRepository`

---

## Open TODOs

- [ ] Define all entities, DAOs, DTOs, mappers
- [ ] Implement `CreacionPedidoScreen` — grid + cart split layout
- [ ] Implement search-in-grid (no navigation, inline filter)
- [ ] Implement line item bottom sheet with price-change disclaimer
- [ ] Implement confirm + payment-type bottom sheet
- [ ] Implement `DetallePedidoScreen` with partial payment history
- [ ] `RecordPedidoUseCase` — creates pedido, updates client balance
