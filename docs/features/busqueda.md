# Feature: Búsqueda Global

## Status: ✅ Done

---

## Spec summary

Global search across all clientes and mercados by name or phone number. Accessible from the search icon in the Mercados top bar. Results are split into two sections: Clientes and Mercados. Tapping a cliente navigates to `DetalleClienteRoute`; tapping a mercado navigates to `DetalleMercadoRoute`.

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
- **Empty state — no query**: Search icon + "Busca clientes y mercados" + instructional subtitle
- **Empty state — query with no results**: Search icon + "Sin resultados" subtitle (mentions both clientes and mercados)
- **Results**: `LazyColumn` with two sections, each preceded by a `SearchGroupLabel` (icon + uppercase label + count):
  - **Clientes** (`Person` icon): `ClienteResultRow` — 42dp `ClienteAvatar` + name + mercado name + status badge chip + balance. Divider at `start = 75.dp`.
  - **Mercados** (`GridView` icon): `MercadoResultRow` — 42dp rounded tile (`PhotoThumbnail`, `GridView` fallback) + name + "N clientes activos" + chevron. Divider at `start = 75.dp`.
- Sections only rendered when they have at least one result.

---

## State

```kotlin
data class BusquedaUiState(
    val query: String = "",
    val clienteResults: List<ClienteSearchResult> = emptyList(),
    val mercadoResults: List<MercadoSearchResult> = emptyList(),
    val isSearching: Boolean = false,
) {
    val hasResults get() = clienteResults.isNotEmpty() || mercadoResults.isNotEmpty()
}

data class ClienteSearchResult(
    val clienteId: String,
    val name: String,
    val photoUrl: String?,
    val mercadoName: String,
    val status: ClientStatus,
    val balance: Double,
)

data class MercadoSearchResult(
    val mercadoId: String,
    val name: String,
    val photoUrl: String?,
    val clientesCount: Int,
)
```

---

## ViewModel

`BusquedaViewModel` injects `ClienteRepository` and `MercadoRepository`. Uses `combine(_query, clienteRepository.getAll(), mercadoRepository.getAll())` to reactively filter both lists by query. Clientes are matched on name or phone; mercados on name. `clientesCount` per mercado is derived from the client list in-memory (no extra DB call).

> Client status and balance default to `AL_DIA / 0.0` until Phase 4 wires real pedidos.

`ClienteRepository` needed a new `getAll(): Flow<List<Cliente>>` method (added to DAO, interface, and impl).

---

## Files

| File | Description |
|------|-------------|
| `ui/screen/busqueda/BusquedaUiState.kt` | `BusquedaUiState` + `ClienteSearchResult` + `MercadoSearchResult` |
| `ui/screen/busqueda/BusquedaViewModel.kt` | `combine` on both repos; real search wired |
| `ui/screen/busqueda/BusquedaScreen.kt` | Sectioned results with `SearchGroupLabel`, `ClienteResultRow`, `MercadoResultRow` |

---

## Open TODOs

- [ ] Phase 4: replace hardcoded `AL_DIA / 0.0` defaults in `ClienteSearchResult` with real balance/status from pedidos
