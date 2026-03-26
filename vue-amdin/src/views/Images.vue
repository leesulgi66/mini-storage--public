<template>
  <div :style="{ display: 'flex', minHeight: '100vh', background: '#f5f5f3', fontFamily: `'DM Sans', sans-serif` }">
    <Sidebar />
    <main :style="{ marginLeft: '220px', flex: 1, padding: '2rem' }">
      <!-- 헤더 -->
      <div :style="{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '1.75rem' }">
        <div>
          <h1 :style="{ fontSize: '20px', fontWeight: 500, color: '#111', letterSpacing: '-0.4px' }">이미지 관리</h1>
          <p :style="{ fontSize: '13px', color: '#999', marginTop: '3px' }">업로드된 이미지를 조회하고 관리하세요</p>
        </div>
        <router-link to="/upload" :style="btnPrimaryStyle">+ 업로드</router-link>
      </div>

      <!-- 버킷 탭 -->
      <div :style="{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '1.25rem' }">
        <span :style="{ fontSize: '12px', color: '#999', fontWeight: 500, flexShrink: 0 }">Bucket</span>
        <div :style="{ display: 'flex', gap: '6px' }">
          <button v-for="b in BUCKETS" :key="b" @click="toggleBucket(b)" :style="{
            padding: '7px 16px', fontSize: '13px',
            border: `1px solid ${selectedBuckets.has(b) ? '#111' : '#e0e0de'}`,
            borderRadius: '7px',
            background: selectedBuckets.has(b) ? '#111' : '#fff',
            color: selectedBuckets.has(b) ? '#fff' : '#555',
            cursor: 'pointer', fontFamily: 'inherit'
          }">{{ b }}</button>
        </div>
      </div>

      <!-- 필터바 -->
      <div :style="{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '1.25rem' }">
        <div :style="{ position: 'relative', flex: 1, maxWidth: '280px' }">
          <svg viewBox="0 0 16 16" fill="none" stroke="#bbb" stroke-width="1.5" width="14" height="14"
            :style="{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)' }">
            <circle cx="6.5" cy="6.5" r="4"/><path d="M11 11l3 3"/>
          </svg>
          <input v-model="search" @input="handleSearch" placeholder="파일명 검색..."
            :style="{ width: '100%', padding: '8px 10px 8px 32px', fontSize: '13px', border: '1px solid #e0e0de', borderRadius: '7px', fontFamily: 'inherit', outline: 'none', boxSizing: 'border-box' }" />
        </div>
        <div :style="{ display: 'flex', border: '1px solid #e0e0de', borderRadius: '7px', overflow: 'hidden' }">
          <button v-for="v in ['grid', 'list']" :key="v" @click="currentView = v" :style="{
            padding: '7px 10px', background: currentView === v ? '#111' : '#fff',
            border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center'
          }">
            <svg v-if="v === 'grid'" viewBox="0 0 16 16" fill="none" :stroke="currentView === v ? '#fff' : '#888'" stroke-width="1.5" width="15" height="15">
              <rect x="1" y="1" width="6" height="6" rx="1"/><rect x="9" y="1" width="6" height="6" rx="1"/>
              <rect x="1" y="9" width="6" height="6" rx="1"/><rect x="9" y="9" width="6" height="6" rx="1"/>
            </svg>
            <svg v-else viewBox="0 0 16 16" fill="none" :stroke="currentView === v ? '#fff' : '#888'" stroke-width="1.5" width="15" height="15">
              <path d="M1 4h14M1 8h14M1 12h14"/>
            </svg>
          </button>
        </div>
        <span :style="{ fontSize: '13px', color: '#999', marginLeft: 'auto' }">총 {{ filtered.length }}개</span>
      </div>

      <!-- 로딩 -->
      <div v-if="loading" :style="{ textAlign: 'center', padding: '4rem', color: '#bbb', fontSize: '14px' }">불러오는 중...</div>

      <!-- 그리드 뷰 -->
      <div v-else-if="currentView === 'grid'" :style="{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '10px' }">
        <div v-if="!pageItems.length" :style="{ gridColumn: '1/-1', textAlign: 'center', padding: '4rem', color: '#bbb', fontSize: '14px' }">이미지가 없습니다</div>
        <div v-for="img in pageItems" :key="img.id" @click="detailImg = img"
          :style="{ border: '1px solid #e8e8e6', borderRadius: '8px', overflow: 'hidden', background: '#fff', cursor: 'pointer' }"
          @mouseenter="e => { e.currentTarget.style.borderColor = '#bbb'; e.currentTarget.querySelector('.card-actions').style.opacity = '1'; }"
          @mouseleave="e => { e.currentTarget.style.borderColor = '#e8e8e6'; e.currentTarget.querySelector('.card-actions').style.opacity = '0'; }">
          <div :style="{ position: 'relative', height: '100px', background: '#f0f0ee', overflow: 'hidden' }">
            <img :src="thumbUrl(img)" :alt="getFilename(img.objectKey)"
              :style="{ width: '100%', height: '100%', objectFit: 'cover' }"
              @error="e => e.target.style.display = 'none'" />
            <div class="card-actions" :style="{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', opacity: 0, transition: 'opacity 0.15s' }">
              <button @click.stop="detailImg = img" :style="actionBtnStyle">
                <svg viewBox="0 0 14 14" fill="none" stroke="#333" stroke-width="1.5" width="14" height="14"><circle cx="7" cy="7" r="4"/><circle cx="7" cy="7" r="1.5" fill="#333" stroke="none"/></svg>
              </button>
              <button v-if="!img.isBucketObject" @click.stop="deleteTarget = img.id" :style="{ ...actionBtnStyle, background: '#fcebeb' }">
                <svg viewBox="0 0 14 14" fill="none" stroke="#a32d2d" stroke-width="1.5" width="14" height="14"><path d="M2 3h10M5 3V2h4v1M4 3v8h6V3"/></svg>
              </button>
            </div>
          </div>
          <div :style="{ padding: '8px 10px' }">
            <div :style="{ fontSize: '12px', color: '#333', fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }">{{ getFilename(img.objectKey) }}</div>
            <div :style="{ fontSize: '11px', color: '#bbb', marginTop: '2px', display: 'flex', justifyContent: 'space-between' }">
              <span>{{ formatBytes(img.size) }}</span>
              <span :style="{ fontSize: '10px', padding: '2px 6px', borderRadius: '4px', background: '#f0f0ee', color: '#888' }">{{ img.contentType?.split('/')[1] || '-' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 리스트 뷰 -->
      <div v-else :style="{ display: 'flex', flexDirection: 'column', gap: '6px' }">
        <div v-if="!pageItems.length" :style="{ textAlign: 'center', padding: '4rem', color: '#bbb', fontSize: '14px' }">이미지가 없습니다</div>
        <div v-for="img in pageItems" :key="img.id"
          :style="{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 14px', background: '#fff', border: '1px solid #e8e8e6', borderRadius: '8px' }">
          <div :style="{ width: '44px', height: '44px', borderRadius: '6px', overflow: 'hidden', background: '#f0f0ee', flexShrink: 0 }">
            <img :src="thumbUrl(img)" :style="{ width: '100%', height: '100%', objectFit: 'cover' }" @error="e => e.target.style.display='none'" />
          </div>
          <span :style="{ fontSize: '13px', fontWeight: 500, color: '#111', flex: 1, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }">{{ getFilename(img.objectKey) }}</span>
          <span :style="{ fontSize: '12px', color: '#999', flexShrink: 0 }">{{ formatBytes(img.size) }}</span>
          <span :style="{ fontSize: '12px', color: '#bbb', flexShrink: 0, width: '140px', textAlign: 'right' }">{{ formatDate(img.createdAt) }}</span>
          <div :style="{ display: 'flex', gap: '6px', flexShrink: 0 }">
            <button @click="detailImg = img" :style="listActionBtnStyle">상세</button>
            <button v-if="!img.isBucketObject" @click="deleteTarget = img.id" :style="{ ...listActionBtnStyle, borderColor: '#f7c1c1', color: '#a32d2d' }">삭제</button>
          </div>
        </div>
      </div>

      <!-- 페이지네이션 -->
      <div v-if="totalPages > 1" :style="{ display: 'flex', justifyContent: 'center', gap: '6px', marginTop: '1.5rem' }">
        <button @click="currentPage > 1 && currentPage--" :disabled="currentPage === 1" :style="pageBtnStyle(currentPage === 1)">이전</button>
        <button v-for="i in totalPages" :key="i" @click="currentPage = i" :style="pageBtnStyle(false, i === currentPage)">{{ i }}</button>
        <button @click="currentPage < totalPages && currentPage++" :disabled="currentPage === totalPages" :style="pageBtnStyle(currentPage === totalPages)">다음</button>
      </div>
    </main>

    <!-- 상세 모달 -->
    <div v-if="detailImg" @click.self="detailImg = null"
      :style="{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }">
      <div :style="{ background: '#fff', borderRadius: '12px', padding: '1.5rem', width: '520px', maxWidth: '90vw', maxHeight: '90vh', overflowY: 'auto' }">
        <div :style="{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem', gap: '10px' }">
          <span :style="{ fontSize: '15px', fontWeight: 500, color: '#111', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', minWidth: 0 }">{{ getFilename(detailImg.objectKey) }}</span>
          <button @click="detailImg = null" :style="{ flexShrink: 0, width: '28px', height: '28px', border: 'none', background: '#f0f0ee', borderRadius: '6px', cursor: 'pointer', fontSize: '16px', color: '#555' }">✕</button>
        </div>
        <div :style="{ width: '100%', height: '220px', background: '#f0f0ee', borderRadius: '8px', overflow: 'hidden', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }">
          <img :src="previewUrl(detailImg)" :style="{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }" />
        </div>
        <div :style="{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', marginBottom: '1.25rem' }">
          <div v-for="m in detailMeta" :key="m.label" :style="{ padding: '10px 12px', background: '#f5f5f3', borderRadius: '7px' }">
            <div :style="{ fontSize: '11px', color: '#999', marginBottom: '3px' }">{{ m.label }}</div>
            <div :style="{ fontSize: '13px', color: '#111', fontWeight: 500, wordBreak: 'break-all' }">{{ m.value }}</div>
          </div>
        </div>
        <div :style="{ display: 'flex', justifyContent: 'flex-end', gap: '8px', paddingTop: '1.25rem', borderTop: '1px solid #f0f0ee' }">
          <button @click="detailImg = null" :style="btnCancelStyle">닫기</button>
          <button v-if="!detailImg.isBucketObject" @click="() => { deleteTarget = detailImg.id; detailImg = null; }" :style="btnDeleteStyle">삭제</button>
        </div>
      </div>
    </div>

    <!-- 삭제 확인 모달 -->
    <div v-if="deleteTarget" @click.self="deleteTarget = null"
      :style="{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 }">
      <div :style="{ background: '#fff', borderRadius: '12px', padding: '1.5rem', width: '340px' }">
        <div :style="{ fontSize: '15px', fontWeight: 500, color: '#111', marginBottom: '0.5rem' }">이미지를 삭제할까요?</div>
        <div :style="{ fontSize: '13px', color: '#666', marginBottom: '1.5rem', lineHeight: 1.6 }">삭제된 이미지는 복구할 수 없습니다.</div>
        <div :style="{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }">
          <button @click="deleteTarget = null" :style="btnCancelStyle">취소</button>
          <button @click="executeDelete" :style="btnDeleteStyle">삭제</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import Sidebar from '../components/Sidebar.vue';
import api, { BASE_URL } from '../api/axios';

const BUCKETS = ['images', 'thumbnails'];
const PAGE_SIZE = 20;

const allImages = ref([]);
const filtered = ref([]);
const search = ref('');
const currentPage = ref(1);
const currentView = ref('grid');
const selectedBuckets = ref(new Set(['images']));
const detailImg = ref(null);
const deleteTarget = ref(null);
const loading = ref(false);

const totalPages = computed(() => Math.ceil(filtered.value.length / PAGE_SIZE));
const pageItems = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE;
  return filtered.value.slice(start, start + PAGE_SIZE);
});

