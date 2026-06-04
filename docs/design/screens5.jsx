/* Screens — Forms + Lista Negra: Crear Mercado, Crear Cliente,
   Saldo Extra, Lista Negra, Agregar a Lista Negra */

// ── map placeholder ────────────────────────────────────────────────
function MapPicker({ height = 150 }) {
  return (
    <div style={{ position: 'relative', height, borderRadius: 16, overflow: 'hidden', background: 'var(--map-bg)', boxShadow: 'inset 0 0 0 1px var(--border-2)' }}>
      <svg width="100%" height="100%" style={{ position: 'absolute', inset: 0, opacity: 0.5 }}>
        <defs>
          <pattern id="streets" width="46" height="46" patternUnits="userSpaceOnUse">
            <rect width="46" height="46" fill="none" />
            <path d="M0 12H46M0 34H46M14 0V46M32 0V46" stroke="var(--map-line)" strokeWidth="1.5" />
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#streets)" />
        <path d="M-10 80 Q120 60 200 110 T420 100" stroke="var(--map-route)" strokeWidth="7" fill="none" />
      </svg>
      <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -100%)' }}>
        <div style={{ filter: 'drop-shadow(0 6px 8px rgba(0,0,0,0.5))' }}>
          <Icon name="pin" size={38} color="var(--accent)" stroke={2} />
        </div>
      </div>
      <div style={{ position: 'absolute', bottom: 10, left: 12, fontSize: 12, fontWeight: 600, color: 'var(--map-chip-text)', background: 'var(--map-chip-bg)', padding: '5px 10px', borderRadius: 9, backdropFilter: 'blur(4px)', boxShadow: '0 1px 4px rgba(0,0,0,0.18)' }}>
        Toca para ajustar el pin
      </div>
    </div>
  );
}

// ── 8 · Crear Mercado ──────────────────────────────────────────────
function CrearMercadoScreen() {
  return (
    <Phone>
      <AppBar title="Nuevo mercado" back actions={<button style={{ width: 40, height: 40, display: 'grid', placeItems: 'center', borderRadius: 12 }}><Icon name="close" /></button>} />
      <Body pad={false}>
        <div style={{ padding: '8px 20px 24px', display: 'flex', flexDirection: 'column', gap: 18 }}>
          <Field label="Nombre del mercado" required value="" placeholder="Ej. Mercado de Coche" />
          <Field label="Dirección" value="" placeholder="Av. Intercomunal, Coche" multiline />
          <div>
            <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-2)', marginBottom: 9 }}>Ubicación</div>
            <MapPicker />
          </div>
          <div>
            <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-2)', marginBottom: 9 }}>Foto <span style={{ color: 'var(--text-4)', fontWeight: 400 }}>(opcional)</span></div>
            <div style={{ height: 96, borderRadius: 16, background: 'var(--surface-2)', boxShadow: 'inset 0 0 0 1.5px var(--border-2)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 7 }}>
              <Icon name="camera" size={26} color="var(--accent)" />
              <span style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-2)' }}>Agregar foto</span>
            </div>
          </div>
        </div>
      </Body>
      <BottomBar><Btn>Guardar mercado</Btn></BottomBar>
    </Phone>
  );
}

