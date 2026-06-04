/* Screens — Creación de Pedido (cart flow) + bottom sheets */

const PROD_KIND = Object.fromEntries(PRODUCTS.map(p => [p.id, p.kind]));
const TINT_FOR = (kind) => {
  const map = { bottle: 210, bag: 40, box: 25, can: 350, jar: 150, block: 280 };
  const h = map[kind] ?? 210;
  return `hsl(${h} var(--tile-s, 22%) var(--tile-l, 18%))`;
};

// ── product card ───────────────────────────────────────────────────
function ProductCard({ p, qty = 0 }) {
  const inCart = qty > 0;
  return (
    <div style={{
      borderRadius: 14, overflow: 'hidden', background: 'var(--surface-2)',
      boxShadow: inCart ? 'inset 0 0 0 1.6px var(--accent)' : 'inset 0 0 0 1px var(--border)',
      position: 'relative',
    }}>
      {/* image */}
      <div style={{ height: 72, background: TINT_FOR(p.kind), display: 'grid', placeItems: 'center', position: 'relative' }}>
        <ProductGlyph kind={p.kind} size={32} />
        {inCart && (
          <div style={{ position: 'absolute', top: 6, right: 6, width: 20, height: 20, borderRadius: '50%', background: 'var(--accent)', display: 'grid', placeItems: 'center', boxShadow: '0 1px 4px rgba(0,0,0,0.4)' }}>
            <Icon name="check" size={13} stroke={2.4} color="#fff" />
          </div>
        )}
      </div>
      {/* meta */}
      <div style={{ padding: '7px 8px 8px' }}>
        <div style={{ fontSize: 11.5, fontWeight: 600, lineHeight: 1.25, height: 29, overflow: 'hidden', letterSpacing: -0.1 }}>{p.name}</div>
        {!inCart ? (
          <div className="mono" style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-2)', marginTop: 4 }}>{money(p.price, false)}</div>
        ) : (
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 6, height: 28, background: 'var(--accent-tint)', borderRadius: 9, padding: '0 2px' }}>
            <button style={{ width: 28, height: 28, display: 'grid', placeItems: 'center', color: 'var(--accent)' }}><Icon name="minus" size={16} stroke={2.2} /></button>
            <span className="mono" style={{ fontSize: 13.5, fontWeight: 700, color: 'var(--accent)' }}>{qty}</span>
            <button style={{ width: 28, height: 28, display: 'grid', placeItems: 'center', color: 'var(--accent)' }}><Icon name="plus" size={16} stroke={2.2} /></button>
          </div>
        )}
      </div>
    </div>
  );
}

function SearchCell() {
  return (
    <div style={{
      borderRadius: 14, background: 'var(--surface)', boxShadow: 'inset 0 0 0 1px var(--border-2)',
      display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 8, minHeight: 124,
    }}>
      <div style={{ width: 38, height: 38, borderRadius: 11, background: 'var(--surface-3)', display: 'grid', placeItems: 'center' }}>
        <Icon name="search" size={20} color="var(--accent)" />
      </div>
      <span style={{ fontSize: 11.5, fontWeight: 600, color: 'var(--text-2)' }}>Buscar producto</span>
    </div>
  );
}

