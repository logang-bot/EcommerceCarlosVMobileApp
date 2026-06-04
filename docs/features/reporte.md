# Feature: Búsqueda Global & Reporte Diario

## Status: 🔲 Pending (Phase 8)

---

## Búsqueda Global

### Spec summary
Search all clients by name or phone number across all Mercados. Results show name, Mercado, and current status. Tap any result to navigate to that client's Detalle de Cliente.

### Screen

| Screen | Route | File |
|--------|-------|------|
| Búsqueda Global | `BusquedaRoute` | `ui/screen/busqueda/BusquedaScreen.kt` — TODO |

### UI notes
- Accessible from app bar (search icon) on Mercados home
- Full-screen search: back button + search bar (auto-focused)
- Live results as user types — no separate "search" button
- Each result row: avatar + name + Mercado name + status badge + balance
- Empty state: "Sin resultados" when query returns nothing

### Open TODOs
- [ ] Implement `BusquedaScreen` and `BusquedaViewModel`
- [ ] `SearchClientesUseCase(query)` — queries by name OR phone (Room `LIKE`)
- [ ] Wire search icon in `MercadosScreen` app bar

---

## Reporte Diario

### Spec summary
Simple daily summary: total collected today, total pedidos created today, total still pending. A list of today's movements (payments received + new pedidos).

### Screen

| Screen | Route | File |
|--------|-------|------|
| Reporte Diario | `ReporteDiarioRoute` | `ui/screen/reporte/ReporteDiarioScreen.kt` — TODO |

### UI notes
- Date header (e.g., "Martes, 3 de junio de 2026")
- Hero card: "Cobrado hoy" — large amount with green gradient background
- Two stat cards: pedidos creados + pendiente del día
- Movements list: green dot = payment received, accent dot = new pedido
- Export button in app bar (PDF / image — see Exportar Cuenta below)

### Exportar Cuenta
From Detalle de Cliente: export full pedidos list as a clean PDF or image (printable receipt format). Library TBD — `PdfDocument` (Android built-in) or a third-party option.

### Open TODOs
- [ ] Implement `ReporteDiarioScreen` and `ReporteDiarioViewModel`
- [ ] `GetReporteDiarioUseCase(date)` — aggregates today's payments + pedidos
- [ ] Implement export-to-PDF from `DetalleClienteScreen`
- [ ] Wire Reporte Diario entry point (app bar action or bottom sheet from home)