// ── 9 · Crear Cliente ──────────────────────────────────────────────
function CrearClienteScreen() {
  return (
    <Phone>
      <AppBar title="Nuevo cliente" back actions={<button style={{ width: 40, height: 40, display: 'grid', placeItems: 'center', borderRadius: 12 }}><Icon name="close" /></button>} />
      <Body pad={false}>
        <div style={{ padding: '8px 20px 24px', display: 'flex', flexDirection: 'column', gap: 18 }}>
          {/* circular photo */}
          <div style={{ display: 'flex', justifyContent: 'center' }}>
            <div style={{ width: 96, height: 96, borderRadius: '50%', background: 'var(--surface-2)', boxShadow: 'inset 0 0 0 1.5px var(--border-2)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 5, position: 'relative' }}>
              <Icon name="user" size={30} color="var(--text-3)" />
              <div style={{ position: 'absolute', bottom: 2, right: 2, width: 32, height: 32, borderRadius: '50%', background: 'var(--accent)', display: 'grid', placeItems: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.4)' }}>
                <Icon name="camera" size={16} color="#fff" />
              </div>
            </div>
          </div>

          <Field label="Nombre" required value="" placeholder="Nombre y apellido" />
          <Field label="Descripción" required value="" placeholder="Puesto 14 · verduras" hint="Importante — identifica el puesto del cliente dentro del mercado." />

          {/* phone list */}
          <div>
            <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-2)', marginBottom: 9 }}>Teléfonos</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 9 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, height: 52, background: 'var(--surface-2)', borderRadius: 14, padding: '0 8px 0 16px', boxShadow: 'inset 0 0 0 1px var(--border-2)' }}>
                <Icon name="phone" size={17} color="var(--text-3)" />
                <span className="mono" style={{ flex: 1, fontSize: 15.5, fontWeight: 500 }}>0414-2230198</span>
                <button style={{ width: 34, height: 34, borderRadius: '50%', background: 'var(--surface-3)', display: 'grid', placeItems: 'center' }}><Icon name="close" size={15} color="var(--text-2)" /></button>
              </div>
              <button style={{ display: 'flex', alignItems: 'center', gap: 8, height: 48, borderRadius: 14, boxShadow: 'inset 0 0 0 1.5px var(--border-2)', color: 'var(--accent)', fontSize: 14.5, fontWeight: 600, justifyContent: 'center' }}>
                <Icon name="plus" size={18} color="var(--accent)" /> Agregar otro teléfono
              </button>
            </div>
          </div>

          <div>
            <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-2)', marginBottom: 9 }}>Ubicación en el mercado</div>
            <MapPicker height={130} />
          </div>
        </div>
      </Body>
      <BottomBar><Btn>Guardar cliente</Btn></BottomBar>
    </Phone>
  );
}

// ── 6 · Saldo Extra ────────────────────────────────────────────────
function SaldoExtraScreen() {
  return (
    <Phone>
      <AppBar title="Agregar saldo extra" subtitle="Ana Rodríguez" back actions={<button style={{ width: 40, height: 40, display: 'grid', placeItems: 'center', borderRadius: 12 }}><Icon name="close" /></button>} />
      <Body pad={false}>
        <div style={{ padding: '8px 20px 24px', display: 'flex', flexDirection: 'column', gap: 18 }}>
          {/* locked category */}
          <div>
            <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-2)', marginBottom: 9 }}>Categoría</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 11, height: 52, background: 'var(--surface)', borderRadius: 14, padding: '0 16px', boxShadow: 'inset 0 0 0 1px var(--border)' }}>
              <div style={{ width: 30, height: 30, borderRadius: 9, background: 'var(--amber-tint)', display: 'grid', placeItems: 'center' }}><Icon name="tag" size={17} color="var(--amber-text)" /></div>
              <span style={{ flex: 1, fontSize: 15.5, fontWeight: 600 }}>Saldo</span>
              <span style={{ fontSize: 11.5, color: 'var(--text-3)', display: 'flex', alignItems: 'center', gap: 5 }}><Icon name="check" size={14} color="var(--text-3)" /> Fijo</span>
            </div>
          </div>

          {/* amount — hero input */}
          <div style={{ textAlign: 'center', padding: '8px 0 4px' }}>
            <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-2)', marginBottom: 10 }}>Monto</div>
            <div style={{ display: 'inline-flex', alignItems: 'baseline', gap: 8 }}>
              <span style={{ fontSize: 22, color: 'var(--text-2)', fontWeight: 600 }}>Bs.</span>
              <span className="mono" style={{ fontSize: 46, fontWeight: 700, letterSpacing: -1.5 }}>60,00</span>
            </div>
            <div style={{ height: 2, background: 'var(--accent)', width: 180, margin: '6px auto 0', borderRadius: 2 }} />
          </div>

          <Field label="Descripción" required value="Envases retornables no devueltos" placeholder="Explica de qué se trata este saldo" multiline />
          <Field label="Fecha" value="3 de junio de 2026" />
        </div>
      </Body>
      <BottomBar><Btn>Registrar saldo</Btn></BottomBar>
    </Phone>
  );
}

// ── 7 · Lista Negra (global) ───────────────────────────────────────
function BlacklistRow({ b }) {
  return (
    <div style={{ display: 'flex', gap: 13, padding: '14px 20px' }}>
      <Avatar name={b.name} size={46} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 10 }}>
          <span style={{ fontSize: 15.5, fontWeight: 600 }}>{b.name}</span>
          <span className="mono" style={{ fontSize: 15, fontWeight: 700, color: 'var(--red-text)', flexShrink: 0 }}>{money(b.balance, false)}</span>
        </div>
        <div style={{ fontSize: 12.5, color: 'var(--text-2)', marginTop: 2 }}>{b.mercado}</div>
        <div style={{ fontSize: 12.5, color: 'var(--text-3)', marginTop: 7, lineHeight: 1.4, display: 'flex', gap: 7 }}>
          <Icon name="ban" size={14} color="var(--red-text)" style={{ flexShrink: 0, marginTop: 1 }} />
          <span style={{ flex: 1 }}>{b.reason}</span>
        </div>
        <div style={{ fontSize: 11.5, color: 'var(--text-4)', marginTop: 5 }}>Agregado el {b.date}</div>
      </div>
    </div>
  );
}

