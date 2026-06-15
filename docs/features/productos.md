# Feature: Catálogo de Productos

## Status: ✅ Done (Phase 6)

---

## Spec summary

Global product catalogue shared across all users. Products appear in the Creación de Pedido grid. Users can create, edit, and delete products. Each product has an optional photo, name, optional description, and price.

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Catálogo (tab 1 of HomeScreen) | tab 1 inside `HomeRoute` | `ui/screen/producto/CatalogoScreen.kt` |
| Crear / Editar Producto | `CreateProductoRoute(productId?)` | `ui/screen/producto/CreateProductoScreen.kt` |

`CatalogoScreen` is not directly in `AppNavigation` — it lives as tab index 1 inside `HomeScreen`, same pattern as `MercadosScreen`.

---

## Role-based access (INVITADO restrictions)

`CatalogoUiState` carries `canWrite: Boolean` (computed as `user?.role != UserRole.INVITADO`).

| Element | Who sees it / behavior |
|---------|------------------------|
| FAB "Producto" (create) | SUPERUSUARIO + USUARIO only |
| Product row tap / chevron | SUPERUSUARIO + USUARIO only — INVITADO rows have no `Modifier.clickable` and no chevron icon |

INVITADO users see the full product catalogue (names, descriptions, prices, photos) but cannot create, edit, or delete products. Rows are visually plain (no chevron, not tappable).

Implementation: `ProductoRow` accepts `onClick: (() -> Unit)?`. When `null`, the `Modifier.clickable` block and the `ChevronRight` icon are both skipped.

---

## UI notes

**CatalogoScreen:**
- Large top bar: "Productos" / "N productos en catálogo"
- Inline search bar (always visible, live-filters the list)
- Product rows: 50×50 tile + name + description + price + chevron
- Tapping a row navigates to `CreateProductoRoute(productId)` for editing
- FAB "Producto" → `CreateProductoRoute()` (create)
- Empty state: `Icons.Default.Tag`, title "Catálogo vacío", hint "Nuevo producto"

**Crear / Editar Producto:**
- 132×132 square photo picker (optional) — camera or gallery
- Name (required), Description (optional), Price in Bs. (required)
- "Guardar" primary CTA; edit mode adds "Eliminar producto" danger button with `AlertDialog` confirm

---

## Data layer

| File | Location |
|------|----------|
| `Producto.kt` | `domain/model/` |
| `ProductoRepository.kt` | `domain/repository/` |
| `ProductoEntity.kt` | `data/local/entity/` |
| `ProductoDao.kt` | `data/local/dao/` |
| `ProductoMapper.kt` | `data/mapper/` |
| `ProductoDto.kt` | `data/remote/dto/` |
| `ProductoRepositoryImpl.kt` | `data/repository/impl/` |

Room migration `MIGRATION_6_7` creates the `productos` table (version 7).

> `kind` (glyph key: bottle/bag/box/can/jar/block) from the design mockup is intentionally omitted — not surfaced in the create form and adds no business value without a picker UI.

---

## Open TODOs

- [ ] Wire Supabase Storage upload for product photos (Phase 9)
- [x] Show actual photo in `ProductoTile` — done via `PhotoThumbnail` (no Coil needed; local FileProvider URIs loaded with `BitmapFactory`)
- [ ] Add `kind`-based colored tile icons if design is updated to include a picker
