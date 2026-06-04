/* Screens — Cliente flow: Detalle Mercado, Detalle Cliente, Detalle Pedido */

// ── 2 · Detalle de Mercado — Lista de Clientes ─────────────────────
function ClienteRow({ c }) {
  const s = STATUS[c.status];
  return (
    <div style={{ position: 'relative', display: 'flex', alignItems: 'center', gap: 13, padding: '13px 18px 13px 16px', background: s.tint, borderLeft: `3px solid ${s.dot}` }}>
      <Avatar name={c.name} size={46} status={c.status} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 15.5, fontWeight: 600, letterSpacing: -0.2 }}>{c.name}</div>
        <div style={{ fontSize: 12.5, color: 'var(--text-2)', marginTop: 2, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{c.desc}</div>
      </div>
      <div style={{ textAlign: 'right', flexShrink: 0 }}>
        <StatusBadge status={c.status} size="sm" />
        <div className="mono" style={{ fontSize: 14, fontWeight: 600, marginTop: 6, color: c.balance > 0 ? 'var(--text)' : 'var(--text-3)' }}>
          {money(c.balance)}
        </div>
      </div>
    </div>
  );
}

const CLIENTE_RANK = { crit: 0, warn: 1, ok: 2 };
function sortClientes(mode) {
  let arr = [...CLIENTES];
  if (mode === 'crit')         arr.sort((a, b) => CLIENTE_RANK[a.status] - CLIENTE_RANK[b.status] || b.balance - a.balance);
  else if (mode === 'balance') arr.sort((a, b) => b.balance - a.balance);
  else if (mode === 'debt')    arr = arr.filter(c => c.balance > 0).sort((a, b) => b.balance - a.balance);
  else                         arr.sort((a, b) => a.name.localeCompare(b.name)); // 'az'
  return arr;
}

const FILTER_ITEMS = [
  { key: 'az',      icon: 'sort',   label: 'Orden alfabético', sub: 'A — Z' },
  { key: 'crit',    icon: 'ban',    label: 'Críticos primero', sub: 'Por nivel de riesgo' },
  { key: 'balance', icon: 'money',  label: 'Mayor saldo primero', sub: 'De más a menos deuda' },
  { key: 'debt',    icon: 'receipt',label: 'Solo con deuda', sub: 'Oculta los que están al día' },
];

function FilterMenu({ active = 'az' }) {
  return (
    <div style={{ position: 'absolute', top: 46, right: 10, zIndex: 50, width: 256,
      background: 'var(--elevated)', borderRadius: 16, padding: 6,
      boxShadow: '0 16px 44px rgba(0,0,0,0.45), 0 0 0 1px var(--border-2)' }}>
      <div style={{ fontSize: 11.5, fontWeight: 600, letterSpacing: 0.4, textTransform: 'uppercase', color: 'var(--text-3)', padding: '9px 12px 7px' }}>Ordenar y filtrar</div>
      {FILTER_ITEMS.map(it => {
        const on = it.key === active;
        return (
          <div key={it.key} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 12px', borderRadius: 11, background: on ? 'var(--accent-soft)' : 'transparent' }}>
            <Icon name={it.icon} size={19} color={on ? 'var(--accent)' : 'var(--text-2)'} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 14, fontWeight: 600, color: on ? 'var(--accent)' : 'var(--text)' }}>{it.label}</div>
              <div style={{ fontSize: 11.5, color: 'var(--text-3)', marginTop: 1 }}>{it.sub}</div>
            </div>
            {on && <Icon name="check" size={17} color="var(--accent)" stroke={2.4} />}
          </div>
        );
      })}
      <div style={{ height: 1, background: 'var(--border)', margin: '5px 10px' }} />
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 12px', borderRadius: 11, opacity: active === 'az' ? 0.4 : 1 }}>
        <Icon name="back" size={19} color="var(--text-2)" />
        <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-2)' }}>Restablecer (normal)</span>
      </div>
    </div>
  );
}