// ── cart panel ─────────────────────────────────────────────────────
function CartPanel({ items, total }) {
  const empty = items.length === 0;
  return (
    <div style={{
      flexShrink: 0, background: 'var(--surface)', borderTop: '1px solid var(--border-2)',
      boxShadow: '0 -10px 30px rgba(0,0,0,0.35)', borderTopLeftRadius: 20, borderTopRightRadius: 20,
    }}>
      <div style={{ display: 'flex', justifyContent: 'center', paddingTop: 8 }}>
        <div style={{ width: 36, height: 4, borderRadius: 2, background: 'var(--border-3)' }} />
      </div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 20px 6px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <Icon name="cart" size={18} color="var(--text-2)" />
          <span style={{ fontSize: 14, fontWeight: 600 }}>Carrito</span>
          <span style={{ fontSize: 12, color: 'var(--text-3)', whiteSpace: 'nowrap' }}>· {items.length} productos</span>
        </div>
      </div>
      {/* rows */}
      <div style={{ maxHeight: 132, overflow: 'hidden' }}>
        {empty && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 11, padding: '6px 20px 12px' }}>
            <div style={{ width: 38, height: 38, borderRadius: 11, background: 'var(--surface-2)', boxShadow: 'inset 0 0 0 1px var(--border)', display: 'grid', placeItems: 'center', flexShrink: 0 }}>
              <Icon name="cart" size={18} color="var(--text-4)" />
            </div>
            <span style={{ fontSize: 13.5, color: 'var(--text-3)' }}>Agrega productos al pedido</span>
          </div>
        )}
        {items.map((it, i) => (
          <div key={it.id} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '8px 20px' }}>
            <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--accent)', width: 26, flexShrink: 0 }} className="mono">×{it.qty}</span>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 13.5, fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{it.name}</div>
              <div className="mono" style={{ fontSize: 11.5, color: 'var(--text-3)', marginTop: 1 }}>{it.qty} × {money(it.unit, false)}</div>
            </div>
            <span className="mono" style={{ fontSize: 13.5, fontWeight: 600 }}>{money(it.qty * it.unit, false)}</span>
            <button style={{ width: 24, height: 24, borderRadius: '50%', background: 'var(--surface-3)', display: 'grid', placeItems: 'center', flexShrink: 0 }}>
              <Icon name="close" size={12} color="var(--text-2)" />
            </button>
          </div>
        ))}
      </div>
      {/* total + cta */}
      <div style={{ padding: '10px 18px 12px' }}>
        <button disabled={empty} style={{
          width: '100%', height: 54, borderRadius: 15,
          background: empty ? 'var(--surface-3)' : 'var(--accent)', color: empty ? 'var(--text-3)' : '#fff',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 10px 0 20px',
          boxShadow: empty ? 'none' : '0 4px 16px rgba(91,141,239,0.35)', cursor: empty ? 'default' : 'pointer',
        }}>
          <span style={{ fontSize: 15.5, fontWeight: 600, whiteSpace: 'nowrap' }}>Confirmar Pedido</span>
          <span style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <span className="mono" style={{ fontSize: 16, fontWeight: 700 }}>{money(total)}</span>
            <span style={{ width: 34, height: 34, borderRadius: 11, background: empty ? 'var(--surface-2)' : 'rgba(255,255,255,0.18)', display: 'grid', placeItems: 'center' }}>
              <Icon name="chevron" size={18} color={empty ? 'var(--text-3)' : '#fff'} stroke={2.2} />
            </span>
          </span>
        </button>
      </div>
    </div>
  );
}

// ── base grid + cart (reused by sheet states) ──────────────────────
function CreacionBase({ search = false, scrim = false, emptyCart = false, query = 'ar' }) {
  const sorted = [...PRODUCTS].sort((a, b) => a.name.localeCompare(b.name));
  const cartItems = emptyCart ? [] : CART;
  const cartQty = Object.fromEntries(cartItems.map(c => [c.id, c.qty]));
  const total = cartItems.reduce((s, c) => s + c.qty * c.unit, 0);
  const shown = search ? sorted.filter(p => new RegExp(query, 'i').test(p.name)) : sorted;

  return (
    <>
      <AppBar title="Nuevo pedido" subtitle="Ana Rodríguez · Mercado Central" back actions={<button style={{ width: 40, height: 40, display: 'grid', placeItems: 'center', borderRadius: 12 }}><Icon name="close" /></button>} />
      {search && (
        <div style={{ flexShrink: 0, padding: '4px 16px 12px' }}>
          <div style={{ height: 44, borderRadius: 13, background: 'var(--surface-2)', boxShadow: 'inset 0 0 0 1.5px var(--accent)', display: 'flex', alignItems: 'center', gap: 10, padding: '0 14px' }}>
            <Icon name="search" size={19} color="var(--accent)" />
            <span style={{ flex: 1, fontSize: 15.5, fontWeight: 500 }}>{query}</span>
            <span style={{ width: 1.5, height: 20, background: 'var(--accent)' }} />
            <button style={{ width: 22, height: 22, borderRadius: '50%', background: 'var(--surface-3)', display: 'grid', placeItems: 'center' }}><Icon name="close" size={13} color="var(--text-2)" /></button>
          </div>
        </div>
      )}
      <div className="pedidos-scroll" style={{ flex: 1, overflow: 'hidden', padding: '14px 16px 16px' }}>
        {search && shown.length === 0 ? (
          <EmptyState icon="search" compact title="Sin coincidencias" subtitle={`Ningún producto coincide con “${query}”.`} />
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10 }}>
            {!search && <SearchCell />}
            {shown.map(p => <ProductCard key={p.id} p={p} qty={cartQty[p.id] || 0} />)}
          </div>
        )}
      </div>
      <CartPanel items={cartItems} total={total} />
      {scrim && <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.55)', zIndex: 30 }} />}
    </>
  );
}

