/* Screens — Auth + Home: Login, Mercados, Búsqueda Global, Reporte Diario */

// ── Brand mark ─────────────────────────────────────────────────────
function BrandMark({ size = 60 }) {
  return (
    <div style={{
      width: size, height: size, borderRadius: size * 0.28, flexShrink: 0,
      background: 'linear-gradient(150deg, #6E9BF5, #4878dd)',
      display: 'grid', placeItems: 'center', color: '#fff',
      fontWeight: 700, fontSize: size * 0.4, letterSpacing: -1,
      boxShadow: '0 8px 24px rgba(91,141,239,0.35)',
      fontFamily: 'Geist, sans-serif',
    }}>CV</div>
  );
}

// ── 0 · Login ──────────────────────────────────────────────────────
function LoginScreen() {
  return (
    <Phone>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: '0 26px' }}>
        <div style={{ flex: 1 }} />
        {/* brand */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
          <BrandMark size={64} />
          <div style={{ marginTop: 20, fontSize: 23, fontWeight: 700, letterSpacing: -0.6 }}>Comercializadora Carlos V</div>
          <div style={{ marginTop: 6, fontSize: 14.5, color: 'var(--text-2)', whiteSpace: 'nowrap' }}>Pedidos &amp; Cuentas</div>
        </div>

        <div style={{ height: 40 }} />

        {/* fields */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <Field label="Correo" value="carlos@comercializadora.ve" />
          <Field label="Contraseña" value="••••••••••" />
        </div>

        <div style={{ height: 20 }} />
        <Btn>Iniciar sesión</Btn>

        {/* divider */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 14, margin: '22px 0' }}>
          <div style={{ flex: 1, height: 1, background: 'var(--border)' }} />
          <span style={{ fontSize: 12.5, color: 'var(--text-3)' }}>o</span>
          <div style={{ flex: 1, height: 1, background: 'var(--border)' }} />
        </div>

        {/* biometric */}
        <button style={{
          height: 52, borderRadius: 14, background: 'var(--surface-2)', color: 'var(--text)',
          boxShadow: 'inset 0 0 0 1px var(--border-2)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', gap: 10, fontSize: 15.5, fontWeight: 600, whiteSpace: 'nowrap',
        }}>
          <Icon name="fingerprint" size={22} color="var(--accent)" />
          Entrar con huella
        </button>

        <div style={{ flex: 1.3 }} />
        <div style={{ textAlign: 'center', fontSize: 12, color: 'var(--text-4)', paddingBottom: 8 }}>
          Acceso exclusivo para titulares de cuenta
        </div>
      </div>
    </Phone>
  );
}

// ── 1 · Home — Lista de Mercados ───────────────────────────────────
function MercadoRow({ m }) {
  const s = STATUS[m.status];
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '15px 20px' }}>
      <div style={{
        width: 44, height: 44, borderRadius: 13, flexShrink: 0, background: 'var(--surface-3)',
        display: 'grid', placeItems: 'center', color: 'var(--text-2)',
        boxShadow: 'inset 0 0 0 1px var(--border)',
      }}>
        <Icon name="grid" size={20} />
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontSize: 16, fontWeight: 600, letterSpacing: -0.2, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{m.name}</span>
          {m.status !== 'ok' && <span style={{ width: 7, height: 7, borderRadius: '50%', background: s.dot, flexShrink: 0 }} />}
        </div>
        <div style={{ fontSize: 13, color: 'var(--text-2)', marginTop: 2, whiteSpace: 'nowrap' }}>{m.clientes} clientes activos</div>
      </div>
      <Icon name="chevron" size={18} color="var(--text-3)" />
    </div>
  );
}

