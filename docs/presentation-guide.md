# Sales Presentation & Demo Guide — CarlosVCommerce

## Who is this app for?

Small business owners (dueños de negocio) in the Hispanic market who:
- Deliver products to customers scattered across physical markets ("mercados")
- Track which customers owe money and how much
- Currently manage everything on paper, WhatsApp, or spreadsheets
- Need something that works even with bad or no internet

---

## Core Value Proposition (one sentence per audience)

> "CarlosVCommerce te muestra exactamente quién te debe, cuánto, y desde cuándo — desde tu celular, sin internet."

English: "CarlosVCommerce shows you exactly who owes you, how much, and since when — from your phone, without needing internet."

---

## Presentation Structure (15–20 min)

### Slide 1 — The Problem (2 min)
**Headline**: "¿Cuánto te deben hoy?"

Pain points to hit:
- At the end of the day, you don't know the exact total owed to you
- You lose track of partial payments across dozens of customers
- Paper or WhatsApp chats are slow, lose history, and cause disputes
- Your delivery route covers 3–5 different markets — impossible to track mentally

Ask the audience: *"¿Cuántas veces has tenido que llamar a un cliente para recordarle lo que debe?"*

---

### Slide 2 — The Solution (1 min)
**Headline**: "Una app hecha para tu negocio"

Show the app name + icon. Keep it brief:
- Designed for the Latin American small business owner
- Works in Spanish
- Works offline (sin internet)
- Everything in one place: customers, orders, payments, reports

---

### Slide 3 — How It Works (3 min)
Walk through the entity hierarchy visually:

```
Mercado → Cliente → Pedido → Pagos
```

Use a real-world analogy: *"Imagina que tienes tres mercados. En cada mercado tienes clientes. Para cada cliente registras sus pedidos. Para cada pedido ves si pagó, pagó a medias, o no ha pagado nada."*

Key screens to show as thumbnails:
- Mercados list (with status dots)
- Cliente list (green/amber/red badges)
- Pedido creation grid
- Balance block on Detalle Cliente

---

### Slide 4 — Key Features (4 min)
Group features by business need, not by screen:

**"Saber quién te debe"**
- Status badges: Al día (verde), Advertencia (ámbar), Crítico (rojo)
- Total balance per customer always visible
- Lista Negra for high-risk customers

**"Registrar rápido en campo"**
- Product grid with photo and price → one tap to add to pedido
- Adjust quantity and price inline without leaving the screen
- Confirm payment status at checkout (pagado / pendiente / parcial)

**"Cobrar y registrar pagos"**
- Mark orders as paid in one tap
- Register partial payments with amount and date
- Payment history per order

**"Ver tus reportes"**
- Daily report: what was collected today, how many orders
- Per-client report: full balance history, exportable as HTML/PDF
- Works offline, always up to date

**"Trabajo en equipo"**
- Multiple users on the same account
- Roles: Superusuario (admin), Usuario (full access), Invitado (read-only)
- Biometric login (huella digital) for frequent users

**"Sincronización automática"**
- Works completely offline; data saves to the phone instantly
- Syncs to the cloud (Supabase) when internet is available
- Multiple devices see the same data

---

### Slide 5 — Security & Trust (1 min)
- Data is stored securely in the cloud
- Role-based access: not every employee can see everything
- Session required on every app start (nobody can open the app on your phone without logging in)

---

### Slide 6 — Pricing / Next Steps (1 min)
[Customize based on your business model]

---

## Demo Script (10–12 min)

Run this flow on a real device or emulator. Use realistic fake data (Spanish names, amounts in Bs. or the relevant currency).

### Demo Setup (before the meeting)
1. Have at least 2 Mercados created
2. Each mercado should have 4–6 clientes with varied status (at least one Crítico, one Advertencia)
3. One cliente should have 3–4 pedidos (mix of PAID, PARTIAL, PENDING)
4. Have 5–8 products in the catalogue with photos
5. One client on the Lista Negra

---

### Demo Flow

**Step 1: Login (30s)**
- Show the login screen briefly
- If you have biometric set up: show the enrolled-user screen ("Bienvenido de nuevo")
- Log in with fingerprint to show the premium feel

**Step 2: Home — Mercados list (1 min)**
- Point out the status dots on each mercado row (red/amber dots signal problems inside)
- *"Con un solo vistazo ves cuáles mercados necesitan tu atención hoy"*
- Tap into the mercado with the most activity

