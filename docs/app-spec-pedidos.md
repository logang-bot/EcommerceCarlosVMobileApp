# App Design Spec: Pedidos & Cuentas

## Overview

Mobile app (Android-first) for small business owners (primarily Hispanic market) to manage delivery orders ("pedidos") and track outstanding balances owed by their customers. The app is used in the context of local markets ("mercados") — physical locations that group multiple individual customers. The owner delivers products to these customers and needs to track what has been paid, what is pending, and who owes how much.

---

## Visual Direction

- **Theme**: Dark mode (near-black surfaces, ~#0F0F0F or similar). The app must support dark mode as the primary theme. Light mode is a secondary variant — design dark first.
- **Style**: Clean, utilitarian — no unnecessary decoration. Every element earns its place.
- **Reference**: The existing PhoebeStore screen (attached) establishes the aesthetic baseline: dark surfaces, white text, subtle borders, product grid with real photos, large tap targets.
- **Typography**: Clear hierarchy. Large bold labels for names/totals. Smaller muted text for secondary info.
- **Color accents**:
  - Green → Fine / Paid / OK status
  - Amber/yellow → Warning / Partial payment
  - Red → Critical / Overdue / Blacklisted
  - Neutral accent (e.g. slate blue or soft white) → CTAs
- **Spacing**: Generous. Avoid clutter. Use whitespace to separate sections.
- **Language**: Spanish UI. All labels, buttons, and copy should be in Spanish.

## Design Output Instructions (for Claude Design)

- Produce **static screen mockups only** — no interactive prototypes, no functional components, no click logic.
- Each screen should be a standalone visual frame at mobile dimensions (390×844 or similar Android equivalent).
- For screens with content that exceeds the viewport height (e.g. long lists, forms), show the screen in a **scrollable frame**: render the full content height, with a visible device frame/chrome that clips at the fold to indicate scroll is expected. Do not truncate content.
- Show placeholder content (realistic fake names, amounts in Bs., fake dates) to make the mockups feel grounded.
- Include a status bar and navigation bar as part of the device chrome.

---

## Roles & Authentication

### Screen: Login
- Email/password field OR biometric authentication button (fingerprint/face)
- Only account owners can log in
- No registration flow visible to end user

### Roles
- **Superusuario**: Can create, edit, enable, disable, and delete any user account. Has full system access.
- **Usuario**: Standard access — can view and manage all Mercados, Clientes, and Pedidos. No account management capabilities.

---

## Entity Hierarchy

```
Mercado (shared — all users access the same Mercados)
└── Cliente (Individual customer)
    └── Pedido (Order — paid, pending, or partial)
        └── Detalle de pedido (Line items: product + quantity + unit price + notes)

Catálogo de Productos (global, shared across all users)
└── Producto (name, photo, description, price)

Usuarios (managed by Superusuario only)
```

**Important**: Mercados, Clientes, Pedidos, and the Catálogo de Productos are shared resources — any logged-in user can view and interact with them. There is no per-user data ownership. The Superusuario manages user accounts only.

---

## Screen 1: Home — Lista de Mercados

**Purpose**: Entry point after login. Shows all Mercados in the system.

**Layout**:
- Screen title: "Mercados"
- Alphabetically sorted list of Mercados
- Each row shows:
  - Mercado name
  - Number of active clientes
  - A subtle status indicator if any cliente inside is in Critical or Warning state
- At the bottom of the list: a button **"Lista Negra"** that navigates to the global blacklist view (a special read-only Mercado that aggregates all blacklisted clientes across all Mercados)
- FAB (Floating Action Button) to create a new Mercado

---

## Screen 2: Detalle de Mercado — Lista de Clientes

**Purpose**: Shows all clientes within a Mercado.

**Layout**:
- Screen title: Mercado name
- Alphabetically sorted list of Clientes (non-blacklisted)
- Each cliente row shows:
  - Photo (circular avatar, optional)
  - Name
  - Debt status badge: **Al día** (green) / **Advertencia** (amber) / **Crítico** (red)
  - Total pending balance (e.g. "Bs. 120.00")
- At the bottom: a single button **"Ver Lista Negra de este Mercado"** → navigates to the blacklist filtered by this Mercado
- FAB to add a new Cliente to this Mercado

---

## Screen 3: Detalle de Cliente

**Purpose**: Shows all pedidos for a specific cliente and their running balance.

**Layout**:
- Header:
  - Cliente photo (if available)
  - Name, description, phone numbers (tappable)
  - Location pin (tappable → opens map)
  - Total balance owed (large, prominent)
  - Status badge (Al día / Advertencia / Crítico)
  - Button: **"Agregar a Lista Negra"** (only shown if not already blacklisted)
- Pedidos list (sorted by date, newest first):
  - Each row: date, brief product summary (e.g. "3 productos"), total amount, payment status chip (Pagado / Pendiente / Parcial)
  - Tapping a pedido opens its detail
- FAB: **"Nuevo Pedido"**
- Also accessible from here: **"Agregar Saldo Extra"** (special manual balance entry — see below)

---

## Screen 4: Detalle de Pedido

**Purpose**: Shows the line items of a single pedido and its payment history.

**Layout**:
- Date of pedido
- List of line items:
  - Product name
  - Quantity delivered
  - Unit price (if it differs from the catalogue price at the time of the pedido, show a small amber indicator and the original catalogue price for reference)
  - Subtotal
  - Notes (if any were added for that line item, shown as a small muted text below)
- Total amount
- Payment status (Pagado / Pendiente / Parcial)
- Payments section (if partial):
  - List of payments made: date + amount
  - Button: **"Registrar pago parcial"**
- Button: **"Marcar como pagado"** (if not fully paid)

---

## Screen 5: Creación de Pedido

**Purpose**: Create a new pedido for a cliente. Modeled after an "add to cart" shopping experience — the user browses products, adds them to the pedido, adjusts quantities inline, and confirms when done.

**This is a single-screen flow.** The user never leaves this screen until they confirm or cancel the pedido.

### Layout

The screen is divided into two persistent regions:

**Top region — Product browser (scrollable grid, 3 columns):**
- Each cell: product photo + name + price (from the product catalogue)
- The first cell of the grid is always fixed and reserved for **"Buscar producto"** (search icon)
- All remaining cells are catalogue products, displayed in alphabetical order

**"Buscar producto" behavior:**
- Tapping the search cell does **not** navigate to a new screen
- Instead, a **search bar slides in at the top of the screen** (below the app bar), and the product grid below it is filtered live as the user types
- Products that don't match the query fade out or are hidden; matching ones remain visible with the same card style
- The cart panel remains visible at the bottom throughout — the user can add products directly from search results without losing their cart state
- Tapping outside the search bar or pressing the back/clear button dismisses the search and restores the full grid

**Adding products from the grid:**
- Tapping a product card does **not** navigate away. Instead:
  - If the product is not yet in the pedido: it is added immediately with quantity = 1, and its card shows a quantity badge/pill (e.g. "×1") with inline − and + controls
  - If the product is already in the pedido: the inline − / + controls on the card let the user adjust quantity directly
  - Setting quantity to 0 removes the product from the pedido
- The card visual state changes clearly between "not added" and "in cart" (e.g. a highlight border, a filled badge, a checkmark overlay)

**Bottom region — Carrito (Cart summary, always visible):**
- A persistent panel anchored to the bottom of the screen
- Lists all added products as compact rows: product name, quantity, unit price, subtotal
- Shows the running total (e.g. "Total: Bs. 47.50")
- Each row has a remove (×) button
- **Tapping a cart row** expands it (or opens a bottom sheet) to allow editing that line item's details:
  - **Quantity stepper** (− / [number] / +), large tap targets
  - **Unit price field** (editable, numeric — pre-filled with the catalogue price)
    - If the user modifies the unit price away from the catalogue default, a **disclaimer banner** appears immediately below the field, styled in amber/yellow:
      - Title: **"El precio ha sido modificado"**
      - Body: explains the new price differs from the standard catalogue price (e.g. "El precio estándar es Bs. 4.00. Estás registrando Bs. 3.00.")
  - **Notes field** (optional, multiline text) — for the user to explain the reason for any change (e.g. "producto dañado", "promoción", "descuento acordado")
  - Running subtotal for that line item updates live
- If the cart is empty, the panel shows a placeholder: "Agrega productos al pedido"
- CTA button: **"Confirmar Pedido"** — disabled when cart is empty, enabled otherwise

### On tapping "Confirmar Pedido":
A bottom sheet or modal appears with the payment options:
- **"Marcar como pagado"**
- **"Dejar pendiente"**
- **"Pago parcial"** → reveals an amount input field to register the partial payment now

---

## Screen 10: Gestión de Productos — Catálogo

**Purpose**: The user manages their product catalogue. Products created here appear in the Creación de Pedido grid.

**Layout**:
- Screen title: "Productos"
- Searchable, scrollable list (or grid) of all products
- Each product row/card shows: photo thumbnail, name, price
- FAB to create a new product
- Long-press or swipe on a product row to reveal **Editar** / **Eliminar** actions
- Tapping a product opens its detail/edit screen

---

## Screen 11: Crear / Editar Producto

**Purpose**: Create a new product or edit an existing one.

**Fields**:
- Photo (required — tap to pick from gallery or camera)
- Name (required)
- Description (optional — e.g. variant, size, notes)
- Price (required — numeric input, in Bs.)

**Actions**:
- **"Guardar"** CTA (primary)
- On edit mode: **"Eliminar producto"** destructive button at the bottom, with a confirmation dialog before deleting

---

## Screen 6: Agregar Saldo Extra ("Extra Saldo")

**Purpose**: Manually register a balance owed by a cliente that doesn't come from a specific product delivery. This is a special type of pedido.

**Fields**:
- Category: **"Saldo"** (pre-selected, locked)
- Description (text, required — explain what this saldo is for)
- Amount (numeric input)
- Date (defaults to today)

---

## Screen 7: Lista Negra (Blacklist)

**Purpose**: View all blacklisted clientes. Can be accessed globally (from Home) or filtered by Mercado.

**Layout**:
- Title: "Lista Negra" (or "Lista Negra — [Mercado name]" if filtered)
- List of blacklisted clientes:
  - Photo, name, reason for blacklisting, total balance owed
  - Date added to blacklist
- Tapping a cliente → navigates to their Detalle de Cliente (read-only or full access, TBD)

---

## Flow: Agregar Cliente a Lista Negra

**Trigger**: Tapping **"Agregar a Lista Negra"** from Detalle de Cliente.

**Steps**:
1. New screen titled **"Agregar a Lista Negra"**
2. Shows a summary of all pending pedidos for this cliente (for reference)
3. Two options for the total owed:
   - **"Calcular automáticamente"** → sums all pending pedidos automatically
   - **"Ingresar manualmente"** → free numeric input
4. Description field (required): reason for blacklisting
5. CTA: **"Confirmar y agregar a Lista Negra"**
6. This action internally creates an "Extra Saldo" pedido with the entered amount and description, and marks the cliente as blacklisted.

---

## Screen 8: Creación de Mercado

**Fields**:
- Name (required)
- Address (text)
- Location (map picker — drop a pin)
- Photo (optional)

---

## Screen 9: Creación de Cliente

**Fields**:
- Photo (optional, circular crop)
- Name (required)
- Description (important — used to identify the cliente's spot/stall inside the Mercado)
- Phone numbers (list — ability to add multiple)
- Location within Mercado (map picker or text description of spot)

---

## Additional Functionalities

### Búsqueda Global
- Search button accessible from Home or Mercado detail
- Searches across all clientes by name or phone number
- Results show: name, Mercado they belong to, current status
- Tapping any result navigates directly to that cliente's Detalle de Cliente screen

### Exportar Cuenta
- From Detalle de Cliente: export the full pedidos list as a PDF or image
- Should look like a clean printable receipt/statement

### Reporte Diario
- A simple daily summary screen
- Shows: total collected today, total pedidos created today, total pending

---

## Summary of Key Spanish Terms Used in UI

| Term | Meaning |
|------|---------|
| Mercado | Market / Location group of customers |
| Cliente | Individual customer within a Mercado |
| Pedido | Order / delivery record (paid, pending, or partial) |
| Carrito | Cart — the in-progress product selection during pedido creation |
| Saldo Extra | Manual balance entry |
| Lista Negra | Blacklist |
| Al día | Up to date (no debt) |
| Advertencia | Warning (some overdue balance) |
| Crítico | Critical (significant overdue balance) |
| Usuario | Standard app user |
| Superusuario | Super admin |
| Producto | A product in the user's catalogue (name, photo, description, price) |