function MercadosScreen() {
  const sorted = [...MERCADOS].sort((a, b) => a.name.localeCompare(b.name));
  return (
    <Phone>
      <AppBar title="Mercados" subtitle="6 mercados · 80 clientes" large actions={<><BarIcon name="search" /><BarIcon name="bell" badge /></>} />
      <Body pad={false}>
        <div style={{ paddingTop: 4 }}>
          {sorted.map((m, i) => (
            <React.Fragment key={m.id}>
              {i > 0 && <div style={{ height: 1, background: 'var(--border)', marginLeft: 78 }} />}
              <MercadoRow m={m} />
            </React.Fragment>
          ))}
        </div>
        {/* lista negra */}
        <div style={{ padding: '18px 20px 0' }}>
          <button style={{
            width: '100%', height: 54, borderRadius: 14, background: 'var(--surface-2)',
            boxShadow: 'inset 0 0 0 1px var(--border)', display: 'flex', alignItems: 'center', gap: 13, padding: '0 16px',
          }}>
            <div style={{ width: 34, height: 34, borderRadius: 10, background: 'var(--red-tint)', display: 'grid', placeItems: 'center' }}>
              <Icon name="ban" size={19} color="var(--red-text)" />
            </div>
            <div style={{ flex: 1, textAlign: 'left' }}>
              <div style={{ fontSize: 14.5, fontWeight: 600 }}>Lista Negra</div>
              <div style={{ fontSize: 12, color: 'var(--text-2)', marginTop: 1 }}>Clientes vetados · todos los mercados</div>
            </div>
            <Icon name="chevron" size={18} color="var(--text-3)" />
          </button>
        </div>
      </Body>
      <FAB label="Mercado" />
    </Phone>
  );
}

// ── Búsqueda Global ────────────────────────────────────────────────
function ResultRow({ c }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 13, padding: '12px 20px' }}>
      <Avatar name={c.name} size={42} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 15.5, fontWeight: 600 }}>{c.name}</div>
        <div style={{ fontSize: 12.5, color: 'var(--text-2)', marginTop: 2 }}>{c.mercado}</div>
      </div>
      <div style={{ textAlign: 'right' }}>
        <StatusBadge status={c.status} size="sm" />
        <div className="mono" style={{ fontSize: 13, color: 'var(--text-2)', marginTop: 5 }}>{money(c.balance)}</div>
      </div>
    </div>
  );
}

function BusquedaScreen() {
  const results = [
    { name: 'María González', mercado: 'Mercado Central', status: 'ok', balance: 8.00 },
    { name: 'Ramón Villegas', mercado: 'Mercado de Coche', status: 'crit', balance: 415.00 },
    { name: 'José Hernández', mercado: 'Mercado Central', status: 'ok', balance: 0.00 },
    { name: 'Gloria Pérez', mercado: 'Mercado Central', status: 'crit', balance: 268.00 },
    { name: 'Marcos Rivas', mercado: 'Mercado La Hoyada', status: 'warn', balance: 54.00 },
  ];
  return (
    <Phone>
      <div style={{ flexShrink: 0, padding: '8px 14px 12px', display: 'flex', alignItems: 'center', gap: 4 }}>
        <button style={{ width: 40, height: 40, display: 'grid', placeItems: 'center', borderRadius: 12 }}><Icon name="back" /></button>
        <div style={{
          flex: 1, height: 44, borderRadius: 13, background: 'var(--surface-2)', boxShadow: 'inset 0 0 0 1px var(--border-2)',
          display: 'flex', alignItems: 'center', gap: 10, padding: '0 14px',
        }}>
          <Icon name="search" size={19} color="var(--text-2)" />
          <span style={{ flex: 1, fontSize: 15.5, fontWeight: 500 }}>mar</span>
          <span style={{ width: 1.5, height: 20, background: 'var(--accent)' }} />
          <button style={{ width: 22, height: 22, borderRadius: '50%', background: 'var(--surface-3)', display: 'grid', placeItems: 'center' }}>
            <Icon name="close" size={13} color="var(--text-2)" />
          </button>
        </div>
      </div>
      <Body pad={false}>
        <div style={{ padding: '4px 20px 10px', fontSize: 12.5, color: 'var(--text-3)' }}>
          5 resultados en clientes
        </div>
        {results.map((c, i) => (
          <React.Fragment key={i}>
            {i > 0 && <div style={{ height: 1, background: 'var(--border)', marginLeft: 75 }} />}
            <ResultRow c={c} />
          </React.Fragment>
        ))}
      </Body>
    </Phone>
  );
}