const detailMeta = computed(() => detailImg.value ? [
  { label: '이미지 ID', value: detailImg.value.id },
  { label: '파일 크기', value: formatBytes(detailImg.value.size) },
  { label: 'MIME 타입', value: detailImg.value.contentType || '-' },
  { label: '업로드 일시', value: formatDate(detailImg.value.createdAt) },
] : []);

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
function thumbUrl(img) {
  if (img.isBucketObject) {
    const key = img.objectKey.startsWith(img.bucket + '/') ? img.objectKey.substring(img.bucket.length + 1) : img.objectKey;
    return `${BASE_URL}/api/v1/proxy/${img.bucket}/${key}`;
  }
  return `${BASE_URL}/api/v1/proxy/images/${img.id}/variants/thumb_xs.png`;
}
function previewUrl(img) {
  if (img.isBucketObject) {
    const key = img.objectKey.startsWith(img.bucket + '/') ? img.objectKey.substring(img.bucket.length + 1) : img.objectKey;
    return `${BASE_URL}/api/v1/proxy/${img.bucket}/${key}`;
  }
  return `${BASE_URL}/api/v1/proxy/images/${img.id}/variants/thumb_md.png`;
}

function handleSearch() {
  const q = search.value.trim().toLowerCase();
  filtered.value = q ? allImages.value.filter(img => getFilename(img.objectKey).toLowerCase().includes(q)) : [...allImages.value];
  currentPage.value = 1;
}

