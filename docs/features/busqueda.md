# Feature: Búsqueda Global

## Status: ✅ Done — Lista Negra section added

---

## Spec summary

Global search across all clientes (active and blacklisted) and mercados by name or phone number. Accessible from the search icon in the Mercados top bar. Results are split into three sections: **Clientes**, **Lista Negra**, **Mercados**. Tapping any client (active or blacklisted) navigates to `DetalleClienteRoute`; tapping a mercado navigates to `DetalleMercadoRoute`.

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
- **Results**: `LazyColumn` with three sections, each preceded by a `SearchGroupLabel` (icon + uppercase label + count):
  - **Clientes** (`Person` icon, `text3` color): `ClienteResultRow` — 42dp `ClienteAvatar` + name + mercado name + status badge chip + balance. Divider at `start = 75.dp`. Only non-blacklisted clients.
  - **Lista Negra** (`Block` icon, `redText` color): `BlacklistResultRow` — 42dp `ClienteAvatar` + red ban badge overlay (18dp circle, `redText` bg, white ban icon, 2dp background-color border ring at bottom-right) + name + mercado name + "En Lista Negra" red pill (redTint bg, redText color) + balance. Divider at `start = 75.dp`.
  - **Mercados** (`GridView` icon, `text3` color): `MercadoResultRow` — 42dp rounded tile (`PhotoThumbnail`, `GridView` fallback) + name + "N clientes activos" + chevron. Divider at `start = 75.dp`.
- Sections only rendered when they have at least one result.
- `SearchGroupLabel` accepts an optional `labelColor: Color?` parameter (defaults to `ext.text3`) used by the Lista Negra section to render in red.

---

## State

```kotlin
data class BusquedaUiState(
    val query: String = "",
    val clienteResults: List<ClienteSearchResult> = emptyList(),
    val blacklistResults: List<BlacklistSearchResult> = emptyList(),
    val mercadoResults: List<MercadoSearchResult> = emptyList(),
    val isSearching: Boolean = false,
) {
    val hasResults get() = clienteResults.isNotEmpty() || blacklistResults.isNotEmpty() || mercadoResults.isNotEmpty()
}

data class ClienteSearchResult(
    val clienteId: String,
    val name: String,
    val photoUrl: String?,
    val mercadoName: String,
    val status: ClientStatus,
    val balance: Double,
)

data class BlacklistSearchResult(
    val clienteId: String,
    val name: String,
    val photoUrl: String?,
    val mercadoName: String,
    val balance: Double,  // from cliente.blacklistBalance
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

`BusquedaViewModel` injects `ClienteRepository` and `MercadoRepository`. Uses `combine(_query, clienteRepository.getAll(), mercadoRepository.getAll())` to reactively filter all lists by query. `getAll()` returns both active and blacklisted clients; the ViewModel splits them: non-blacklisted → `clienteResults`, blacklisted → `blacklistResults`. Mercados matched by name. `clientesCount` per mercado is derived from the client list in-memory (no extra DB call). `BlacklistSearchResult.balance` comes from `cliente.blacklistBalance`.

> Client status and balance in `clienteResults` default to `AL_DIA / 0.0` until Phase 4 wires real pedidos.

`ClienteRepository` needed a new `getAll(): Flow<List<Cliente>>` method (added to DAO, interface, and impl).

---

## Files

| File | Description |
|------|-------------|
| `ui/screen/busqueda/BusquedaUiState.kt` | `BusquedaUiState` + `ClienteSearchResult` + `BlacklistSearchResult` + `MercadoSearchResult` |
| `ui/screen/busqueda/BusquedaViewModel.kt` | Splits `getAll()` into active/blacklisted; populates all three result lists |
| `ui/screen/busqueda/BusquedaScreen.kt` | Three-section results: `ClienteResultRow`, `BlacklistResultRow`, `MercadoResultRow` |

---

## Open TODOs

- [ ] Phase 4: replace hardcoded `AL_DIA / 0.0` defaults in `ClienteSearchResult` with real balance/status from pedidos
