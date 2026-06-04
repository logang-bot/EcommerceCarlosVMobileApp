/* ──────────────────────────────────────────────────────────────────
   Pedidos & Cuentas — shared UI kit (device chrome + primitives)
   Exports to window. Loaded as a Babel script.
   ────────────────────────────────────────────────────────────────── */

// ── helpers ────────────────────────────────────────────────────────
const money = (n, withUnit = true) => {
  const s = Number(n).toLocaleString('es-VE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  return withUnit ? `Bs. ${s}` : s;
};

const STATUS = {
  ok:   { label: 'Al día',      color: 'var(--green-text)', tint: 'var(--green-tint)', dot: 'var(--green)' },
  warn: { label: 'Advertencia', color: 'var(--amber-text)', tint: 'var(--amber-tint)', dot: 'var(--amber)' },
  crit: { label: 'Crítico',     color: 'var(--red-text)',   tint: 'var(--red-tint)',   dot: 'var(--red)' },
};

const PAY = {
  paid:    { label: 'Pagado',    color: 'var(--green-text)', tint: 'var(--green-tint)' },
  pending: { label: 'Pendiente', color: 'var(--amber-text)', tint: 'var(--amber-tint)' },
  partial: { label: 'Parcial',   color: 'var(--accent)',     tint: 'var(--accent-tint)' },
};

// ── icons (minimal line set) ───────────────────────────────────────
function Icon({ name, size = 22, stroke = 1.7, color = 'currentColor', style }) {
  const p = { fill: 'none', stroke: color, strokeWidth: stroke, strokeLinecap: 'round', strokeLinejoin: 'round' };
  const paths = {
    back:    <path d="M15 5l-7 7 7 7" {...p} />,
    search:  <g {...p}><circle cx="11" cy="11" r="7" /><path d="M20 20l-3.5-3.5" /></g>,
    plus:    <path d="M12 5v14M5 12h14" {...p} />,
    minus:   <path d="M5 12h14" {...p} />,
    close:   <path d="M6 6l12 12M18 6L6 18" {...p} />,
    phone:   <path d="M5 4h3l2 5-2.5 1.5a11 11 0 005 5L14 13l5 2v3a2 2 0 01-2 2A14 14 0 014 6a2 2 0 012-2z" {...p} />,
    pin:     <g {...p}><path d="M12 21s7-5.2 7-11a7 7 0 10-14 0c0 5.8 7 11 7 11z" /><circle cx="12" cy="10" r="2.5" /></g>,
    chevron: <path d="M9 6l6 6-6 6" {...p} />,
    dots:    <g fill={color} stroke="none"><circle cx="5" cy="12" r="1.6" /><circle cx="12" cy="12" r="1.6" /><circle cx="19" cy="12" r="1.6" /></g>,
    edit:    <path d="M4 20h4L18 10l-4-4L4 16v4zM14 6l4 4" {...p} />,
    trash:   <g {...p}><path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13" /></g>,
    check:   <path d="M5 12.5l4.5 4.5L19 7" {...p} />,
    ban:     <g {...p}><circle cx="12" cy="12" r="8.5" /><path d="M6 6l12 12" /></g>,
    user:    <g {...p}><circle cx="12" cy="8" r="3.5" /><path d="M5 20c1.5-3.5 4-5 7-5s5.5 1.5 7 5" /></g>,
    camera:  <g {...p}><path d="M4 8h3l1.5-2h7L17 8h3v11H4z" /><circle cx="12" cy="13" r="3.2" /></g>,
    doc:     <g {...p}><path d="M7 3h7l5 5v13H7z" /><path d="M14 3v5h5" /></g>,
    receipt: <g {...p}><path d="M6 3h12v18l-2-1.3-2 1.3-2-1.3-2 1.3-2-1.3L6 21z" /><path d="M9 8h6M9 12h6" /></g>,
    fingerprint: <g {...p}><path d="M6 11a6 6 0 0112 0v2M9 12a3 3 0 016 0v3M12 12v5M9 16v2M15 15v3" /></g>,
    chart:   <g {...p}><path d="M4 20V10M10 20V4M16 20v-7M22 20H2" /></g>,
    cart:    <g {...p}><circle cx="9" cy="20" r="1.4" /><circle cx="18" cy="20" r="1.4" /><path d="M3 4h2l2.5 12h11l2-8H6" /></g>,
    filter:  <path d="M4 6h16M7 12h10M10 18h4" {...p} />,
    sort:    <g {...p}><path d="M7 4v16M7 20l-3-3M7 20l3-3" /><path d="M14 7h6M14 12h4M14 17h2" /></g>,
    bell:    <g {...p}><path d="M6 9a6 6 0 0112 0c0 5 2 6 2 6H4s2-1 2-6z" /><path d="M10 20a2 2 0 004 0" /></g>,
    money:   <g {...p}><rect x="3" y="6" width="18" height="12" rx="2" /><circle cx="12" cy="12" r="2.5" /></g>,
    tag:     <g {...p}><path d="M3 12l8-8h7v7l-8 8z" /><circle cx="14.5" cy="9.5" r="1.2" fill={color} stroke="none" /></g>,
    grid:    <g {...p}><rect x="4" y="4" width="6" height="6" rx="1" /><rect x="14" y="4" width="6" height="6" rx="1" /><rect x="4" y="14" width="6" height="6" rx="1" /><rect x="14" y="14" width="6" height="6" rx="1" /></g>,
  };
  return <svg width={size} height={size} viewBox="0 0 24 24" style={style}>{paths[name]}</svg>;
}

// ── product placeholder glyphs (line art on tinted tile) ────────────
function ProductGlyph({ kind = 'box', size = 34, color = 'var(--glyph)' }) {
  const p = { fill: 'none', stroke: color, strokeWidth: 1.5, strokeLinecap: 'round', strokeLinejoin: 'round' };
  const g = {
    bottle: <g {...p}><path d="M13 3h6v4l2 4v17a2 2 0 01-2 2h-6a2 2 0 01-2-2V11l2-4z" transform="translate(-4 -1)" /><path d="M8 13h8" /></g>,
    bag:    <g {...p}><path d="M7 9l2-4h6l2 4v15a1 1 0 01-1 1H8a1 1 0 01-1-1z" /><path d="M9 5l1.5 4M15 5l-1.5 4" /></g>,
    box:    <g {...p}><path d="M5 8l7-4 7 4v9l-7 4-7-4z" /><path d="M5 8l7 4 7-4M12 12v9" /></g>,
    can:    <g {...p}><ellipse cx="12" cy="6" rx="6" ry="2.2" /><path d="M6 6v13c0 1.2 2.7 2.2 6 2.2s6-1 6-2.2V6" /><path d="M6 11c0 1.2 2.7 2.2 6 2.2s6-1 6-2.2" /></g>,
    jar:    <g {...p}><path d="M8 4h8v3l1 1v12a1 1 0 01-1 1H8a1 1 0 01-1-1V8l1-1z" /><path d="M9 4V3h6v1" /></g>,
    block:  <g {...p}><rect x="5" y="7" width="14" height="11" rx="1.5" /><path d="M5 11h14" /></g>,
  };
  return <svg width={size} height={size} viewBox="0 0 24 24">{g[kind] || g.box}</svg>;
}

// ── avatar (initials on tinted bg) ─────────────────────────────────
function Avatar({ name = '', size = 44, status }) {
  const initials = name.split(' ').filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase();
  let hue = 0; for (const c of name) hue = (hue * 31 + c.charCodeAt(0)) % 360;
  const ring = status ? STATUS[status].dot : 'transparent';
  return (
    <div style={{ position: 'relative', width: size, height: size, flexShrink: 0 }}>
      <div style={{
        width: size, height: size, borderRadius: '50%',
        background: `hsl(${hue} 32% var(--avatar-l, 26%))`, color: `hsl(${hue} 55% var(--avatar-text-l, 78%))`,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: size * 0.36, fontWeight: 600, letterSpacing: 0.3,
        boxShadow: status ? `0 0 0 2px var(--bg), 0 0 0 4px ${ring}` : 'none',
      }}>{initials}</div>
    </div>
  );
}

// ── status / payment badges ─────────────────────────────────────────
function StatusBadge({ status, size = 'md' }) {
  const s = STATUS[status]; if (!s) return null;
  const sm = size === 'sm';
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: sm ? 5 : 6,
      padding: sm ? '3px 8px 3px 7px' : '5px 11px 5px 9px',
      borderRadius: 999, background: s.tint, color: s.color,
      fontSize: sm ? 11.5 : 13, fontWeight: 600, lineHeight: 1, whiteSpace: 'nowrap',
    }}>
      <span style={{ width: sm ? 5 : 6, height: sm ? 5 : 6, borderRadius: '50%', background: s.dot }} />
      {s.label}
    </span>
  );
}

