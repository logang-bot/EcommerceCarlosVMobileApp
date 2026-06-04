/* Screens — Productos: Catálogo, Crear / Editar Producto */

// ── 10 · Catálogo ──────────────────────────────────────────────────
function ProductListRow({ p }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 13, padding: '11px 20px', background: 'var(--bg)' }}>
      <div style={{ width: 50, height: 50, borderRadius: 12, flexShrink: 0, background: `hsl(${({bottle:210,bag:40,box:25,can:350,jar:150,block:280})[p.kind]} var(--tile-s,22%) var(--tile-l,18%))`, display: 'grid', placeItems: 'center' }}>
        <ProductGlyph kind={p.kind} size={28} />
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 15, fontWeight: 600, letterSpacing: -0.2 }}>{p.name}</div>
        <div style={{ fontSize: 12.5, color: 'var(--text-2)', marginTop: 2 }}>{p.desc}</div>
      </div>
      <div className="mono" style={{ fontSize: 15, fontWeight: 600, flexShrink: 0 }}>{money(p.price, false)}</div>
      <Icon name="chevron" size={17} color="var(--text-4)" style={{ flexShrink: 0, marginLeft: -2 }} />
    </div>
  );
}

function CatalogoScreen() {
  const sorted = [...PRODUCTS].sort((a, b) => a.name.localeCompare(b.name));
  return (
    <Phone>
      <AppBar title="Productos" subtitle="12 productos en catálogo" large actions={<BarIcon name="search" />} />
      <Body pad={false}>
        <div style={{ padding: '0 16px 10px' }}>
          <div style={{ height: 44, borderRadius: 13, background: 'var(--surface-2)', boxShadow: 'inset 0 0 0 1px var(--border)', display: 'flex', alignItems: 'center', gap: 10, padding: '0 14px' }}>
            <Icon name="search" size={19} color="var(--text-3)" />
            <span style={{ flex: 1, fontSize: 15, color: 'var(--text-3)' }}>Buscar en el catálogo</span>
          </div>
        </div>
        {sorted.map((p, i) => (
          <React.Fragment key={p.id}>
            {i > 0 && <div style={{ height: 1, background: 'var(--border)', marginLeft: 83 }} />}
            <ProductListRow p={p} />
          </React.Fragment>
        ))}
      </Body>
      <FAB label="Producto" />
    </Phone>
  );
}

// ── 11 · Crear / Editar Producto ───────────────────────────────────
function ProductoFormScreen({ edit = false }) {
  return (
    <Phone>
      <AppBar title={edit ? 'Editar producto' : 'Nuevo producto'} back actions={<button style={{ width: 40, height: 40, display: 'grid', placeItems: 'center', borderRadius: 12 }}><Icon name="close" /></button>} />
      <Body pad={false}>
        <div style={{ padding: '8px 20px 24px', display: 'flex', flexDirection: 'column', gap: 18 }}>
          {/* photo picker */}
          <div style={{ display: 'flex', justifyContent: 'center' }}>
            {edit ? (
              <div style={{ width: 132, height: 132, borderRadius: 22, background: `hsl(25 var(--tile-s,22%) var(--tile-l,18%))`, display: 'grid', placeItems: 'center', position: 'relative', boxShadow: 'inset 0 0 0 1px var(--border-2)' }}>
                <ProductGlyph kind="box" size={56} color="var(--glyph)" />
                <div style={{ position: 'absolute', bottom: 8, right: 8, width: 34, height: 34, borderRadius: 11, background: 'var(--accent)', display: 'grid', placeItems: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.4)' }}>
                  <Icon name="camera" size={18} color="#fff" />
                </div>
              </div>
            ) : (
              <div style={{ width: 132, height: 132, borderRadius: 22, background: 'var(--surface-2)', boxShadow: 'inset 0 0 0 1.5px var(--border-2)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 9 }}>
                <Icon name="camera" size={28} color="var(--accent)" />
                <span style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-2)' }}>Agregar foto</span>
              </div>
            )}
          </div>
          <div style={{ textAlign: 'center', fontSize: 12, color: 'var(--text-4)', marginTop: -8 }}>Toca para elegir de la galería o cámara</div>

          <Field label="Nombre" required value={edit ? 'Café Madrid 250g' : ''} placeholder="Ej. Harina PAN 1kg" />
          <Field label="Descripción" value={edit ? 'Molido · tueste medio' : ''} placeholder="Variante, tamaño o nota" hint="Opcional — ayuda a identificar el producto." />
          <Field label="Precio" required prefix="Bs." value={edit ? '5,20' : ''} placeholder="0,00" />
        </div>
      </Body>
      <BottomBar>
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 10 }}>
          <Btn>Guardar</Btn>
          {edit && <Btn variant="danger" icon="trash">Eliminar producto</Btn>}
        </div>
      </BottomBar>
    </Phone>
  );
}

function CrearProductoScreen() { return <ProductoFormScreen edit={false} />; }
function EditarProductoScreen() { return <ProductoFormScreen edit={true} />; }

Object.assign(window, { CatalogoScreen, CrearProductoScreen, EditarProductoScreen, ProductListRow });
