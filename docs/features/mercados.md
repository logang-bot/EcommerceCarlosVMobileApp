# Feature: Mercados

## Status: ✅ Done (Phase 2)

---

## Spec summary

Entry point after login. Shows all Mercados alphabetically. Each row shows name, active client count, and a status dot if any client inside is in Warning or Critical state. A "Lista Negra" button at the bottom links to the global blacklist. FAB creates a new Mercado.

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Lista de Mercados | `MercadosRoute` | `ui/screen/mercado/MercadosScreen.kt` — TODO |
| Detalle de Mercado | `DetalleMercadoRoute(mercadoId)` | `ui/screen/mercado/DetalleMercadoScreen.kt` — TODO |
| Crear / Editar Mercado | `CreateMercadoRoute(mercadoId?)` | `ui/screen/mercado/CreateMercadoScreen.kt` — TODO |

---

## UI notes (from mockup)

- Large app bar (`large = true`) with subtitle showing total counts
- Each row: grid icon tile + name + optional status dot + chevron
- "Lista Negra" row at the bottom — red tint icon, distinct from client rows
- FAB: extended, label "Mercado"
- Filter / sort accessible via `⋯` action button in app bar

---

## Data models needed

- `Mercado` domain model: `id, name, address, location, photoUrl, createdAt`
- `MercadoRepository` interface + `MercadoRepositoryImpl`
- Room entity: `MercadoEntity`
- Remote DTO: `MercadoDto`

---

## Files created

| File | Description |
|------|-------------|
| `domain/model/Mercado.kt` | Domain model |
| `domain/repository/MercadoRepository.kt` | Repository interface |
| `data/local/entity/MercadoEntity.kt` | Room entity |
| `data/local/dao/MercadoDao.kt` | DAO (getAll Flow, getById, insert, update, deleteById) |
| `data/local/AppDatabase.kt` | Room DB — version 1 |
| `data/mapper/MercadoMapper.kt` | Entity ↔ Domain mapper |
| `data/repository/impl/MercadoRepositoryImpl.kt` | Repository implementation |
| `di/DatabaseModule.kt` | Provides Room DB + DAOs |
| `di/RepositoryModule.kt` | Binds repository interfaces |
| `ui/common/PedidosTopBar.kt` | Shared top bar (small + large variant, optional back button) |
| `ui/common/EmptyState.kt` | Shared empty state (icon + title + subtitle + hint) |
| `ui/screen/mercado/MercadosScreen.kt` | Alphabetical list, Lista Negra button, FAB |
| `ui/screen/mercado/DetalleMercadoScreen.kt` | Stub — empty state until Phase 3 adds clients |
| `ui/screen/mercado/CreateMercadoScreen.kt` | Name + address form, save to Room |

## Open TODOs

- [ ] Add client count to `MercadoRow` once `ClienteRepository` is ready (Phase 3)
- [ ] Add worst-status dot to `MercadoRow` once client status is computed (Phase 3)
- [ ] Wire `DetalleMercadoScreen` client list (Phase 3)
- [ ] Add map pin picker to `CreateMercadoScreen` (deferred)
- [ ] Wire Lista Negra navigation (Phase 7)
