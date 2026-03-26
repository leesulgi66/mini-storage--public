import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import api from '../api/axios';

const BASE_URL = api.defaults.baseURL;

function formatBytes(bytes) {
  if (!bytes) return '0 B';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + ' MB';
  return (bytes / 1024 / 1024 / 1024).toFixed(2) + ' GB';
}

export default function Dashboard() {
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  async function loadDashboard() {
    try {
      const [dashRes, imgRes] = await Promise.all([
        api.get('/api/v1/dashboard'),
        api.get('/api/v1/images?limit=10')
      ]);
      setStats(dashRes.data);
      setImages(imgRes.data.images || []);
    } catch(e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  const totalSize = stats?.buckets?.reduce((sum, b) => sum + (b.totalSize || 0), 0) || 0;

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#f5f5f3', fontFamily: "'DM Sans', sans-serif" }}>
      <Sidebar />
      <main style={{ marginLeft: 220, flex: 1, padding: '2rem' }}>
        <div style={{ marginBottom: '1.75rem' }}>
          <h1 style={{ fontSize: 20, fontWeight: 500, color: '#111', letterSpacing: '-0.4px' }}>대시보드</h1>
          <p style={{ fontSize: 13, color: '#999', marginTop: 3 }}>전체 현황을 확인하세요</p>
        </div>

        {/* 통계 카드 */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12, marginBottom: '1.75rem' }}>
          {[
            { label: '총 이미지', value: stats?.totalImages ?? '-', sub: 'image-service' },
            { label: '총 오브젝트', value: stats?.totalObjects ?? '-', sub: '썸네일 포함' },
            { label: '버킷 수', value: stats?.buckets?.length ?? '-', sub: 'mini-s3' },
            { label: '총 용량', value: formatBytes(totalSize), sub: '저장 중' },
          ].map(card => (
            <div key={card.label} style={{ background: '#fff', border: '1px solid #e8e8e6', borderRadius: 10, padding: '1.1rem 1.25rem' }}>
              <div style={{ fontSize: 12, color: '#999', marginBottom: '0.5rem' }}>{card.label}</div>
              <div style={{ fontSize: 24, fontWeight: 500, color: '#111', letterSpacing: '-0.5px' }}>{card.value}</div>
              <div style={{ fontSize: 12, color: '#bbb', marginTop: 4 }}>{card.sub}</div>
            </div>
          ))}
        </div>

        {/* 버킷 현황 */}
        <div style={{ background: '#fff', border: '1px solid #e8e8e6', borderRadius: 10, padding: '1.25rem', marginBottom: '1.25rem' }}>
          <div style={{ fontSize: 14, fontWeight: 500, color: '#111', marginBottom: '1.25rem' }}>버킷 현황</div>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '3rem', color: '#bbb', fontSize: 14 }}>불러오는 중...</div>
          ) : stats?.buckets?.length ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {stats.buckets.map(b => (
                <div key={b.name} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 12px', border: '1px solid #e8e8e6', borderRadius: 8 }}>
                  <span style={{ fontSize: 14, fontWeight: 500, color: '#111' }}>{b.name}</span>
                  <span style={{ fontSize: 13, color: '#999' }}>{b.objectCount}개 · {formatBytes(b.totalSize)}</span>
                </div>
              ))}
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '3rem', color: '#bbb', fontSize: 14 }}>버킷이 없습니다</div>
          )}
        </div>

        {/* 최근 이미지 */}
        <div style={{ background: '#fff', border: '1px solid #e8e8e6', borderRadius: 10, padding: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
            <span style={{ fontSize: 14, fontWeight: 500, color: '#111' }}>최근 이미지</span>
            <button onClick={() => navigate('/images')} style={{ fontSize: 13, padding: '6px 14px', border: '1px solid #e0e0de', borderRadius: 7, background: '#fff', color: '#555', cursor: 'pointer', fontFamily: 'inherit' }}>
              전체 보기
            </button>
          </div>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '3rem', color: '#bbb', fontSize: 14 }}>불러오는 중...</div>
          ) : images.length ? (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 10 }}>
              {images.map(img => {
                const filename = img.objectKey?.split('/').pop() || '-';
                const thumbUrl = `${BASE_URL}/api/v1/proxy/images/${img.id}/variants/thumb_xs.png`;
                return (
                  <div key={img.id} style={{ border: '1px solid #e8e8e6', borderRadius: 8, overflow: 'hidden', cursor: 'pointer' }} onClick={() => navigate('/images')}>
                    <div style={{ height: 100, background: '#f0f0ee', overflow: 'hidden' }}>
                      <img src={thumbUrl} alt={filename} style={{ width: '100%', height: '100%', objectFit: 'cover' }} onError={e => e.target.style.display = 'none'} />
                    </div>
                    <div style={{ padding: '8px 10px' }}>
                      <div style={{ fontSize: 12, color: '#333', fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{filename}</div>
                      <div style={{ fontSize: 11, color: '#bbb', marginTop: 2 }}>{formatBytes(img.size)}</div>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '3rem', color: '#bbb', fontSize: 14 }}>이미지가 없습니다</div>
          )}
        </div>
      </main>
    </div>
  );
}