function CreacionPedidoScreen() {
  return <Phone><CreacionBase /></Phone>;
}

function CreacionBusquedaScreen() {
  return <Phone><CreacionBase search /></Phone>;
}

function CreacionBusquedaEmptyScreen() {
  return <Phone><CreacionBase search query="zzz" /></Phone>;
}

function CreacionCartEmptyScreen() {
  return <Phone><CreacionBase emptyCart /></Phone>;
}

// ── bottom sheet wrapper ───────────────────────────────────────────
function Sheet({ children, title, height }) {
  return (
    <div style={{ position: 'absolute', left: 0, right: 0, bottom: 0, zIndex: 40 }}>
      <div style={{ background: 'var(--surface)', borderTopLeftRadius: 24, borderTopRightRadius: 24, boxShadow: '0 -16px 40px rgba(0,0,0,0.5)', paddingBottom: 4 }}>
        <div style={{ display: 'flex', justifyContent: 'center', paddingTop: 10 }}>
          <div style={{ width: 40, height: 4.5, borderRadius: 3, background: 'var(--border-3)' }} />
        </div>
        {children}
      </div>
    </div>
  );
}

// ── line-item edit sheet ───────────────────────────────────────────
function LineEditSheet() {
  return (
    <Phone>
      <CreacionBase scrim />
      <Sheet>
        <div style={{ padding: '14px 22px 22px' }}>
          {/* product header */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 13, marginBottom: 20 }}>
            <div style={{ width: 48, height: 48, borderRadius: 13, background: TINT_FOR('box'), display: 'grid', placeItems: 'center' }}>
              <ProductGlyph kind="box" size={28} />
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 16.5, fontWeight: 700, letterSpacing: -0.3 }}>Café Madrid 250g</div>
              <div style={{ fontSize: 12.5, color: 'var(--text-3)', marginTop: 2 }}>Precio de catálogo · Bs. 5,20</div>
            </div>
          </div>

          {/* quantity stepper */}
          <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-2)', marginBottom: 9 }}>Cantidad</div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', height: 56, background: 'var(--surface-2)', borderRadius: 15, padding: '0 8px', boxShadow: 'inset 0 0 0 1px var(--border-2)', marginBottom: 18 }}>
            <button style={{ width: 44, height: 44, borderRadius: 11, background: 'var(--surface-3)', display: 'grid', placeItems: 'center' }}><Icon name="minus" size={20} /></button>
            <span className="mono" style={{ fontSize: 22, fontWeight: 700 }}>4</span>
            <button style={{ width: 44, height: 44, borderRadius: 11, background: 'var(--accent-tint)', display: 'grid', placeItems: 'center' }}><Icon name="plus" size={20} color="var(--accent)" /></button>
          </div>

          {/* unit price */}
          <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-2)', marginBottom: 9 }}>Precio unitario</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, height: 54, background: 'var(--surface-2)', borderRadius: 14, padding: '0 16px', boxShadow: 'inset 0 0 0 1.5px var(--amber)' }}>
            <span style={{ fontSize: 15, color: 'var(--text-2)', fontWeight: 500 }}>Bs.</span>
            <span className="mono" style={{ flex: 1, fontSize: 16, fontWeight: 600 }}>3,00</span>
            <span style={{ width: 1.5, height: 22, background: 'var(--amber)' }} />
          </div>
          {/* amber disclaimer */}
          <div style={{ marginTop: 10, padding: '12px 14px', background: 'var(--amber-tint)', borderRadius: 13, display: 'flex', gap: 10, boxShadow: 'inset 0 0 0 1px rgba(231,178,62,0.22)' }}>
            <Icon name="tag" size={18} color="var(--amber-text)" style={{ flexShrink: 0, marginTop: 1 }} />
            <div>
              <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--amber-text)' }}>El precio ha sido modificado</div>
              <div style={{ fontSize: 12.5, color: 'var(--text-2)', marginTop: 3, lineHeight: 1.45 }}>
                El precio estándar es <b style={{ color: 'var(--text)' }}>Bs. 5,20</b>. Estás registrando <b style={{ color: 'var(--text)' }}>Bs. 3,00</b>.
              </div>
            </div>
          </div>

          {/* notes */}
          <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-2)', margin: '18px 0 9px' }}>Nota <span style={{ color: 'var(--text-4)', fontWeight: 400 }}>(opcional)</span></div>
          <div style={{ minHeight: 70, background: 'var(--surface-2)', borderRadius: 14, padding: '13px 16px', boxShadow: 'inset 0 0 0 1px var(--border-2)', fontSize: 14.5, color: 'var(--text)', lineHeight: 1.45 }}>
            Descuento acordado — cliente frecuente
          </div>

          {/* subtotal + done */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', margin: '20px 0 14px' }}>
            <span style={{ fontSize: 14, color: 'var(--text-2)' }}>Subtotal de la línea</span>
            <span className="mono" style={{ fontSize: 22, fontWeight: 700 }}>{money(12.00)}</span>
          </div>
          <Btn>Listo</Btn>
        </div>
      </Sheet>
    </Phone>
  );
}