function DetalleMercadoScreen({ sort = 'az', menu = false }) {
  const list = sortClientes(sort);
  const labelFor = { az: '12 clientes', crit: 'Ordenado: críticos primero', balance: 'Ordenado: mayor saldo', debt: `${list.length} con deuda` };
  return (
    <Phone>
      <div style={{ position: 'relative' }}>
        <AppBar title="Mercado Central" subtitle={labelFor[sort]} back actions={<><BarIcon name="search" /><button style={{ width: 40, height: 40, display: 'grid', placeItems: 'center', borderRadius: 12, position: 'relative', color: menu ? 'var(--accent)' : 'var(--text)', background: menu ? 'var(--accent-soft)' : 'transparent' }}><Icon name="dots" size={21} /></button></>} />
        {menu && <FilterMenu active={sort} />}
      </div>
      <Body pad={false}>
        {menu && <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.28)', zIndex: 40 }} />}
        <div style={{ paddingTop: 8, display: 'flex', flexDirection: 'column', gap: 3 }}>
          {list.map(c => <ClienteRow key={c.id} c={c} />)}
        </div>
        <div style={{ padding: '16px 20px 0' }}>
          <button style={{
            width: '100%', height: 48, borderRadius: 13, background: 'transparent',
            boxShadow: 'inset 0 0 0 1px var(--border-2)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 9,
            color: 'var(--text-2)', fontSize: 14, fontWeight: 600,
          }}>
            <Icon name="ban" size={18} color="var(--red-text)" />
            Ver Lista Negra de este mercado
          </button>
        </div>
      </Body>
      <FAB label="Cliente" />
    </Phone>
  );
}

const DetalleMercadoMenuScreen = () => <DetalleMercadoScreen sort="crit" menu />;
const DetalleMercadoCritScreen = () => <DetalleMercadoScreen sort="crit" />;

// ── 3 · Detalle de Cliente ─────────────────────────────────────────
function PedidoRow({ o }) {
  const isSaldo = o.kind === 'saldo';
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 13, padding: '14px 20px', background: isSaldo ? 'rgba(231,178,62,0.045)' : 'transparent' }}>
      <div style={{
        width: 42, height: 42, borderRadius: 12, flexShrink: 0,
        background: isSaldo ? 'var(--amber-tint)' : 'var(--surface-3)',
        display: 'grid', placeItems: 'center', boxShadow: isSaldo ? 'none' : 'inset 0 0 0 1px var(--border)',
      }}>
        <Icon name={isSaldo ? 'tag' : 'receipt'} size={19} color={isSaldo ? 'var(--amber-text)' : 'var(--text-2)'} />
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 7 }}>
          <span style={{ fontSize: 14.5, fontWeight: 600 }}>{isSaldo ? 'Saldo extra' : `${o.items} productos`}</span>
          {isSaldo && <span style={{ fontSize: 10.5, fontWeight: 700, letterSpacing: 0.3, color: 'var(--amber-text)', background: 'var(--amber-tint)', padding: '2px 6px', borderRadius: 5, textTransform: 'uppercase', whiteSpace: 'nowrap' }}>Manual</span>}
        </div>
        <div style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 2, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {o.date}{isSaldo && o.label ? ` · ${o.label.replace('Saldo extra · ', '')}` : ''}
        </div>
      </div>
      <div style={{ textAlign: 'right', flexShrink: 0 }}>
        <div className="mono" style={{ fontSize: 14.5, fontWeight: 600 }}>{money(o.total, false)}</div>
        <div style={{ marginTop: 5 }}><PayChip status={o.status} /></div>
      </div>
    </div>
  );
}

const BAL_GRAD = {
  crit: { bg: 'linear-gradient(160deg, rgba(240,90,80,0.15), rgba(240,90,80,0.04))', ring: 'rgba(240,90,80,0.22)' },
  warn: { bg: 'linear-gradient(160deg, rgba(231,178,62,0.15), rgba(231,178,62,0.04))', ring: 'rgba(231,178,62,0.22)' },
  ok:   { bg: 'linear-gradient(160deg, rgba(54,200,128,0.14), rgba(54,200,128,0.03))', ring: 'rgba(54,200,128,0.20)' },
};