async function toggleBucket(bucket) {
  const next = new Set(selectedBuckets.value);
  if (next.has(bucket)) {
    if (next.size === 1) return;
    next.delete(bucket);
  } else {
    next.add(bucket);
  }
  selectedBuckets.value = next;
  search.value = '';
  await loadSelectedBuckets(next);
}

async function loadSelectedBuckets(buckets) {
  loading.value = true;
  const results = [];
  for (const bucket of buckets) {
    try {
      if (bucket === 'images') {
        const res = await api.get('/api/v1/images?limit=200');
        results.push(...(res.data.images || []).map(img => ({ ...img, isBucketObject: false })));
      } else {
        const res = await api.get(`/api/v1/buckets/${bucket}/objects?limit=200`);
        results.push(...(res.data.objects || []).map(obj => ({
          id: obj.key, objectKey: obj.key, bucket: obj.bucket,
          size: obj.size, contentType: obj.contentType, createdAt: obj.createdAt, isBucketObject: true
        })));
      }
    } catch(e) { console.error(e); }
  }
  allImages.value = results;
  filtered.value = [...results];
  currentPage.value = 1;
  loading.value = false;
}

async function executeDelete() {
  if (localStorage.getItem('role') === 'VIEWER') {
    alert('열람 전용 계정은 삭제할 수 없습니다.');
    deleteTarget.value = null;
    return;
  }
  if (!deleteTarget.value) return;
  try {
    await api.delete(`/api/v1/images/${deleteTarget.value}`);
    allImages.value = allImages.value.filter(i => i.id !== deleteTarget.value);
    filtered.value = filtered.value.filter(i => i.id !== deleteTarget.value);
  } catch(e) { alert('삭제 중 오류가 발생했습니다'); }
  finally { deleteTarget.value = null; }
}

