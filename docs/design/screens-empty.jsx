/* Screens — empty states for list & search screens */

const EmptyWrap = ({ children }) => (
  <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>{children}</div>
);

// Mercados (Home) — no mercados
function MercadosEmptyScreen() {
  return (
    <Phone>
      <AppBar title="Mercados" subtitle="0 mercados" large actions={<><BarIcon name="search" /><BarIcon name="bell" /></>} />
      <EmptyWrap>
        <EmptyState icon="grid"
          title="Aún no hay mercados"
          subtitle="Crea tu primer mercado para empezar a registrar clientes y pedidos."
          hint="Nuevo mercado" />
      </EmptyWrap>
      <FAB label="Mercado" />
    </Phone>
  );
}

// Detalle de Mercado — no clientes
function DetalleMercadoEmptyScreen() {
  return (
    <Phone>
      <AppBar title="Mercado de Coche" subtitle="0 clientes" back actions={<><BarIcon name="search" /><BarIcon name="dots" /></>} />
      <EmptyWrap>
        <EmptyState icon="user"
          title="Sin clientes en este mercado"
          subtitle="Agrega el primer cliente para llevar el control de sus pedidos y saldos."
          hint="Nuevo cliente" />
      </EmptyWrap>
      <FAB label="Cliente" />
    </Phone>
  );
}

// Búsqueda global — no results
function BusquedaEmptyScreen() {
  return (
    <Phone>
      <div style={{ flexShrink: 0, padding: '8px 14px 12px', display: 'flex', alignItems: 'center', gap: 4 }}>
        <button style={{ width: 40, height: 40, display: 'grid', placeItems: 'center', borderRadius: 12 }}><Icon name="back" /></button>
        <div style={{ flex: 1, height: 44, borderRadius: 13, background: 'var(--surface-2)', boxShadow: 'inset 0 0 0 1.5px var(--accent)', display: 'flex', alignItems: 'center', gap: 10, padding: '0 14px' }}>
          <Icon name="search" size={19} color="var(--text-2)" />
          <span style={{ flex: 1, fontSize: 15.5, fontWeight: 500 }}>zerpa</span>
          <span style={{ width: 1.5, height: 20, background: 'var(--accent)' }} />
          <button style={{ width: 22, height: 22, borderRadius: '50%', background: 'var(--surface-3)', display: 'grid', placeItems: 'center' }}><Icon name="close" size={13} color="var(--text-2)" /></button>
        </div>
      </div>
      <EmptyWrap>
        <EmptyState icon="search"
          title="Sin resultados"
          subtitle="No encontramos clientes que coincidan con “zerpa”. Revisa el nombre o el teléfono." />
      </EmptyWrap>
    </Phone>
  );
}

// Catálogo — no productos
function CatalogoEmptyScreen() {
  return (
    <Phone>
      <AppBar title="Productos" subtitle="0 productos en catálogo" large actions={<BarIcon name="search" />} />
      <EmptyWrap>
        <EmptyState icon="tag"
          title="Catálogo vacío"
          subtitle="Crea productos con foto y precio para poder agregarlos a los pedidos."
          hint="Nuevo producto" />
      </EmptyWrap>
      <FAB label="Producto" />
    </Phone>
  );
}

// Lista Negra — empty
function ListaNegraEmptyScreen() {
  return (
    <Phone>
      <AppBar title="Lista Negra" subtitle="Todos los mercados · solo lectura" back actions={<BarIcon name="search" />} />
      <EmptyWrap>
        <EmptyState icon="ban"
          title="Lista negra vacía"
          subtitle="No hay clientes vetados. Cuando agregues uno desde su perfil aparecerá aquí." />
      </EmptyWrap>
    </Phone>
  );
}

Object.assign(window, {
  MercadosEmptyScreen, DetalleMercadoEmptyScreen, BusquedaEmptyScreen,
  CatalogoEmptyScreen, ListaNegraEmptyScreen,
});
