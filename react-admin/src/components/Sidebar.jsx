import { NavLink, useNavigate } from 'react-router-dom';

const navItems = [
  {
    to: '/dashboard', label: '대시보드',
    icon: <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" width="16" height="16"><rect x="1" y="1" width="6" height="6" rx="1"/><rect x="9" y="1" width="6" height="6" rx="1"/><rect x="1" y="9" width="6" height="6" rx="1"/><rect x="9" y="9" width="6" height="6" rx="1"/></svg>
  },
  {
    to: '/logs', label: '접속 로그',
    icon: <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" width="16" height="16"><path d="M2 4h12M2 8h12M2 12h8"/></svg>
  },
  {
    to: '/images', label: '이미지 관리',
    icon: <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" width="16" height="16"><rect x="1" y="3" width="14" height="10" rx="1.5"/><circle cx="5.5" cy="7.5" r="1.5"/><path d="M15 11l-4-4-6 6"/></svg>
  },
  {
    to: '/upload', label: '업로드',
    icon: <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" width="16" height="16"><path d="M8 11V3M5 6l3-3 3 3"/><path d="M2 13h12"/></svg>
  },
];

export default function Sidebar() {
  const navigate = useNavigate();

  const token = localStorage.getItem('accessToken');
  let email = '';
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    email = payload.email || '';
  } catch(e) {}

  function logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('role');
    navigate('/login');
  }

  return (
    <aside style={{
      width: 220, background: '#fff', borderRight: '1px solid #e8e8e6',
      padding: '1.5rem 1rem', display: 'flex', flexDirection: 'column',
      position: 'fixed', top: 0, left: 0, height: '100vh', 
      fontFamily: "'DM Sans', sans-serif",
      boxSizing: 'border-box',
      overflowY: 'auto'
    }}>
      {/* 로고 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '0 0.5rem', marginBottom: '2rem' }}>
        <div style={{ width: 30, height: 30, background: '#111', borderRadius: 6, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <svg viewBox="0 0 18 18" fill="none" width="15" height="15">
            <rect x="2" y="2" width="6" height="6" rx="1.5" fill="white"/>
            <rect x="10" y="2" width="6" height="6" rx="1.5" fill="white" opacity="0.5"/>
            <rect x="2" y="10" width="6" height="6" rx="1.5" fill="white" opacity="0.5"/>
            <rect x="10" y="10" width="6" height="6" rx="1.5" fill="white" opacity="0.3"/>
          </svg>
        </div>
        <div>
          <div style={{ fontSize: 14, fontWeight: 500, color: '#111', letterSpacing: '-0.3px' }}>mini-storage</div>
          <div style={{ fontSize: 11, color: '#999' }}>admin console</div>
        </div>
      </div>

      {/* 메뉴 */}
      <div style={{ fontSize: 11, color: '#bbb', letterSpacing: '0.5px', textTransform: 'uppercase', padding: '0 0.5rem', marginBottom: '0.5rem' }}>메뉴</div>
      {navItems.map(item => (
        <NavLink key={item.to} to={item.to} style={({ isActive }) => ({
          display: 'flex', alignItems: 'center', gap: 8,
          padding: '8px 10px', borderRadius: 7, fontSize: 14,
          color: isActive ? '#fff' : '#555',
          background: isActive ? '#111' : 'transparent',
          marginBottom: 2, textDecoration: 'none'
        })}>
          {item.icon}
          {item.label}
        </NavLink>
      ))}

      <div style={{ flex: 1 }} />

      {/* 유저 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 10px' }}>
        <div style={{ width: 28, height: 28, background: '#e8e8e6', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 11, fontWeight: 500, color: '#555' }}>
          {email.substring(0, 2).toUpperCase()}
        </div>
        <div>
          <div style={{ fontSize: 13, fontWeight: 500, color: '#111' }}>{email.split('@')[0]}</div>
          <div style={{ fontSize: 11, color: '#999' }}>관리자</div>
        </div>
      </div>
      <button onClick={logout} style={{ width: '100%', marginTop: '0.5rem', padding: '8px 10px', fontSize: 13, color: '#999', background: 'none', border: 'none', cursor: 'pointer', textAlign: 'left', fontFamily: 'inherit', borderRadius: 7 }}>
        로그아웃
      </button>
    </aside>
  );
}