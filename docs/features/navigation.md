# Feature: Navigation & Home Shell

## Status: ✅ Done (Phase 2f)

---

## Overview

The app has a single top-level navigation graph (`AppNavigation.kt`). After login, the user lands on `HomeRoute`, which is a tab shell managing the three main sections via a persistent bottom navigation bar.

---

## Routes

| Route | Screen | Notes |
|-------|--------|-------|
| `LoginRoute` | `LoginScreen` | Start destination |
| `HomeRoute` | `HomeScreen` | Post-login shell; manages bottom nav tabs |
| `DetalleMercadoRoute(mercadoId)` | `DetalleMercadoScreen` | Pushed on top of Home |
| `CreateMercadoRoute(mercadoId?)` | `CreateMercadoScreen` | Create or edit |
| `PerfilRoute` | `PerfilScreen` | User profile |
| `EditarPerfilRoute` | `EditarPerfilScreen` | Edit name/email/phone |
| `GestionUsuariosRoute` | `GestionUsuariosScreen` | Superuser only |
| `UsuarioDetalleRoute(userId)` | `UsuarioDetalleScreen` | |
| `CrearUsuarioRoute` | `CrearUsuarioScreen` | Create user with temp password |
| `BusquedaRoute` | `BusquedaScreen` | Global client search |
| `ClientesRoute(mercadoId)` | `ClientesScreen` | Client list for a mercado |
| `DetalleClienteRoute(clienteId)` | `DetalleClienteScreen` | Client detail + pedidos list |
| `CreateClienteRoute(mercadoId, clienteId?)` | `CreateClienteScreen` | Create or edit client |
| `ListaNegraRoute` | `ListaNegraScreen` | Global blacklist |
| `AgregarListaNegraRoute(clienteId)` | `AgregarListaNegraScreen` | Add client to blacklist |
| `CreateProductoRoute(productId?)` | `CreateProductoScreen` | Create or edit product |
| `CreacionPedidoRoute(clienteId, clienteName, mercadoName)` | `CreacionPedidoScreen` | Cart-based order creation (Phase 4) |
| `DetallePedidoRoute(pedidoId)` | `DetallePedidoScreen` | Order detail + payment actions (Phase 5) |
| `SaldoExtraRoute(clienteId)` | `SaldoExtraScreen` | Manual balance entry (Phase 3) |

All routes are `@Serializable` objects/data classes in `presentation/screens/AppRoutes.kt`.

---

## HomeScreen & bottom navigation

`HomeScreen` (`ui/screen/home/HomeScreen.kt`) manages tab selection with `rememberSaveable { mutableIntStateOf(0) }` and renders the appropriate tab screen via a `when` expression.

### Bottom navigation tabs

| Index | Key | Icon | Label | Screen |
|-------|-----|------|-------|--------|
| 0 | mercados | `Icons.Default.GridView` | Mercados | `MercadosScreen` |
| 1 | productos | `Icons.Default.Sell` | Productos | `ProductosStubScreen` *(Phase 6)* |
| 2 | reporte | `Icons.Default.Assessment` | Reporte | `ReporteStubScreen` *(Phase 8)* |

The `AppBottomNavBar` composable (`ui/screen/home/AppBottomNavBar.kt`) renders a Material3 `NavigationBar` with zero tonal elevation on the app background. Selected item indicator uses `extendedColors.accentSoft`.

### Tab screen pattern

Each tab screen (including stubs) receives `selectedTab: Int` and `onTabSelected: (Int) -> Unit` and includes `AppBottomNavBar` in its own `Scaffold.bottomBar`. This gives each tab its own `TopBar` and `FloatingActionButton` managed by the same Scaffold, so the FAB is automatically elevated above the nav bar.

```
HomeScreen
  └── when(selectedTab):
        0 → MercadosScreen(selectedTab, onTabSelected)
              Scaffold(topBar, floatingActionButton, bottomBar = AppBottomNavBar)
        1 → ProductosStubScreen(selectedTab, onTabSelected)
              Scaffold(topBar, bottomBar = AppBottomNavBar)
        2 → ReporteStubScreen(selectedTab, onTabSelected)
              Scaffold(topBar, bottomBar = AppBottomNavBar)
```

### Stub screens

`ProductosStubScreen` and `ReporteStubScreen` live in `ui/screen/home/StubScreens.kt`. Each shows an `EmptyState` with the section icon and a "próximamente" message. They will be replaced by real screens in Phase 6 (Catálogo de Productos) and Phase 8 (Reporte Diario) respectively.

---

## Login → Home navigation

```kotlin
// LoginScreen on success:
navController.navigate(HomeRoute) {
    popUpTo(LoginRoute) { inclusive = true }
}
```

`LoginRoute` is popped inclusive so the back button from `HomeScreen` exits the app rather than returning to login.

---

## Logout navigation

```kotlin
// PerfilScreen on logout:
navController.navigate(LoginRoute) {
    popUpTo(0) { inclusive = true }
}
```

`popUpTo(0)` clears the entire back stack so pressing back from login does not navigate back into the app.