// ── Reporte Diario ─────────────────────────────────────────────────
function StatCard({ label, value, accent, icon, sub }) {
  return (
    <div style={{
      flex: 1, background: 'var(--surface-2)', borderRadius: 16, padding: '16px 16px 18px',
      boxShadow: 'inset 0 0 0 1px var(--border)',
    }}>
      <div style={{ width: 34, height: 34, borderRadius: 10, background: accent.tint, display: 'grid', placeItems: 'center', marginBottom: 14 }}>
        <Icon name={icon} size={18} color={accent.text} />
      </div>
      <div className="mono" style={{ fontSize: 22, fontWeight: 600, color: accent.text, letterSpacing: -0.5 }}>{value}</div>
      <div style={{ fontSize: 12.5, color: 'var(--text-2)', marginTop: 4 }}>{label}</div>
      {sub && <div style={{ fontSize: 11.5, color: 'var(--text-3)', marginTop: 2 }}>{sub}</div>}
    </div>
  );
}

function ReporteScreen() {
  const movs = [
    { name: 'Doris Salazar', mercado: 'Mercado Central', amount: 85.50, type: 'cobro' },
    { name: 'María González', mercado: 'Mercado Central', amount: 47.50, type: 'pedido' },
    { name: 'Eduardo Ramos', mercado: 'Mercado Central', amount: 32.00, type: 'cobro' },
    { name: 'Luisa Marcano', mercado: 'Mercado La Hoyada', amount: 96.50, type: 'pedido' },
  ];
  return (
    <Phone>
      <AppBar title="Reporte diario" back actions={<BarIcon name="doc" />} />
      <Body pad={false}>
        <div style={{ padding: '14px 20px 6px', fontSize: 13.5, color: 'var(--text-2)', fontWeight: 500 }}>
          Martes, 3 de junio de 2026
        </div>
        {/* hero collected */}
        <div style={{ padding: '8px 20px 4px' }}>
          <div style={{
            borderRadius: 18, padding: '20px 22px', background: 'linear-gradient(160deg, rgba(54,200,128,0.16), rgba(54,200,128,0.05))',
            boxShadow: 'inset 0 0 0 1px rgba(54,200,128,0.22)',
          }}>
            <div style={{ fontSize: 12.5, color: 'var(--green-text)', fontWeight: 600, letterSpacing: 0.3, textTransform: 'uppercase' }}>Cobrado hoy</div>
            <div className="mono" style={{ fontSize: 38, fontWeight: 700, letterSpacing: -1, marginTop: 6 }}>{money(617.50)}</div>
            <div style={{ fontSize: 13, color: 'var(--text-2)', marginTop: 4 }}>de 9 pagos registrados</div>
          </div>
        </div>
        {/* two stats */}
        <div style={{ display: 'flex', gap: 12, padding: '12px 20px 4px' }}>
          <StatCard label="Pedidos creados" value="14" icon="cart" accent={{ tint: 'var(--accent-tint)', text: 'var(--accent)' }} />
          <StatCard label="Pendiente del día" value={money(238)} icon="money" accent={{ tint: 'var(--amber-tint)', text: 'var(--amber-text)' }} />
        </div>
        {/* movements */}
        <div style={{ paddingTop: 20 }}>
          <SectionLabel>Movimientos de hoy</SectionLabel>
          {movs.map((m, i) => (
            <React.Fragment key={i}>
              {i > 0 && <div style={{ height: 1, background: 'var(--border)', marginLeft: 20 }} />}
              <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '13px 20px' }}>
                <div style={{ width: 8, height: 8, borderRadius: '50%', background: m.type === 'cobro' ? 'var(--green)' : 'var(--accent)' }} />
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 14.5, fontWeight: 500 }}>{m.name}</div>
                  <div style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 1 }}>{m.type === 'cobro' ? 'Pago recibido' : 'Nuevo pedido'} · {m.mercado}</div>
                </div>
                <div className="mono" style={{ fontSize: 14, fontWeight: 600, color: m.type === 'cobro' ? 'var(--green-text)' : 'var(--text)' }}>
                  {m.type === 'cobro' ? '+' : ''}{money(m.amount, false)}
                </div>
              </div>
            </React.Fragment>
          ))}
        </div>
      </Body>
    </Phone>
  );
}

Object.assign(window, { LoginScreen, MercadosScreen, BusquedaScreen, ReporteScreen, BrandMark });