function PayChip({ status }) {
  const s = PAY[status]; if (!s) return null;
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', padding: '3px 9px', borderRadius: 999,
      background: s.tint, color: s.color, fontSize: 11.5, fontWeight: 600, lineHeight: 1.2, whiteSpace: 'nowrap',
    }}>{s.label}</span>
  );
}

// ── Android device chrome ──────────────────────────────────────────
function StatusBar({ dark = true }) {
  const c = 'var(--text)';
  return (
    <div style={{
      height: 34, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '0 20px', fontSize: 13.5, fontWeight: 600, color: c, letterSpacing: 0.2,
    }}>
      <span className="mono">9:41</span>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        {/* signal */}
        <svg width="17" height="12" viewBox="0 0 17 12" fill={c}><rect x="0" y="8" width="3" height="4" rx="1"/><rect x="4.7" y="5.5" width="3" height="6.5" rx="1"/><rect x="9.4" y="3" width="3" height="9" rx="1"/><rect x="14" y="0.5" width="3" height="11.5" rx="1"/></svg>
        {/* wifi */}
        <svg width="16" height="12" viewBox="0 0 16 12" fill="none" stroke={c} strokeWidth="1.6" strokeLinecap="round"><path d="M1.5 4.5a10 10 0 0113 0M4 7a6.3 6.3 0 018 0"/><circle cx="8" cy="10" r="0.9" fill={c} stroke="none"/></svg>
        {/* battery */}
        <svg width="25" height="12" viewBox="0 0 25 12" fill="none"><rect x="0.6" y="0.6" width="21" height="10.8" rx="2.6" stroke={c} strokeOpacity="0.45"/><rect x="2.4" y="2.4" width="14" height="7.2" rx="1.4" fill={c}/><rect x="23" y="3.8" width="1.6" height="4.4" rx="0.8" fill={c} fillOpacity="0.45"/></svg>
      </div>
    </div>
  );
}

