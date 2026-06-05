# Feature: Búsqueda Global

## Status: 🟡 Partial — UI done (Phase 2f), results wired in Phase 3

---

## Spec summary

Global search across all clientes by name or phone number. Accessible from the search icon in the Mercados top bar. Results show the client name, their mercado, current debt status badge, and balance. Tapping a result navigates to that cliente's detail screen.

---

## Screen

| Screen | Route | File |
|--------|-------|------|
| Búsqueda Global | `BusquedaRoute` | `ui/screen/busqueda/BusquedaScreen.kt` |

---

## UI implementation notes

- No `Scaffold` top bar — the search bar IS the header
- Layout: back button + `BasicTextField` search bar in a `Row` at the top; results (or empty state) fill the rest
- Search bar: 44dp height, 13dp corners, `surface2` background, `border2` inset, `Search` icon left, clear (`×`) button appears when query is non-empty
- Keyboard focus is requested automatically on `LaunchedEffect(Unit)`
- **Empty state — no query**: Search icon + "Busca clientes" + instructional subtitle
- **Empty state — query with no results**: Person icon + "Sin resultados" subtitle
- **Results**: `LazyColumn` of `SearchResultRow` items with `border` dividers starting at `start = 75.dp`
- Each `SearchResultRow`: 42dp circular avatar (initials), name, mercado name — status badge and balance will be added in Phase 3

---

## State

```kotlin
data class BusquedaUiState(
    val query: String = "",
    val results: List<ClienteSearchResult> = emptyList(),
    val isSearching: Boolean = false,
)

data class ClienteSearchResult(
    val clienteId: String,
    val name: String,
    val mercadoName: String,
    val status: ClientStatus,
    val balance: Double,
)
```

---

## Files

| File | Description |
|------|-------------|
| `ui/screen/busqueda/BusquedaUiState.kt` | `BusquedaUiState` + `ClienteSearchResult` |
| `ui/screen/busqueda/BusquedaViewModel.kt` | Exposes `query` flow; maps to empty results until Phase 3 |
| `ui/screen/busqueda/BusquedaScreen.kt` | Search bar + empty states + result rows |

---

## Open TODOs

- [ ] Inject `ClienteRepository` into `BusquedaViewModel` and wire real search (Phase 3)
- [ ] Render `StatusBadge` (debt status chip) and balance in `SearchResultRow` (Phase 3)
- [ ] Navigate to `DetalleClienteRoute(clienteId)` on row tap (Phase 3)
- [ ] Add phone number search support in the DAO query (Phase 3)
