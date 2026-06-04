# Feature: Lista Negra

## Status: 🔲 Pending (Phase 7)

---

## Spec summary

Aggregated view of all blacklisted clients. Accessible globally (from Mercados home) or filtered by Mercado. Adding a client to the blacklist creates a special "Saldo Extra" pedido with the owed amount and a required reason description.

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Lista Negra (global) | `ListaNegraRoute` | `ui/screen/listanegra/ListaNegraScreen.kt` — TODO |
| Lista Negra (by mercado) | `ListaNegraRoute(mercadoId?)` | same screen, filtered |
| Agregar a Lista Negra | `AgregarListaNegraRoute(clienteId)` | `ui/screen/listanegra/AgregarListaNegraScreen.kt` — TODO |

---

## UI notes

**Lista Negra:**
- Title: "Lista Negra" or "Lista Negra — [Mercado name]" if filtered
- Each row: photo + name + mercado + reason + balance + date added
- Tap → navigates to Detalle de Cliente

**Agregar a Lista Negra:**
1. Summary of all pending pedidos for reference
2. Total owed — two options:
   - "Calcular automáticamente" (sum of all pending)
   - "Ingresar manualmente" (free numeric input)
3. Description field (required — reason for blacklisting)
4. CTA: "Confirmar y agregar a Lista Negra"

**On confirm:**
- Creates a `SaldoExtra` pedido with the entered amount + description
- Sets `cliente.isBlacklisted = true`

---

## Open TODOs

- [ ] Add `isBlacklisted`, `blacklistReason`, `blacklistDate` to `ClienteEntity`
- [ ] Implement `ListaNegraScreen` and `ListaNegraViewModel`
- [ ] Implement `AgregarListaNegraScreen` — show pending pedidos summary, amount options, reason field
- [ ] `BlacklistClienteUseCase` — marks client, creates SaldoExtra pedido atomically
- [ ] Wire "Lista Negra" button in `MercadosScreen` and "Ver Lista Negra" in `DetalleMercadoScreen`