**Step 3: Cliente list inside a Mercado (1 min)**
- Show the red CRÍTICO badge on a customer
- Show the amber ADVERTENCIA badge
- Point out the balance amount on each row
- *"Esta clienta tiene Bs. 340 pendientes — sin que yo tenga que calcular nada"*

**Step 4: Detalle de Cliente (2 min)**
- Tap on the CRÍTICO client
- Show the large balance block (total owed)
- Show the pedidos list — expand a pedido row to show the product lines
- Tap a PARTIAL pedido → show DetallePedido → tap "Registrar pago parcial"
- Enter an amount → confirm → the balance on the client updates live
- *"El saldo se actualiza en tiempo real — no necesito hacer ningún cálculo"*

**Step 5: Create a new Pedido (3 min)**
- Go back to the client list, select a fresh client
- Tap "Nuevo Pedido"
- Show the 3-column product grid with photos
- Tap 2–3 products — show the quantity badge appearing on the card
- Tap the cart panel at the bottom → tap a row → modify the price → show the amber disclaimer
- *"Si el precio cambió, la app te avisa para que quede registrado el motivo"*
- Tap "Confirmar Pedido" → select "Dejar pendiente"
- Return to the client detail — the new pedido appears and the balance updated

**Step 6: Lista Negra (1 min)**
- Navigate back and tap "Lista Negra" from the mercado screen
- Show the blacklisted client with reason and balance
- *"Los clientes problemáticos quedan separados para que no se mezclen con los demás"*

**Step 7: Catálogo de Productos (1 min)**
- Go to the Productos tab
- Show the grid/list of products with photos
- Tap one to show edit mode
- *"El catálogo es compartido — todos los usuarios ven los mismos productos"*

**Step 8: Reportes (1 min)**
- Go to the Reportes tab
- Show Diario mode: "Cobrado hoy" hero card with total
- Switch to Por Cliente mode → select the active client → show balance stats
- Tap "Generar reporte" → show the export status screen
- *"En un tap generas el reporte de tu cliente — puedes compartirlo directo por WhatsApp"*

**Step 9: Offline mode demo (optional, 1 min)**
- Turn on airplane mode on the device
- Create a new pedido — it saves instantly
- Point out no error messages, app works normally
- *"Funciona sin internet — cuando se reconecte, sincroniza solo"*

---

## Key Objections & Responses

| Objection | Response |
|-----------|----------|
| "Ya uso WhatsApp / cuaderno" | "¿Cuánto tiempo pierdes calculando quién te debe al final del día? La app lo hace en segundos" |
| "¿Y si se me va el internet?" | "Funciona sin internet. Todo queda guardado en tu celular y sube a la nube cuando hay señal" |
| "¿Qué pasa si pierdo el celular?" | "Los datos están en la nube. Entras en otro celular y todo sigue ahí" |
| "Es complicado de usar" | "Está en español. Registrar un pedido tarda menos de 1 minuto — te lo demuestro" |
| "¿Mis empleados pueden ver todo?" | "No. Tú controlas quién puede editar y quién solo puede ver" |

---

## Talking Points to Emphasize

1. **Spanish-first** — not translated, designed in Spanish from the start
2. **Offline-first** — not a web app, doesn't freeze when signal drops
3. **Roles** — multi-user with access control (important for business owners with employees)
4. **Speed** — creating a pedido with the product grid is faster than writing it down
5. **Reports** — shareable HTML reports that look professional (good for sending to accountant or client)
6. **Status system** — Al día / Advertencia / Crítico gives instant visual triage without reading numbers

---

## Slide Deck Outline (for Google Slides / PowerPoint)

1. Cover — App name, tagline, logo
2. The Problem — 3 pain points with icons
3. The Solution — one-liner + entity hierarchy diagram
4. Feature matrix — 3 columns (Know your debtors / Register fast / Reports)
5. How sync works — simple diagram: Phone → Cloud → Other devices
6. Roles — table of Superusuario / Usuario / Invitado
7. Demo screenshots — 4–6 key screens side by side
8. Pricing / Contact / CTA

---

## Screenshots to Capture for Slides

| Screen | Why it sells |
|--------|-------------|
| MercadosScreen with status dots | Instant visual overview |
| ClientesScreen with red/amber badges and balances | Pain point resolution |
| CreacionPedidoScreen with product grid and cart open | Speed and ease |
| DetalleClienteScreen with large balance block | The core value |
| ReporteScreen — Diario with "Cobrado hoy" card | Daily ROI summary |
| ListaNegraScreen | Risk management angle |
| LoginScreen biometric state | Trust / security angle |