// ── payment options sheet ──────────────────────────────────────────
function PagoSheet() {
  const opt = (icon, title, sub, tint, color, active) => (
    <button style={{
      width: '100%', display: 'flex', alignItems: 'center', gap: 14, padding: '15px 16px', borderRadius: 15,
      background: active ? tint : 'var(--surface-2)', boxShadow: active ? `inset 0 0 0 1.5px ${color}` : 'inset 0 0 0 1px var(--border)', textAlign: 'left',
    }}>
      <div style={{ width: 40, height: 40, borderRadius: 12, background: tint, display: 'grid', placeItems: 'center', flexShrink: 0 }}>
        <Icon name={icon} size={20} color={color} />
      </div>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 15, fontWeight: 600 }}>{title}</div>
        <div style={{ fontSize: 12.5, color: 'var(--text-3)', marginTop: 2 }}>{sub}</div>
      </div>
      <span style={{ width: 20, height: 20, borderRadius: '50%', background: active ? color : 'transparent', boxShadow: active ? 'none' : 'inset 0 0 0 1.6px var(--border-3)', display: 'grid', placeItems: 'center' }}>
        {active && <Icon name="check" size={13} stroke={2.6} color="var(--on-success)" />}
      </span>
    </button>
  );
  return (
    <Phone>
      <CreacionBase scrim />
      <Sheet>
        <div style={{ padding: '12px 22px 24px' }}>
          <div style={{ textAlign: 'center', marginBottom: 4 }}>
            <div style={{ fontSize: 18, fontWeight: 700, letterSpacing: -0.3 }}>Confirmar pedido</div>
            <div className="mono" style={{ fontSize: 30, fontWeight: 700, letterSpacing: -1, marginTop: 8 }}>{money(73.00)}</div>
            <div style={{ fontSize: 13, color: 'var(--text-2)', marginTop: 2 }}>3 productos · Ana Rodríguez</div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginTop: 20 }}>
            {opt('check', 'Marcar como pagado', 'El cliente pagó el total ahora', 'var(--green-tint)', 'var(--green)', false)}
            {opt('money', 'Pago parcial', 'Registrar un abono inicial', 'var(--accent-tint)', 'var(--accent)', true)}
            {/* revealed amount input under partial */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, height: 54, background: 'var(--surface-2)', borderRadius: 14, padding: '0 16px', boxShadow: 'inset 0 0 0 1.5px var(--accent)', margin: '-2px 0 2px' }}>
              <span style={{ fontSize: 15, color: 'var(--text-2)', fontWeight: 500 }}>Bs.</span>
              <span className="mono" style={{ flex: 1, fontSize: 16, fontWeight: 600 }}>30,00</span>
              <span style={{ fontSize: 12.5, color: 'var(--text-3)' }}>Restan {money(43.00)}</span>
            </div>
            {opt('tag', 'Dejar pendiente', 'Suma al saldo del cliente', 'var(--amber-tint)', 'var(--amber)', false)}
          </div>
          <div style={{ marginTop: 18 }}><Btn>Registrar pedido</Btn></div>
        </div>
      </Sheet>
    </Phone>
  );
}

Object.assign(window, { CreacionPedidoScreen, CreacionBusquedaScreen, CreacionBusquedaEmptyScreen, CreacionCartEmptyScreen, LineEditSheet, PagoSheet, ProductCard, CartPanel, CreacionBase });
