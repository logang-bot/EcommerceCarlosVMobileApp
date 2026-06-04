# Design Mockups — Source Files

## Live design URL

```
https://api.anthropic.com/v1/design/h/0qfJ4vVzUyw3HA2lTF-fSg?open_file=Pedidos+y+Cuentas+-+Mockups.html
```

Always fetch this URL at the start of a session to get the latest version. The response is a gzip tar archive. Extract it with:

```bash
# 1. The WebFetch tool saves the binary to a path shown in its result — copy that path, then:
cp <path_from_webfetch_result> /tmp/design.tar.gz
gunzip -f /tmp/design.tar.gz          # produces /tmp/design.tar
tar -xf /tmp/design.tar -C /tmp/design_extracted/
# Files land in /tmp/design_extracted/comercializadora-carlos-v-mobile-app/project/
```

After extracting, compare changed files against `docs/design/` and copy over any updates:

```bash
diff /tmp/design_extracted/comercializadora-carlos-v-mobile-app/project/screens2.jsx \
     docs/design/screens2.jsx
```

---

Exported from Claude Design (claude.ai/design). These are the source files behind the "Pedidos y Cuentas — Mockups" canvas. Use them as the pixel-perfect reference when implementing each screen.

## File map

| File | Contents |
|------|----------|
| `styles.css` | All design tokens — dark + light color variables, typography, component base styles |
| `kit.jsx` | Shared primitives: `Phone`, `StatusBar`, `AppBar`, `FAB`, `Btn`, `Field`, `StatusBadge`, `PayChip`, `Avatar`, `EmptyState`, `Icon` SVG paths |
| `data.jsx` | Sample data — `PRODUCTS`, `MERCADOS`, `CLIENTES`, `PEDIDOS`, `BLACKLIST`, `CART` |
| `screens1.jsx` | Auth & Home: `LoginScreen`, `MercadosScreen`, `BusquedaScreen`, `ReporteScreen` |
| `screens2.jsx` | Clientes & Pedidos: `DetalleMercadoScreen`, `DetalleClienteScreen`, `DetallePedidoScreen`, `SaldoExtraScreen` |
| `screens3.jsx` | Creación de Pedido: product grid, search, cart panel, line-item sheet, payment sheet |
| `screens4.jsx` | Catálogo & Altas: `ProductosScreen`, `CreateProductoScreen`, `CreateMercadoScreen`, `CreateClienteScreen` |
| `screens5.jsx` | Lista Negra: `ListaNegraScreen`, `AgregarListaNegraScreen` |
| `screens-empty.jsx` | Empty states for every list/search screen |

## How to use in a new session

Tell Claude: *"Read the design files in `docs/design/` — `styles.css` for tokens, `kit.jsx` for component specs, and the relevant `screensN.jsx` for the screens you're implementing."*

## Key design decisions recorded in chat

- Catalog rows are **tap-to-edit**, no swipe-to-reveal
- "Agregar saldo extra" button sits **below** "Agregar a Lista Negra" in Detalle de Cliente
- Saldo Extra screen lives in the **Clientes & Pedidos** section (not Altas)
- Client rows are **fully color-washed** by status (red/amber/green tint + left accent bar)
- ⋯ filter menu on Detalle Mercado: A–Z (default), Críticos primero, Mayor saldo, Solo con deuda, Restablecer
