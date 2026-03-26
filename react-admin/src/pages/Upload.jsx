import { useState } from 'react';
import Sidebar from '../components/Sidebar';
import api from '../api/axios';

function formatBytes(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / 1024 / 1024).toFixed(1) + ' MB';
}

export default function Upload() {
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [dragging, setDragging] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [progressText, setProgressText] = useState('');
  const [results, setResults] = useState([]);

  function addFiles(files) {
    const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    const newFiles = Array.from(files).filter(f =>
      allowed.includes(f.type) && f.size <= 20 * 1024 * 1024 &&
      !selectedFiles.find(s => s.name === f.name && s.size === f.size)
    );
    setSelectedFiles(prev => [...prev, ...newFiles]);
  }

  function removeFile(index) {
    setSelectedFiles(prev => prev.filter((_, i) => i !== index));
  }

  async function startUpload() {
    if (localStorage.getItem('role') === 'VIEWER') {
      alert('열람 전용 계정은 업로드할 수 없습니다.');
      return;
    }
    if (!selectedFiles.length) return;
    setUploading(true);
    setResults([]);

    const newResults = [];
    for (let i = 0; i < selectedFiles.length; i++) {
      const file = selectedFiles[i];
      setProgress(Math.round((i / selectedFiles.length) * 100));
      setProgressText(`업로드 중... (${i + 1}/${selectedFiles.length})`);

      try {
        const formData = new FormData();
        formData.append('file', file);
        await api.post('/api/v1/images', formData);
        newResults.push({ name: file.name, success: true });
      } catch(e) {
        newResults.push({ name: file.name, success: false, msg: e.response?.data?.message || '업로드 실패' });
      }
      setResults([...newResults]);
    }

    setProgress(100);
    setProgressText('업로드 완료');
    setSelectedFiles([]);
    setUploading(false);
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#f5f5f3', fontFamily: "'DM Sans', sans-serif" }}>
      <Sidebar />
      <main style={{ marginLeft: 220, flex: 1, padding: '2rem', maxWidth: 860 }}>
        <div style={{ marginBottom: '1.75rem' }}>
          <h1 style={{ fontSize: 20, fontWeight: 500, color: '#111', letterSpacing: '-0.4px' }}>이미지 업로드</h1>
          <p style={{ fontSize: 13, color: '#999', marginTop: 3 }}>이미지를 업로드하면 썸네일(xs/sm/md/lg)이 자동으로 생성됩니다</p>
        </div>

        <div style={{ background: '#fff', border: '1px solid #e8e8e6', borderRadius: 10, padding: '1.5rem', marginBottom: '1.25rem' }}>
          <div style={{ fontSize: 14, fontWeight: 500, color: '#111', marginBottom: '1.25rem' }}>파일 선택</div>

          {/* 드롭존 */}
          <div
            onClick={() => document.getElementById('file-input').click()}
            onDragOver={e => { e.preventDefault(); setDragging(true); }}
            onDragLeave={() => setDragging(false)}
            onDrop={e => { e.preventDefault(); setDragging(false); addFiles(e.dataTransfer.files); }}
            style={{
              border: `2px dashed ${dragging ? '#aaa' : '#e0e0de'}`,
              borderRadius: 10, padding: '3rem 2rem', textAlign: 'center',
              cursor: 'pointer', background: dragging ? '#fafaf8' : '#fff',
              transition: 'all 0.15s'
            }}>
            <div style={{ width: 44, height: 44, margin: '0 auto 1rem', background: '#f0f0ee', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <svg viewBox="0 0 22 22" fill="none" stroke="#aaa" strokeWidth="1.5" width="22" height="22">
                <path d="M11 14V4M8 7l3-3 3 3"/><path d="M3 15v3a1 1 0 001 1h14a1 1 0 001-1v-3"/>
              </svg>
            </div>
            <div style={{ fontSize: 14, fontWeight: 500, color: '#333', marginBottom: 4 }}>파일을 드래그하거나 클릭하여 선택</div>
            <div style={{ fontSize: 13, color: '#bbb' }}>JPG, PNG, GIF, WEBP · 최대 20MB · 여러 파일 동시 업로드 가능</div>
          </div>
          <input id="file-input" type="file" accept="image/jpeg,image/png,image/gif,image/webp" multiple
            style={{ display: 'none' }} onChange={e => { addFiles(e.target.files); e.target.value = ''; }} />

          {/* 미리보기 */}
          {selectedFiles.length > 0 && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 10, marginTop: '1.25rem' }}>
              {selectedFiles.map((file, i) => (
                <div key={i} style={{ border: '1px solid #e8e8e6', borderRadius: 8, overflow: 'hidden', position: 'relative' }}>
                  <div style={{ height: 90, background: '#f0f0ee', overflow: 'hidden' }}>
                    <img src={URL.createObjectURL(file)} alt={file.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  </div>
                  <div style={{ padding: '8px 10px' }}>
                    <div style={{ fontSize: 12, color: '#333', fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{file.name}</div>
                    <div style={{ fontSize: 11, color: '#bbb', marginTop: 2 }}>{formatBytes(file.size)}</div>
                  </div>
                  <button onClick={() => removeFile(i)}
                    style={{ position: 'absolute', top: 5, right: 5, width: 20, height: 20, background: 'rgba(0,0,0,0.45)', borderRadius: '50%', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 0 }}>
                    <svg viewBox="0 0 10 10" fill="none" stroke="white" strokeWidth="1.5" width="10" height="10"><path d="M2 2l6 6M8 2l-6 6"/></svg>
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* 업로드 버튼 */}
          {selectedFiles.length > 0 && (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '1.25rem' }}>
              <span style={{ fontSize: 13, color: '#999' }}>{selectedFiles.length}개 파일 선택됨</span>
              <button onClick={startUpload} disabled={uploading}
                style={{ padding: '10px 24px', fontSize: 14, fontWeight: 500, background: uploading ? '#ccc' : '#111', color: '#fff', border: 'none', borderRadius: 8, cursor: uploading ? 'not-allowed' : 'pointer', fontFamily: 'inherit' }}>
                업로드 시작
              </button>
            </div>
          )}

          {/* 프로그레스 */}
          {uploading && (
            <div style={{ marginTop: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: '#999', marginBottom: 6 }}>
                <span>{progressText}</span>
                <span>{progress}%</span>
              </div>
              <div style={{ height: 4, background: '#f0f0ee', borderRadius: 2, overflow: 'hidden' }}>
                <div style={{ height: '100%', background: '#111', borderRadius: 2, width: progress + '%', transition: 'width 0.3s' }} />
              </div>
            </div>
          )}
        </div>

        {/* 결과 */}
        {results.length > 0 && (
          <div style={{ background: '#fff', border: '1px solid #e8e8e6', borderRadius: 10, padding: '1.5rem' }}>
            <div style={{ fontSize: 14, fontWeight: 500, color: '#111', marginBottom: '1.25rem' }}>업로드 결과</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {results.map((r, i) => (
                <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 12px', border: '1px solid #e8e8e6', borderRadius: 8 }}>
                  <div style={{ width: 28, height: 28, borderRadius: 6, background: r.success ? '#eaf3de' : '#fcebeb', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                    {r.success
                      ? <svg viewBox="0 0 16 16" width="16" height="16" fill="none" stroke="#3b6d11" strokeWidth="1.5"><path d="M3 8l4 4 6-6"/></svg>
                      : <svg viewBox="0 0 16 16" width="16" height="16" fill="none" stroke="#a32d2d" strokeWidth="1.5"><path d="M4 4l8 8M12 4l-8 8"/></svg>
                    }
                  </div>
                  <span style={{ fontSize: 13, color: '#333', flex: 1, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{r.name}</span>
                  {r.msg && <span style={{ fontSize: 11, color: '#bbb', flexShrink: 0 }}>{r.msg}</span>}
                  <span style={{ fontSize: 11, padding: '3px 8px', borderRadius: 4, background: r.success ? '#eaf3de' : '#fcebeb', color: r.success ? '#3b6d11' : '#a32d2d', flexShrink: 0 }}>
                    {r.success ? '완료' : '실패'}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}