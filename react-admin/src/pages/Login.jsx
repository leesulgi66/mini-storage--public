import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [viewerLoading, setViewerLoading] = useState(false);

  async function handleLogin(e) {
    e?.preventDefault();
    if (!email || !password) { setError('이메일과 비밀번호를 입력해주세요'); return; }
    setLoading(true);
    setError('');
    try {
      const res = await api.post('/api/v1/auth/login', { email, password });
      localStorage.setItem('accessToken', res.data.accessToken);
      localStorage.setItem('refreshToken', res.data.refreshToken);
      localStorage.setItem('role', res.data.role);
      navigate('/dashboard');
    } catch(e) {
      setError(e.response?.data?.message || '로그인에 실패했습니다');
    } finally {
      setLoading(false);
    }
  }

  async function handleViewerLogin() {
    setViewerLoading(true);
    setError('');
    try {
      const res = await api.post('/api/v1/auth/login', {
        email: 'viewer@example.com',
        password: 'viewer1234'
      });
      localStorage.setItem('accessToken', res.data.accessToken);
      localStorage.setItem('refreshToken', res.data.refreshToken);
      localStorage.setItem('role', res.data.role);
      navigate('/dashboard');
    } catch(e) {
      setError('Viewer 로그인에 실패했습니다');
    } finally {
      setViewerLoading(false);
    }
  }

  return (
    <div style={{ minHeight: '100vh', background: '#f5f5f3', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: "'DM Sans', sans-serif" }}>
      <div style={{ background: '#fff', border: '1px solid #e8e8e6', borderRadius: 12, padding: '2.5rem', width: '100%', maxWidth: 400 }}>
        {/* 로고 */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: '2rem' }}>
          <div style={{ width: 36, height: 36, background: '#111', borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <svg viewBox="0 0 18 18" fill="none" width="18" height="18">
              <rect x="2" y="2" width="6" height="6" rx="1.5" fill="white"/>
              <rect x="10" y="2" width="6" height="6" rx="1.5" fill="white" opacity="0.5"/>
              <rect x="2" y="10" width="6" height="6" rx="1.5" fill="white" opacity="0.5"/>
              <rect x="10" y="10" width="6" height="6" rx="1.5" fill="white" opacity="0.3"/>
            </svg>
          </div>
          <div>
            <div style={{ fontSize: 15, fontWeight: 500, color: '#111' }}>mini-storage</div>
            <div style={{ fontSize: 12, color: '#888' }}>admin console</div>
          </div>
        </div>

        <h1 style={{ fontSize: 22, fontWeight: 500, color: '#111', letterSpacing: '-0.5px', marginBottom: '0.4rem' }}>로그인</h1>
        <p style={{ fontSize: 14, color: '#888', marginBottom: '2rem' }}>관리자 계정으로 로그인하세요</p>

        <div style={{ marginBottom: '1.25rem' }}>
          <label style={{ display: 'block', fontSize: 13, color: '#666', marginBottom: 6 }}>이메일</label>
          <input type="email" value={email} onChange={e => setEmail(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleLogin()}
            placeholder="admin@example.com"
            style={{ width: '100%', padding: '10px 12px', fontSize: 14, border: '1px solid #ddd', borderRadius: 8, outline: 'none', fontFamily: 'inherit', boxSizing: 'border-box' }} />
        </div>

        <div style={{ marginBottom: '1.25rem' }}>
          <label style={{ display: 'block', fontSize: 13, color: '#666', marginBottom: 6 }}>비밀번호</label>
          <input type="password" value={password} onChange={e => setPassword(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleLogin()}
            placeholder="••••••••"
            style={{ width: '100%', padding: '10px 12px', fontSize: 14, border: '1px solid #ddd', borderRadius: 8, outline: 'none', fontFamily: 'inherit', boxSizing: 'border-box' }} />
        </div>

        {error && <p style={{ fontSize: 13, color: '#d85a30', marginBottom: '0.75rem' }}>{error}</p>}

        <button onClick={handleLogin} disabled={loading}
          style={{ width: '100%', padding: 11, fontSize: 14, fontWeight: 500, background: loading ? '#999' : '#111', color: '#fff', border: 'none', borderRadius: 8, cursor: loading ? 'not-allowed' : 'pointer', fontFamily: 'inherit' }}>
          {loading ? '로그인 중...' : '로그인'}
        </button>

        <button onClick={handleViewerLogin} disabled={viewerLoading}
          style={{ width: '100%', marginTop: 8, padding: 11, fontSize: 14, fontWeight: 500, background: '#fff', color: '#555', border: '1px solid #ddd', borderRadius: 8, cursor: viewerLoading ? 'not-allowed' : 'pointer', fontFamily: 'inherit' }}>
          {viewerLoading ? '로그인 중...' : 'Viewer로 둘러보기'}
        </button>

        <hr style={{ border: 'none', borderTop: '1px solid #f0f0ee', margin: '1.75rem 0' }} />
        <p style={{ fontSize: 12, color: '#bbb', textAlign: 'center' }}>mini-storage v1.0.0</p>
      </div>
    </div>
  );
}