function DetalleClienteScreen({ empty = false }) {
  const c = empty ? CLIENTES[1] : CLIENTES[0]; // empty → Carlos Méndez (al día, 0)
  const g = BAL_GRAD[c.status];
  return (
    <Phone>
      <AppBar title="Cliente" back actions={<><BarIcon name="doc" /><BarIcon name="dots" /></>} />
      <Body pad={false}>
        {/* header */}
        <div style={{ padding: '6px 20px 18px', display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
          <Avatar name={c.name} size={76} />
          <div style={{ fontSize: 21, fontWeight: 700, letterSpacing: -0.4, marginTop: 12 }}>{c.name}</div>
          <div style={{ fontSize: 13.5, color: 'var(--text-2)', marginTop: 3 }}>{c.desc}</div>
          {/* contact chips */}
          <div style={{ display: 'flex', gap: 9, marginTop: 14 }}>
            {[{ i: 'phone', t: c.phone }, { i: 'pin', t: 'Ubicación' }].map((x, k) => (
              <button key={k} style={{
                display: 'flex', alignItems: 'center', gap: 7, height: 38, padding: '0 14px', borderRadius: 11,
                background: 'var(--surface-2)', boxShadow: 'inset 0 0 0 1px var(--border-2)', color: 'var(--text)',
                fontSize: 13.5, fontWeight: 500,
              }}>
                <Icon name={x.i} size={16} color="var(--accent)" /> {x.t}
              </button>
            ))}
          </div>
        </div>

        {/* balance block */}
        <div style={{ padding: '0 20px' }}>
          <div style={{
            borderRadius: 18, padding: '18px 20px',
            background: g.bg,
            boxShadow: `inset 0 0 0 1px ${g.ring}`,
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          }}>
            <div>
              <div style={{ fontSize: 12.5, color: 'var(--text-2)', fontWeight: 500 }}>Saldo pendiente total</div>
              <div className="mono" style={{ fontSize: 32, fontWeight: 700, letterSpacing: -1, marginTop: 4 }}>{money(c.balance)}</div>
            </div>
            <StatusBadge status={c.status} />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginTop: 12 }}>
            <button style={{
              width: '100%', height: 46, borderRadius: 13, background: 'var(--red-tint)',
              color: 'var(--red-text)', fontSize: 14.5, fontWeight: 600, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
              boxShadow: 'inset 0 0 0 1px rgba(240,90,80,0.25)', whiteSpace: 'nowrap',
            }}>
              <Icon name="ban" size={18} /> Agregar a Lista Negra
            </button>
            <button style={{
              width: '100%', height: 46, borderRadius: 13, background: 'var(--surface-2)',
              color: 'var(--text)', fontSize: 14.5, fontWeight: 600, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
              boxShadow: 'inset 0 0 0 1px var(--border-2)', whiteSpace: 'nowrap',
            }}>
              <Icon name="tag" size={18} color="var(--amber-text)" /> Agregar saldo extra
            </button>
          </div>
        </div>

        {/* pedidos */}
        <div style={{ paddingTop: 24, minHeight: empty ? 280 : 0, display: 'flex', flexDirection: 'column' }}>
          <SectionLabel>Pedidos</SectionLabel>
          {empty ? (
            <div style={{ flex: 1 }}>
              <EmptyState icon="receipt" compact
                title="Sin pedidos todavía"
                subtitle="Este cliente aún no tiene pedidos registrados."
                hint="Nuevo pedido" />
            </div>
          ) : PEDIDOS.map((o, i) => (
            <React.Fragment key={o.id}>
              {i > 0 && <div style={{ height: 1, background: 'var(--border)', marginLeft: 75 }} />}
              <PedidoRow o={o} />
            </React.Fragment>
          ))}
        </div>
      </Body>
      <FAB label="Nuevo pedido" icon="cart" />
    </Phone>
  );
}

