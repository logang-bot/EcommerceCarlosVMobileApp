# Feature: Catálogo de Productos

## Status: 🔲 Pending (Phase 6)

---

## Spec summary

Global product catalogue shared across all users. Products appear in the Creación de Pedido grid. Users can create, edit, and delete products. Each product has a photo (required), name, optional description, and price.

---

## Screens

| Screen | Route | File |
|--------|-------|------|
| Lista de Productos | `ProductosRoute` | `ui/screen/producto/ProductosScreen.kt` — TODO |
| Crear / Editar Producto | `CreateProductoRoute(productoId?)` | `ui/screen/producto/CreateProductoScreen.kt` — TODO |

---

## UI notes

**Lista de Productos:**
- Title: "Productos"
- Scrollable list (or grid) — each row: photo thumbnail + name + price
- Tap row → opens edit screen (no swipe-to-reveal — confirmed design decision)
- FAB: "Producto"
- Search bar in app bar

**Crear / Editar Producto:**
- Photo (required) — tap to pick from gallery or camera
- Name (required)
- Description (optional)
- Price (required, numeric, in Bs.)
- "Guardar" primary CTA
- Edit mode only: "Eliminar producto" destructive button + confirmation dialog

---

## Data models needed

- `Producto` domain model: `id, name, description, price, imageUrl, createdAt`
- `ProductoRepository` interface + impl
- Room entity: `ProductoEntity`
- Supabase storage: `productos/{productoId}/image.jpg`

---

## Open TODOs

- [ ] Define `ProductoEntity`, `ProductoDao`, `ProductoDto`, `ProductoMapper`
- [ ] Implement `ProductosScreen` and `ProductosViewModel`
- [ ] Implement `CreateProductoScreen` with camera + gallery image picker
- [ ] Wire Supabase Storage upload for product images
- [ ] Add `ProductosRoute` to `AppRoutes.kt` and `AppNavigation.kt`
