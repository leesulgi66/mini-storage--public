import { useEffect, useState } from 'react';
import Sidebar from '../components/Sidebar';
import api from '../api/axios';

const BASE_URL =  api.defaults.baseURL;
const BUCKETS = ['images', 'thumbnails'];
const PAGE_SIZE = 20;

function formatBytes(bytes) {
  if (!bytes) return '0 B';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / 1024 / 1024).toFixed(1) + ' MB';
}
function formatDate(str) {
  if (!str) return '-';
  return new Date(str).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}
function getFilename(objectKey) {
  return objectKey?.split('/').pop() || '-';
}

export default function Images() {
  const [allImages, setAllImages] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [search, setSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [currentView, setCurrentView] = useState('grid');
  const [selectedBuckets, setSelectedBuckets] = useState(new Set(['images']));
  const [detailImg, setDetailImg] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadSelectedBuckets(selectedBuckets);
  }, []);

  async function loadSelectedBuckets(buckets) {
    setLoading(true);
    const results = [];
    for (const bucket of buckets) {
      try {
        if (bucket === 'images') {
          const res = await api.get('/api/v1/images?limit=200');
          results.push(...(res.data.images || []).map(img => ({ ...img, isBucketObject: false })));
        } else {
          const res = await api.get(`/api/v1/buckets/${bucket}/objects?limit=200`);
          results.push(...(res.data.objects || []).map(obj => ({
            id: obj.key,
            objectKey: obj.key,
            bucket: obj.bucket,
            size: obj.size,
            contentType: obj.contentType,
            createdAt: obj.createdAt,
            isBucketObject: true
          })));
        }
      } catch(e) { console.error(e); }
    }
    setAllImages(results);
    setFiltered(results);
    setCurrentPage(1);
    setLoading(false);
  }

  async function toggleBucket(bucket) {
    const next = new Set(selectedBuckets);
    if (next.has(bucket)) {
      if (next.size === 1) return;
      next.delete(bucket);
    } else {
      next.add(bucket);
    }
    setSelectedBuckets(next);
    setSearch('');
    await loadSelectedBuckets(next);
  }

  function handleSearch(q) {
    setSearch(q);
    const filtered = q
      ? allImages.filter(img => getFilename(img.objectKey).toLowerCase().includes(q.toLowerCase()))
      : [...allImages];
    setFiltered(filtered);
    setCurrentPage(1);
  }

  function thumbUrl(img) {
    if (img.isBucketObject) {
      const key = img.objectKey.startsWith(img.bucket + '/')
        ? img.objectKey.substring(img.bucket.length + 1)
        : img.objectKey;
      return `${BASE_URL}/api/v1/proxy/${img.bucket}/${key}`;
    }
    return `${BASE_URL}/api/v1/proxy/images/${img.id}/variants/thumb_xs.png`;
  }

  function previewUrl(img) {
    if (img.isBucketObject) {
      const key = img.objectKey.startsWith(img.bucket + '/')
        ? img.objectKey.substring(img.bucket.length + 1)
        : img.objectKey;
      return `${BASE_URL}/api/v1/proxy/${img.bucket}/${key}`;
    }
    return `${BASE_URL}/api/v1/proxy/images/${img.id}/variants/thumb_md.png`;
  }

  function confirmDelete(imageId) {
    if (localStorage.getItem('role') === 'VIEWER') {
      alert('열람 전용 계정은 삭제할 수 없습니다.');
      return;
    }
    setDeleteTarget(imageId);
  }

  async function executeDelete() {
    if (!deleteTarget) return;
    if (localStorage.getItem('role') === 'VIEWER') {
    alert('열람 전용 계정은 삭제할 수 없습니다.');
    return;
  }
    try {
      await api.delete(`/api/v1/images/${deleteTarget}`);
      const next = allImages.filter(i => i.id !== deleteTarget);
      setAllImages(next);
      setFiltered(next.filter(i => getFilename(i.objectKey).toLowerCase().includes(search.toLowerCase())));
    } catch(e) { alert('삭제 중 오류가 발생했습니다'); }
    finally { setDeleteTarget(null); }
  }

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const items = filtered.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#f5f5f3', fontFamily: "'DM Sans', sans-serif" }}>
      <Sidebar />
      <main style={{ marginLeft: 220, flex: 1, padding: '2rem' }}>
        {/* 헤더 */}
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '1.75rem' }}>
          <div>
            <h1 style={{ fontSize: 20, fontWeight: 500, color: '#111', letterSpacing: '-0.4px' }}>이미지 관리</h1>
            <p style={{ fontSize: 13, color: '#999', marginTop: 3 }}>업로드된 이미지를 조회하고 관리하세요</p>
          </div>
          <a href="/upload" style={{ padding: '9px 18px', fontSize: 13, fontWeight: 500, background: '#111', color: '#fff', borderRadius: 8, textDecoration: 'none' }}>+ 업로드</a>
        </div>

        {/* 버킷 탭 */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: '1.25rem' }}>
          <span style={{ fontSize: 12, color: '#999', fontWeight: 500, flexShrink: 0 }}>Bucket</span>
          <div style={{ display: 'flex', gap: 6 }}>
            {BUCKETS.map(b => (
              <button key={b} onClick={() => toggleBucket(b)} style={{
                padding: '7px 16px', fontSize: 13,
                border: '1px solid ' + (selectedBuckets.has(b) ? '#111' : '#e0e0de'),
                borderRadius: 7,
                background: selectedBuckets.has(b) ? '#111' : '#fff',
                color: selectedBuckets.has(b) ? '#fff' : '#555',
                cursor: 'pointer', fontFamily: 'inherit'
              }}>{b}</button>
            ))}
          </div>
        </div>

        {/* 필터바 */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: '1.25rem' }}>
          <div style={{ position: 'relative', flex: 1, maxWidth: 280 }}>
            <svg viewBox="0 0 16 16" fill="none" stroke="#bbb" strokeWidth="1.5" width="14" height="14"
              style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)' }}>
              <circle cx="6.5" cy="6.5" r="4"/><path d="M11 11l3 3"/>
            </svg>
            <input value={search} onChange={e => handleSearch(e.target.value)} placeholder="파일명 검색..."
              style={{ width: '100%', padding: '8px 10px 8px 32px', fontSize: 13, border: '1px solid #e0e0de', borderRadius: 7, fontFamily: 'inherit', outline: 'none', boxSizing: 'border-box' }} />
          </div>
          <div style={{ display: 'flex', border: '1px solid #e0e0de', borderRadius: 7, overflow: 'hidden' }}>
            {['grid', 'list'].map(v => (
              <button key={v} onClick={() => setCurrentView(v)} style={{
                padding: '7px 10px', background: currentView === v ? '#111' : '#fff',
                border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center'
              }}>
                {v === 'grid'
                  ? <svg viewBox="0 0 16 16" fill="none" stroke={currentView === v ? '#fff' : '#888'} strokeWidth="1.5" width="15" height="15"><rect x="1" y="1" width="6" height="6" rx="1"/><rect x="9" y="1" width="6" height="6" rx="1"/><rect x="1" y="9" width="6" height="6" rx="1"/><rect x="9" y="9" width="6" height="6" rx="1"/></svg>
                  : <svg viewBox="0 0 16 16" fill="none" stroke={currentView === v ? '#fff' : '#888'} strokeWidth="1.5" width="15" height="15"><path d="M1 4h14M1 8h14M1 12h14"/></svg>
                }
              </button>
            ))}
          </div>
          <span style={{ fontSize: 13, color: '#999', marginLeft: 'auto' }}>총 {filtered.length}개</span>
        </div>

        {/* 그리드 뷰 */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '4rem', color: '#bbb', fontSize: 14 }}>불러오는 중...</div>
        ) : currentView === 'grid' ? (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 10 }}>
            {items.length ? items.map(img => {
              const filename = getFilename(img.objectKey);
              return (
                <div key={img.id} onClick={() => setDetailImg(img)}
                  style={{ border: '1px solid #e8e8e6', borderRadius: 8, overflow: 'hidden', background: '#fff', cursor: 'pointer' }}
                  onMouseEnter={e => {
                    e.currentTarget.style.borderColor = '#bbb';
                    e.currentTarget.querySelector('.card-actions').style.opacity = '1';
                  }}
                  onMouseLeave={e => {
                    e.currentTarget.style.borderColor = '#e8e8e6';
                    e.currentTarget.querySelector('.card-actions').style.opacity = '0';
                  }}>
                  <div style={{ position: 'relative', height: 100, background: '#f0f0ee', overflow: 'hidden' }}>
                    <img src={thumbUrl(img)} alt={filename} style={{ width: '100%', height: '100%', objectFit: 'cover' }} onError={e => e.target.style.display = 'none'} />
                    <div className="card-actions" style={{
                      position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.4)',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      gap: 8, opacity: 0, transition: 'opacity 0.15s'
                    }}>
                      <button onClick={e => { e.stopPropagation(); setDetailImg(img); }}
                        style={{ width: 30, height: 30, borderRadius: 6, border: 'none', background: '#fff', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <svg viewBox="0 0 14 14" fill="none" stroke="#333" strokeWidth="1.5" width="14" height="14"><circle cx="7" cy="7" r="4"/><circle cx="7" cy="7" r="1.5" fill="#333" stroke="none"/></svg>
                      </button>
                      {!img.isBucketObject && (
                        <button onClick={e => { e.stopPropagation(); confirmDelete(img.id); }}
                          style={{ width: 30, height: 30, borderRadius: 6, border: 'none', background: '#fcebeb', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                          <svg viewBox="0 0 14 14" fill="none" stroke="#a32d2d" strokeWidth="1.5" width="14" height="14"><path d="M2 3h10M5 3V2h4v1M4 3v8h6V3"/></svg>
                        </button>
                      )}
                    </div>
                  </div>
                  <div style={{ padding: '8px 10px' }}>
                    <div style={{ fontSize: 12, color: '#333', fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }} title={filename}>{filename}</div>
                    <div style={{ fontSize: 11, color: '#bbb', marginTop: 2, display: 'flex', justifyContent: 'space-between' }}>
                      <span>{formatBytes(img.size)}</span>
                      <span style={{ fontSize: 10, padding: '2px 6px', borderRadius: 4, background: '#f0f0ee', color: '#888' }}>{img.contentType?.split('/')[1] || '-'}</span>
                    </div>
                  </div>
                </div>
              );
            }) : <div style={{ gridColumn: '1/-1', textAlign: 'center', padding: '4rem', color: '#bbb', fontSize: 14 }}>이미지가 없습니다</div>}
          </div>
        ) : (
          // 리스트 뷰
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            {items.length ? items.map(img => {
              const filename = getFilename(img.objectKey);
              return (
                <div key={img.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 14px', background: '#fff', border: '1px solid #e8e8e6', borderRadius: 8 }}>
                  <div style={{ width: 44, height: 44, borderRadius: 6, overflow: 'hidden', background: '#f0f0ee', flexShrink: 0 }}>
                    <img src={thumbUrl(img)} alt={filename} style={{ width: '100%', height: '100%', objectFit: 'cover' }} onError={e => e.target.style.display = 'none'} />
                  </div>
                  <span style={{ fontSize: 13, fontWeight: 500, color: '#111', flex: 1, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{filename}</span>
                  <span style={{ fontSize: 12, color: '#999', flexShrink: 0 }}>{formatBytes(img.size)}</span>
                  <span style={{ fontSize: 12, color: '#bbb', flexShrink: 0, width: 140, textAlign: 'right' }}>{formatDate(img.createdAt)}</span>
                  <div style={{ display: 'flex', gap: 6, flexShrink: 0 }}>
                    <button onClick={() => setDetailImg(img)} style={{ padding: '5px 10px', fontSize: 12, border: '1px solid #e0e0de', borderRadius: 6, background: '#fff', cursor: 'pointer', fontFamily: 'inherit', color: '#555' }}>상세</button>
                    {!img.isBucketObject && <button onClick={() => setDeleteTarget(img.id)} style={{ padding: '5px 10px', fontSize: 12, border: '1px solid #f7c1c1', borderRadius: 6, background: '#fff', cursor: 'pointer', fontFamily: 'inherit', color: '#a32d2d' }}>삭제</button>}
                  </div>
                </div>
              );
            }) : <div style={{ textAlign: 'center', padding: '4rem', color: '#bbb', fontSize: 14 }}>이미지가 없습니다</div>}
          </div>
        )}

        {/* 페이지네이션 */}
        {totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: 6, marginTop: '1.5rem' }}>
            <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage === 1}
              style={{ padding: '6px 12px', fontSize: 13, border: '1px solid #e0e0de', borderRadius: 6, background: '#fff', color: currentPage === 1 ? '#ccc' : '#555', cursor: currentPage === 1 ? 'not-allowed' : 'pointer', fontFamily: 'inherit' }}>이전</button>
            {Array.from({ length: totalPages }, (_, i) => i + 1).map(i => (
              <button key={i} onClick={() => setCurrentPage(i)}
                style={{ padding: '6px 12px', fontSize: 13, border: '1px solid ' + (i === currentPage ? '#111' : '#e0e0de'), borderRadius: 6, background: i === currentPage ? '#111' : '#fff', color: i === currentPage ? '#fff' : '#555', cursor: 'pointer', fontFamily: 'inherit' }}>{i}</button>
            ))}
            <button onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages}
              style={{ padding: '6px 12px', fontSize: 13, border: '1px solid #e0e0de', borderRadius: 6, background: '#fff', color: currentPage === totalPages ? '#ccc' : '#555', cursor: currentPage === totalPages ? 'not-allowed' : 'pointer', fontFamily: 'inherit' }}>다음</button>
          </div>
        )}
      </main>

      {/* 상세 모달 */}
      {detailImg && (
        <div onClick={e => e.target === e.currentTarget && setDetailImg(null)}
          style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }}>
          <div style={{ background: '#fff', borderRadius: 12, padding: '1.5rem', width: 520, maxWidth: '90vw', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem', gap: 10 }}>
              <span style={{ fontSize: 15, fontWeight: 500, color: '#111', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', minWidth: 0 }}>{getFilename(detailImg.objectKey)}</span>
              <button onClick={() => setDetailImg(null)} style={{ flexShrink: 0, width: 28, height: 28, border: 'none', background: '#f0f0ee', borderRadius: 6, cursor: 'pointer', fontSize: 16, color: '#555' }}>✕</button>
            </div>
            <div style={{ width: '100%', height: 220, background: '#f0f0ee', borderRadius: 8, overflow: 'hidden', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <img src={previewUrl(detailImg)} alt="" style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }} />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: '1.25rem' }}>
              {[
                { label: '이미지 ID', value: detailImg.id },
                { label: '파일 크기', value: formatBytes(detailImg.size) },
                { label: 'MIME 타입', value: detailImg.contentType || '-' },
                { label: '업로드 일시', value: formatDate(detailImg.createdAt) },
              ].map(m => (
                <div key={m.label} style={{ padding: '10px 12px', background: '#f5f5f3', borderRadius: 7 }}>
                  <div style={{ fontSize: 11, color: '#999', marginBottom: 3 }}>{m.label}</div>
                  <div style={{ fontSize: 13, color: '#111', fontWeight: 500, wordBreak: 'break-all' }}>{m.value}</div>
                </div>
              ))}
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, paddingTop: '1.25rem', borderTop: '1px solid #f0f0ee' }}>
              <button onClick={() => setDetailImg(null)} style={{ padding: '8px 16px', fontSize: 13, border: '1px solid #e0e0de', borderRadius: 7, background: '#fff', color: '#555', cursor: 'pointer', fontFamily: 'inherit' }}>닫기</button>
              {!detailImg.isBucketObject && (
                <button onClick={() => { setDeleteTarget(detailImg.id); setDetailImg(null); }}
                  style={{ padding: '8px 16px', fontSize: 13, border: '1px solid #f7c1c1', borderRadius: 7, background: '#fff', color: '#a32d2d', cursor: 'pointer', fontFamily: 'inherit' }}>삭제</button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* 삭제 확인 모달 */}
      {deleteTarget && (
        <div onClick={e => e.target === e.currentTarget && setDeleteTarget(null)}
          style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 }}>
          <div style={{ background: '#fff', borderRadius: 12, padding: '1.5rem', width: 340 }}>
            <div style={{ fontSize: 15, fontWeight: 500, color: '#111', marginBottom: '0.5rem' }}>이미지를 삭제할까요?</div>
            <div style={{ fontSize: 13, color: '#666', marginBottom: '1.5rem', lineHeight: 1.6 }}>삭제된 이미지는 복구할 수 없습니다.</div>
            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
              <button onClick={() => setDeleteTarget(null)} style={{ padding: '8px 16px', fontSize: 13, border: '1px solid #e0e0de', borderRadius: 7, background: '#fff', color: '#555', cursor: 'pointer', fontFamily: 'inherit' }}>취소</button>
              <button onClick={executeDelete} style={{ padding: '8px 16px', fontSize: 13, border: '1px solid #f7c1c1', borderRadius: 7, background: '#fff', color: '#a32d2d', cursor: 'pointer', fontFamily: 'inherit' }}>삭제</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}