onMounted(() => loadSelectedBuckets(selectedBuckets.value));

const btnPrimaryStyle = { padding: '9px 18px', fontSize: '13px', fontWeight: 500, background: '#111', color: '#fff', borderRadius: '8px', textDecoration: 'none' };
const actionBtnStyle = { width: '30px', height: '30px', borderRadius: '6px', border: 'none', background: '#fff', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' };
const listActionBtnStyle = { padding: '5px 10px', fontSize: '12px', border: '1px solid #e0e0de', borderRadius: '6px', background: '#fff', cursor: 'pointer', fontFamily: 'inherit', color: '#555' };
const btnCancelStyle = { padding: '8px 16px', fontSize: '13px', border: '1px solid #e0e0de', borderRadius: '7px', background: '#fff', color: '#555', cursor: 'pointer', fontFamily: 'inherit' };
const btnDeleteStyle = { padding: '8px 16px', fontSize: '13px', border: '1px solid #f7c1c1', borderRadius: '7px', background: '#fff', color: '#a32d2d', cursor: 'pointer', fontFamily: 'inherit' };
const pageBtnStyle = (disabled, active) => ({
  padding: '6px 12px', fontSize: '13px', border: `1px solid ${active ? '#111' : '#e0e0de'}`,
  borderRadius: '6px', background: active ? '#111' : '#fff',
  color: disabled ? '#ccc' : active ? '#fff' : '#555',
  cursor: disabled ? 'not-allowed' : 'pointer', fontFamily: 'inherit'
});
</script>