/* Sample data — realistic Venezuelan market placeholder content */

const PRODUCTS = [
  { id: 'p1',  name: 'Aceite Vatel 1L',      desc: 'Aceite vegetal',     price: 4.50, kind: 'bottle' },
  { id: 'p2',  name: 'Arroz Primor 1kg',     desc: 'Grano largo',        price: 3.00, kind: 'bag' },
  { id: 'p3',  name: 'Atún Eveba 140g',      desc: 'En aceite',          price: 3.40, kind: 'can' },
  { id: 'p4',  name: 'Azúcar Montalbán 1kg', desc: 'Refinada',           price: 2.80, kind: 'bag' },
  { id: 'p5',  name: 'Café Madrid 250g',     desc: 'Molido',             price: 5.20, kind: 'box' },
  { id: 'p6',  name: 'Harina PAN 1kg',       desc: 'Maíz precocido',     price: 2.50, kind: 'bag' },
  { id: 'p7',  name: 'Leche Nido 400g',      desc: 'En polvo',           price: 6.00, kind: 'can' },
  { id: 'p8',  name: 'Mantequilla Mavesa',   desc: '250g',               price: 3.20, kind: 'block' },
  { id: 'p9',  name: 'Mayonesa Kraft 445g',  desc: 'Tarro',              price: 4.00, kind: 'jar' },
  { id: 'p10', name: 'Pasta Capri 500g',     desc: 'Spaghetti',          price: 1.80, kind: 'box' },
  { id: 'p11', name: 'Sardina Margarita',    desc: 'En salsa',           price: 2.20, kind: 'can' },
  { id: 'p12', name: 'Salsa La Torre 397g',  desc: 'De tomate',          price: 2.10, kind: 'bottle' },
];

const MERCADOS = [
  { id: 'm1', name: 'Mercado de Coche',     clientes: 18, status: 'crit' },
  { id: 'm2', name: 'Mercado Central',      clientes: 12, status: 'warn' },
  { id: 'm3', name: 'Mercado Guaicaipuro',  clientes: 6,  status: 'ok' },
  { id: 'm4', name: 'Mercado La Hoyada',    clientes: 21, status: 'warn' },
  { id: 'm5', name: 'Mercado Quinta Crespo', clientes: 9, status: 'ok' },
  { id: 'm6', name: 'Mercado San Martín',   clientes: 14, status: 'crit' },
];

const CLIENTES = [
  { id: 'c1', name: 'Ana Rodríguez',     desc: 'Puesto 14 · verduras',         status: 'crit', balance: 340.00, phone: '0414-2230198' },
  { id: 'c2', name: 'Carlos Méndez',     desc: 'Entrada norte · abarrotes',    status: 'ok',   balance: 0.00,   phone: '0412-5567710' },
  { id: 'c3', name: 'Doris Salazar',     desc: 'Puesto 7 · charcutería',       status: 'warn', balance: 85.50,  phone: '0426-1180042' },
  { id: 'c4', name: 'Eduardo Ramos',     desc: 'Pasillo central · frutas',     status: 'ok',   balance: 12.00,  phone: '0414-9902231' },
  { id: 'c5', name: 'Gloria Pérez',      desc: 'Esquina sur · café',           status: 'crit', balance: 268.00, phone: '0424-3340915' },
  { id: 'c6', name: 'José Hernández',    desc: 'Puesto 22 · pescadería',       status: 'ok',   balance: 0.00,   phone: '0416-7781203' },
  { id: 'c7', name: 'Luisa Marcano',     desc: 'Frente a la balanza · víveres', status: 'warn', balance: 47.50, phone: '0412-2218876' },
  { id: 'c8', name: 'María González',    desc: 'Puesto 3 · panadería',         status: 'ok',   balance: 8.00,   phone: '0414-6650117' },
];

// pedidos for cliente "Ana Rodríguez"
const PEDIDOS = [
  { id: 'o1', date: '02 Jun 2026', items: 3, total: 47.50,  status: 'pending', kind: 'order' },
  { id: 'o2', date: '28 May 2026', items: 5, total: 112.00, status: 'partial', kind: 'order', paid: 50.00 },
  { id: 'o3', date: '21 May 2026', items: 2, total: 24.00,  status: 'paid',    kind: 'order' },
  { id: 'o4', date: '15 May 2026', items: 0, total: 60.00,  status: 'pending', kind: 'saldo', label: 'Saldo extra · envases retornables' },
  { id: 'o5', date: '09 May 2026', items: 4, total: 96.50,  status: 'paid',    kind: 'order' },
];

// line items for pedido o2
const PEDIDO_DETAIL = {
  date: '28 May 2026',
  status: 'partial',
  total: 112.00,
  paid: 50.00,
  lines: [
    { name: 'Harina PAN 1kg',    qty: 12, unit: 2.50, cat: 2.50 },
    { name: 'Aceite Vatel 1L',   qty: 6,  unit: 4.50, cat: 4.50 },
    { name: 'Arroz Primor 1kg',  qty: 10, unit: 3.00, cat: 3.00, note: null },
    { name: 'Café Madrid 250g',  qty: 4,  unit: 3.00, cat: 5.20, note: 'Descuento acordado — cliente frecuente' },
  ],
  payments: [
    { date: '28 May 2026', amount: 30.00 },
    { date: '31 May 2026', amount: 20.00 },
  ],
};

// blacklist
const BLACKLIST = [
  { id: 'b1', name: 'Ramón Villegas',  mercado: 'Mercado de Coche', reason: 'Tres pedidos vencidos sin respuesta', balance: 415.00, date: '18 May 2026' },
  { id: 'b2', name: 'Yelitza Áñez',    mercado: 'Mercado San Martín', reason: 'Acuerdo de pago incumplido', balance: 290.50, date: '02 May 2026' },
  { id: 'b3', name: 'Pedro Cabrera',   mercado: 'Mercado de Coche', reason: 'Cambió de puesto sin saldar', balance: 178.00, date: '24 Abr 2026' },
];

// cart (for creación de pedido)
const CART = [
  { id: 'p6',  name: 'Harina PAN 1kg',   qty: 12, unit: 2.50 },
  { id: 'p1',  name: 'Aceite Vatel 1L',  qty: 6,  unit: 4.50 },
  { id: 'p5',  name: 'Café Madrid 250g', qty: 4,  unit: 3.00 },
];

Object.assign(window, { PRODUCTS, MERCADOS, CLIENTES, PEDIDOS, PEDIDO_DETAIL, BLACKLIST, CART });