function ListaNegraScreen() {
  return (
    <Phone>
      <AppBar title="Lista Negra" subtitle="Todos los mercados · solo lectura" back actions={<BarIcon name="search" />} />
      <Body pad={false}>
        <div style={{ margin: '12px 20px 6px', padding: '12px 16px', background: 'var(--red-tint)', borderRadius: 13, display: 'flex', gap: 11, boxShadow: 'inset 0 0 0 1px rgba(240,90,80,0.20)' }}>
          <Icon name="ban" size={20} color="var(--red-text)" style={{ flexShrink: 0, marginTop: 1 }} />
          <div>
            <div style={{ fontSize: 13.5, fontWeight: 700, color: 'var(--red-text)' }}>3 clientes vetados</div>
            <div style={{ fontSize: 12.5, color: 'var(--text-2)', marginTop: 2 }}>Saldo total irrecuperable · {money(883.50)}</div>
          </div>
        </div>
        {BLACKLIST.map((b, i) => (
          <React.Fragment key={b.id}>
            {i > 0 && <div style={{ height: 1, background: 'var(--border)', marginLeft: 79 }} />}
            <BlacklistRow b={b} />
          </React.Fragment>
        ))}
      </Body>
    </Phone>
  );
}

// ── Flow · Agregar a Lista Negra ───────────────────────────────────
function AgregarListaNegraScreen() {
  const pend = PEDIDOS.filter(o => o.status !== 'paid');
  const auto = pend.reduce((s, o) => s + (o.status === 'partial' ? o.total - (o.paid || 0) : o.total), 0);
  return (
    <Phone>
      <AppBar title="Agregar a Lista Negra" subtitle="Ana Rodríguez" back />
      <Body pad={false}>
        <div style={{ padding: '12px 20px 0' }}>
          {/* pending summary */}
          <SectionLabel>Pedidos pendientes</SectionLabel>
          <div style={{ background: 'var(--surface-2)', borderRadius: 14, boxShadow: 'inset 0 0 0 1px var(--border)', overflow: 'hidden' }}>
            {pend.map((o, i) => (
              <React.Fragment key={o.id}>
                {i > 0 && <div style={{ height: 1, background: 'var(--border)' }} />}
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '11px 16px' }}>
                  <div>
                    <div style={{ fontSize: 13.5, fontWeight: 500 }}>{o.kind === 'saldo' ? 'Saldo extra' : `${o.items} productos`}</div>
                    <div style={{ fontSize: 11.5, color: 'var(--text-3)', marginTop: 1 }}>{o.date}</div>
                  </div>
                  <span className="mono" style={{ fontSize: 14, fontWeight: 600 }}>{money(o.status === 'partial' ? o.total - o.paid : o.total, false)}</span>
                </div>
              </React.Fragment>
            ))}
          </div>
        </div>

        {/* total mode */}
        <div style={{ padding: '22px 20px 0' }}>
          <SectionLabel>Total adeudado</SectionLabel>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <button style={{ display: 'flex', alignItems: 'center', gap: 13, padding: '14px 16px', borderRadius: 14, background: 'var(--accent-tint)', boxShadow: 'inset 0 0 0 1.5px var(--accent)', textAlign: 'left' }}>
              <span style={{ width: 20, height: 20, borderRadius: '50%', background: 'var(--accent)', display: 'grid', placeItems: 'center', flexShrink: 0 }}><Icon name="check" size={13} stroke={2.6} color="#fff" /></span>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 14.5, fontWeight: 600 }}>Calcular automáticamente</div>
                <div style={{ fontSize: 12, color: 'var(--text-2)', marginTop: 2 }}>Suma todos los pedidos pendientes</div>
              </div>
              <span className="mono" style={{ fontSize: 17, fontWeight: 700, color: 'var(--accent)' }}>{money(auto, false)}</span>
            </button>
            <button style={{ display: 'flex', alignItems: 'center', gap: 13, padding: '14px 16px', borderRadius: 14, background: 'var(--surface-2)', boxShadow: 'inset 0 0 0 1px var(--border)', textAlign: 'left' }}>
              <span style={{ width: 20, height: 20, borderRadius: '50%', boxShadow: 'inset 0 0 0 1.6px var(--border-3)', flexShrink: 0 }} />
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 14.5, fontWeight: 600 }}>Ingresar manualmente</div>
                <div style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 2 }}>Escribe un monto distinto</div>
              </div>
            </button>
          </div>
        </div>

        <div style={{ padding: '22px 20px 8px' }}>
          <Field label="Motivo del veto" required value="Tres pedidos vencidos sin respuesta" placeholder="Explica por qué se agrega a la lista" multiline />
        </div>
      </Body>
      <BottomBar><Btn variant="danger" icon="ban">Confirmar y agregar a Lista Negra</Btn></BottomBar>
    </Phone>
  );
}

Object.assign(window, { CrearMercadoScreen, CrearClienteScreen, SaldoExtraScreen, ListaNegraScreen, AgregarListaNegraScreen, MapPicker, BlacklistRow });