// ── 4 · Detalle de Pedido ──────────────────────────────────────────
function LineItem({ l }) {
  const changed = l.unit !== l.cat;
  return (
    <div style={{ padding: '14px 20px' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
        <div style={{ width: 38, height: 38, borderRadius: 10, flexShrink: 0, background: 'var(--surface-3)', display: 'grid', placeItems: 'center', boxShadow: 'inset 0 0 0 1px var(--border)' }}>
          <ProductGlyph kind={l.kind || 'box'} size={24} />
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', gap: 10 }}>
            <span style={{ fontSize: 14.5, fontWeight: 600 }}>{l.name}</span>
            <span className="mono" style={{ fontSize: 14.5, fontWeight: 600, flexShrink: 0 }}>{money(l.qty * l.unit, false)}</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 7, marginTop: 4, fontSize: 13, color: 'var(--text-2)' }} className="mono">
            <span>{l.qty} × {money(l.unit, false)}</span>
            {changed && (
              <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4, color: 'var(--amber-text)' }}>
                <span style={{ width: 5, height: 5, borderRadius: '50%', background: 'var(--amber)' }} />
                <span style={{ textDecoration: 'line-through', opacity: 0.7 }}>{money(l.cat, false)}</span>
              </span>
            )}
          </div>
          {l.note && (
            <div style={{ fontSize: 12.5, color: 'var(--text-3)', marginTop: 6, fontStyle: 'italic', lineHeight: 1.4 }}>“{l.note}”</div>
          )}
        </div>
      </div>
    </div>
  );
}

function DetallePedidoScreen() {
  const d = { ...PEDIDO_DETAIL, lines: [
    { name: 'Harina PAN 1kg', qty: 12, unit: 2.50, cat: 2.50, kind: 'bag' },
    { name: 'Aceite Vatel 1L', qty: 6, unit: 4.50, cat: 4.50, kind: 'bottle' },
    { name: 'Arroz Primor 1kg', qty: 10, unit: 3.00, cat: 3.00, kind: 'bag' },
    { name: 'Café Madrid 250g', qty: 4, unit: 3.00, cat: 5.20, kind: 'box', note: 'Descuento acordado — cliente frecuente' },
  ]};
  const remaining = d.total - d.paid;
  return (
    <Phone>
      <AppBar title="Pedido" subtitle={d.date} back actions={<BarIcon name="dots" />} />
      <Body pad={false}>
        {/* status strip */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 20px 14px' }}>
          <PayChip status={d.status} />
          <span style={{ fontSize: 12.5, color: 'var(--text-3)' }}>4 productos · Ana Rodríguez</span>
        </div>
        <div className="hr" />
        {/* line items */}
        {d.lines.map((l, i) => (
          <React.Fragment key={i}>
            {i > 0 && <div style={{ height: 1, background: 'var(--border)', marginLeft: 70 }} />}
            <LineItem l={l} />
          </React.Fragment>
        ))}
        <div className="hr" />
        {/* totals */}
        <div style={{ padding: '16px 20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14, color: 'var(--text-2)', marginBottom: 9 }}>
            <span>Total del pedido</span><span className="mono">{money(d.total)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14, color: 'var(--green-text)', marginBottom: 9 }}>
            <span>Pagado</span><span className="mono">− {money(d.paid)}</span>
          </div>
          <div style={{ height: 1, background: 'var(--border)', margin: '12px 0' }} />
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <span style={{ fontSize: 15, fontWeight: 600 }}>Saldo restante</span>
            <span className="mono" style={{ fontSize: 22, fontWeight: 700, color: 'var(--amber-text)' }}>{money(remaining)}</span>
          </div>
        </div>

        {/* payments */}
        <div style={{ padding: '4px 0 8px' }}>
          <SectionLabel>Pagos registrados</SectionLabel>
          {d.payments.map((p, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '11px 20px' }}>
              <div style={{ width: 30, height: 30, borderRadius: 9, background: 'var(--green-tint)', display: 'grid', placeItems: 'center' }}>
                <Icon name="check" size={16} color="var(--green-text)" />
              </div>
              <span style={{ flex: 1, fontSize: 13.5, color: 'var(--text-2)' }}>{p.date}</span>
              <span className="mono" style={{ fontSize: 14, fontWeight: 600, color: 'var(--green-text)' }}>+ {money(p.amount, false)}</span>
            </div>
          ))}
        </div>
      </Body>
      <BottomBar>
        <Btn variant="secondary" icon="plus">Pago parcial</Btn>
        <Btn variant="success" icon="check">Marcar pagado</Btn>
      </BottomBar>
    </Phone>
  );
}

const DetalleClienteEmptyScreen = () => <DetalleClienteScreen empty />;

Object.assign(window, { DetalleMercadoScreen, DetalleMercadoMenuScreen, DetalleMercadoCritScreen, DetalleClienteScreen, DetalleClienteEmptyScreen, DetallePedidoScreen, ClienteRow, PedidoRow });
