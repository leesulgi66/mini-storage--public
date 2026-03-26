import { useEffect, useState } from 'react';
import Sidebar from '../components/Sidebar';
import api from '../api/axios';

const PAGE_SIZE = 10;

function formatDate(str) {
  if (!str) return '-';
  return new Date(str).toLocaleDateString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  });
}

export default function Logs() {
  const [allLogs, setAllLogs] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/v1/audit-logs?limit=200')
      .then(res => setAllLogs(res.data || []))
      .catch(e => console.error(e))
      .finally(() => setLoading(false));
  }, []);

  const totalPages = Math.ceil(allLogs.length / PAGE_SIZE);
  const items = allLogs.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#f5f5f3', fontFamily: "'DM Sans', sans-serif" }}>
      <Sidebar />
      <main style={{ marginLeft: 220, flex: 1, padding: '2rem' }}>
        <div style={{ marginBottom: '1.75rem' }}>
          <h1 style={{ fontSize: 20, fontWeight: 500, color: '#111', letterSpacing: '-0.4px' }}>접속 로그</h1>
          <p style={{ fontSize: 13, color: '#999', marginTop: 3 }}>관리자 접속 기록을 확인하세요</p>
        </div>

        <div style={{ background: '#fff', border: '1px solid #e8e8e6', borderRadius: 10, padding: '1.25rem' }}>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '4rem', color: '#bbb', fontSize: 14 }}>불러오는 중...</div>
          ) : items.length ? (
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
              <thead>
                <tr>
                  {['#', '시간', '액션', '대상', 'IP'].map(h => (
                    <th key={h} style={{ textAlign: 'left', padding: '8px 12px', fontSize: 11, color: '#999', fontWeight: 500, borderBottom: '1px solid #e8e8e6' }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {items.map(log => (
                  <tr key={log.id}
                    onMouseEnter={e => e.currentTarget.style.background = '#fafaf8'}
                    onMouseLeave={e => e.currentTarget.style.background = ''}>
                    <td style={tdStyle}>{log.id}</td>
                    <td style={tdStyle}>{formatDate(log.createdAt)}</td>
                    <td style={tdStyle}>
                      <span style={{
                        display: 'inline-block', fontSize: 11, padding: '2px 8px', borderRadius: 4,
                        background: log.action === 'auth.login' ? '#eaf3de' : '#f0f0ee',
                        color: log.action === 'auth.login' ? '#3b6d11' : '#666'
                      }}>{log.action}</span>
                    </td>
                    <td style={tdStyle}>{log.target || '-'}</td>
                    <td style={tdStyle}>{log.ip}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div style={{ textAlign: 'center', padding: '4rem', color: '#bbb', fontSize: 14 }}>로그가 없습니다</div>
          )}

          {/* 페이지네이션 */}
          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', gap: 6, marginTop: '1.5rem' }}>
              <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage === 1}
                style={pageBtnStyle(currentPage === 1)}>이전</button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(i => (
                <button key={i} onClick={() => setCurrentPage(i)} style={pageBtnStyle(false, i === currentPage)}>{i}</button>
              ))}
              <button onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages}
                style={pageBtnStyle(currentPage === totalPages)}>다음</button>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

const tdStyle = { padding: '10px 12px', borderBottom: '1px solid #f0f0ee', color: '#333' };
const pageBtnStyle = (disabled, active) => ({
  padding: '6px 12px', fontSize: 13,
  border: `1px solid ${active ? '#111' : '#e0e0de'}`,
  borderRadius: 6, background: active ? '#111' : '#fff',
  color: disabled ? '#ccc' : active ? '#fff' : '#555',
  cursor: disabled ? 'not-allowed' : 'pointer', fontFamily: 'inherit'
});