function GestureNav() {
  return (
    <div style={{ height: 24, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--bg)' }}>
      <div style={{ width: 132, height: 4.5, borderRadius: 3, background: 'var(--border-3)' }} />
    </div>
  );
}

// ── App bar ────────────────────────────────────────────────────────
function AppBar({ title, subtitle, back = false, actions = null, large = false, onTitleNote }) {
  return (
    <div style={{ flexShrink: 0, background: 'var(--bg)', borderBottom: large ? 'none' : '1px solid var(--border)' }}>
      <div style={{ height: 52, display: 'flex', alignItems: 'center', gap: 4, padding: '0 8px 0 6px' }}>
        {back && (
          <button style={{ width: 40, height: 40, display: 'grid', placeItems: 'center', borderRadius: 12 }}>
            <Icon name="back" size={22} />
          </button>
        )}
        {!large && (
          <div style={{ flex: 1, minWidth: 0, paddingLeft: back ? 2 : 12 }}>
            <div style={{ fontSize: 17, fontWeight: 600, letterSpacing: -0.2, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{title}</div>
            {subtitle && <div style={{ fontSize: 12, color: 'var(--text-2)', marginTop: 1 }}>{subtitle}</div>}
          </div>
        )}
        {large && <div style={{ flex: 1 }} />}
        <div style={{ display: 'flex', alignItems: 'center', gap: 2 }}>{actions}</div>
      </div>
      {large && (
        <div style={{ padding: '2px 20px 16px' }}>
          <div style={{ fontSize: 30, fontWeight: 700, letterSpacing: -0.8 }}>{title}</div>
          {subtitle && <div style={{ fontSize: 13.5, color: 'var(--text-2)', marginTop: 3 }}>{subtitle}</div>}
        </div>
      )}
    </div>
  );
}

function BarIcon({ name, badge }) {
  return (
    <button style={{ width: 40, height: 40, display: 'grid', placeItems: 'center', borderRadius: 12, position: 'relative', color: 'var(--text)' }}>
      <Icon name={name} size={21} />
      {badge && <span style={{ position: 'absolute', top: 8, right: 9, width: 7, height: 7, borderRadius: '50%', background: 'var(--red)', boxShadow: '0 0 0 2px var(--bg)' }} />}
    </button>
  );
}

// ── FAB ────────────────────────────────────────────────────────────
function FAB({ label, icon = 'plus', bottom = 22 }) {
  const extended = !!label;
  return (
    <div style={{ position: 'absolute', right: 18, bottom, zIndex: 20 }}>
      <div style={{
        height: 56, minWidth: 56, borderRadius: extended ? 18 : '50%',
        background: 'var(--accent)', color: '#fff',
        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 9,
        padding: extended ? '0 22px 0 18px' : 0,
        boxShadow: '0 8px 22px rgba(91,141,239,0.40), 0 2px 6px rgba(0,0,0,0.4)',
      }}>
        <Icon name={icon} size={24} stroke={2} color="#fff" />
        {extended && <span style={{ fontSize: 15.5, fontWeight: 600, letterSpacing: -0.1 }}>{label}</span>}
      </div>
    </div>
  );
}

// ── primary / secondary buttons ────────────────────────────────────
function Btn({ children, variant = 'primary', icon, full = true, disabled = false, size = 'lg' }) {
  const h = size === 'lg' ? 52 : 44;
  const base = {
    height: h, width: full ? '100%' : 'auto', padding: full ? 0 : '0 20px',
    borderRadius: 14, fontSize: 15.5, fontWeight: 600, letterSpacing: -0.1,
    display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 9, whiteSpace: 'nowrap',
  };
  const styles = {
    primary:   { background: disabled ? 'var(--surface-3)' : 'var(--accent)', color: disabled ? 'var(--text-3)' : '#fff', boxShadow: disabled ? 'none' : '0 2px 10px rgba(91,141,239,0.30)' },
    secondary: { background: 'var(--surface-3)', color: 'var(--text)', boxShadow: 'inset 0 0 0 1px var(--border-2)' },
    ghost:     { background: 'transparent', color: 'var(--text)', boxShadow: 'inset 0 0 0 1px var(--border-2)' },
    danger:    { background: 'var(--red-tint)', color: 'var(--red-text)', boxShadow: 'inset 0 0 0 1px rgba(240,90,80,0.25)' },
    success:   { background: 'var(--green)', color: 'var(--on-success)' },
  };
  return (
    <button style={{ ...base, ...styles[variant] }}>
      {icon && <Icon name={icon} size={20} color="currentColor" />}
      {children}
    </button>
  );
}

// ── form field ─────────────────────────────────────────────────────
function Field({ label, value, placeholder, hint, suffix, prefix, multiline, locked, required }) {
  return (
    <div>
      {label && (
        <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-2)', marginBottom: 8, display: 'flex', gap: 5 }}>
          {label}{required && <span style={{ color: 'var(--accent)' }}>*</span>}
        </div>
      )}
      <div style={{
        display: 'flex', alignItems: multiline ? 'flex-start' : 'center', gap: 8,
        minHeight: multiline ? 88 : 52, padding: multiline ? '14px 16px' : '0 16px',
        background: locked ? 'var(--surface)' : 'var(--surface-2)', borderRadius: 14,
        boxShadow: `inset 0 0 0 1px ${value ? 'var(--border-2)' : 'var(--border)'}`,
      }}>
        {prefix && <span style={{ fontSize: 15, color: 'var(--text-2)', fontWeight: 500 }}>{prefix}</span>}
        <span style={{ flex: 1, fontSize: 15.5, color: value ? 'var(--text)' : 'var(--text-3)', fontWeight: value ? 500 : 400, lineHeight: 1.4 }}>
          {value || placeholder}
        </span>
        {locked && <Icon name="check" size={16} color="var(--text-3)" />}
        {suffix && <span style={{ fontSize: 14, color: 'var(--text-2)' }}>{suffix}</span>}
      </div>
      {hint && <div style={{ fontSize: 12.5, color: 'var(--text-3)', marginTop: 7, lineHeight: 1.4 }}>{hint}</div>}
    </div>
  );
}

// ── bottom action bar (sticky CTA) ─────────────────────────────────
function BottomBar({ children }) {
  return (
    <div style={{
      flexShrink: 0, padding: '12px 18px 10px', background: 'var(--bg)',
      borderTop: '1px solid var(--border)', display: 'flex', gap: 10,
    }}>{children}</div>
  );
}

// ── section label ──────────────────────────────────────────────────
function SectionLabel({ children, action }) {
  return (
    <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', padding: '0 20px', marginBottom: 10 }}>
      <span style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-3)', letterSpacing: 0.5, textTransform: 'uppercase' }}>{children}</span>
      {action && <span style={{ fontSize: 13, fontWeight: 500, color: 'var(--accent)' }}>{action}</span>}
    </div>
  );
}

// ── empty state ────────────────────────────────────────────────────
function EmptyState({ icon, title, subtitle, hint, compact }) {
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: compact ? '24px 36px' : '32px 44px' }}>
      <div style={{ width: compact ? 60 : 74, height: compact ? 60 : 74, borderRadius: compact ? 18 : 22, background: 'var(--surface-2)', boxShadow: 'inset 0 0 0 1px var(--border-2)', display: 'grid', placeItems: 'center', marginBottom: compact ? 14 : 18 }}>
        <Icon name={icon} size={compact ? 25 : 30} color="var(--text-3)" />
      </div>
      <div style={{ fontSize: compact ? 15.5 : 17, fontWeight: 700, letterSpacing: -0.3 }}>{title}</div>
      <div style={{ fontSize: 13.5, color: 'var(--text-2)', marginTop: 8, lineHeight: 1.5, maxWidth: 250 }}>{subtitle}</div>
      {hint && (
        <div style={{ marginTop: 16, display: 'inline-flex', alignItems: 'center', gap: 7, fontSize: 12.5, fontWeight: 600, color: 'var(--accent)', background: 'var(--accent-soft)', padding: '8px 13px', borderRadius: 10, whiteSpace: 'nowrap' }}>
          <Icon name="plus" size={15} color="var(--accent)" stroke={2.2} /> {hint}
        </div>
      )}
    </div>
  );
}

// ── Phone shell ────────────────────────────────────────────────────
function Phone({ children, statusbar = true, gesture = true }) {
  return (
    <div className="pedidos-screen">
      <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column' }}>
        {statusbar && <StatusBar />}
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: 0 }}>{children}</div>
        {gesture && <GestureNav />}
      </div>
    </div>
  );
}

// ── scroll body ────────────────────────────────────────────────────
function Body({ children, pad = true, style }) {
  return (
    <div className="pedidos-scroll" style={{ flex: 1, overflow: 'hidden', padding: pad ? '16px 0 24px' : 0, ...style }}>
      {children}
    </div>
  );
}

Object.assign(window, {
  money, STATUS, PAY, Icon, ProductGlyph, Avatar, StatusBadge, PayChip,
  StatusBar, GestureNav, AppBar, BarIcon, FAB, Btn, Field, BottomBar,
  SectionLabel, EmptyState, Phone, Body,
});
