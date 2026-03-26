<template>
  <div :style="{ display: 'flex', minHeight: '100vh', background: '#f5f5f3', fontFamily: `'DM Sans', sans-serif` }">
    <Sidebar />
    <main :style="{ marginLeft: '220px', flex: 1, padding: '2rem' }">
      <div :style="{ marginBottom: '1.75rem' }">
        <h1 :style="{ fontSize: '20px', fontWeight: 500, color: '#111', letterSpacing: '-0.4px' }">대시보드</h1>
        <p :style="{ fontSize: '13px', color: '#999', marginTop: '3px' }">전체 현황을 확인하세요</p>
      </div>

      <!-- 통계 카드 -->
      <div :style="{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '12px', marginBottom: '1.75rem' }">
        <div v-for="card in statCards" :key="card.label"
          :style="{ background: '#fff', border: '1px solid #e8e8e6', borderRadius: '10px', padding: '1.1rem 1.25rem' }">
          <div :style="{ fontSize: '12px', color: '#999', marginBottom: '0.5rem' }">{{ card.label }}</div>
          <div :style="{ fontSize: '24px', fontWeight: 500, color: '#111', letterSpacing: '-0.5px' }">{{ card.value }}</div>
          <div :style="{ fontSize: '12px', color: '#bbb', marginTop: '4px' }">{{ card.sub }}</div>
        </div>
      </div>

      <!-- 버킷 현황 -->
      <div :style="sectionStyle">
        <div :style="{ fontSize: '14px', fontWeight: 500, color: '#111', marginBottom: '1.25rem' }">버킷 현황</div>
        <div v-if="loading" :style="emptyStyle">불러오는 중...</div>
        <div v-else-if="stats?.buckets?.length" :style="{ display: 'flex', flexDirection: 'column', gap: '8px' }">
          <div v-for="b in stats.buckets" :key="b.name"
            :style="{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 12px', border: '1px solid #e8e8e6', borderRadius: '8px' }">
            <span :style="{ fontSize: '14px', fontWeight: 500, color: '#111' }">{{ b.name }}</span>
            <span :style="{ fontSize: '13px', color: '#999' }">{{ b.objectCount }}개 · {{ formatBytes(b.totalSize) }}</span>
          </div>
        </div>
        <div v-else :style="emptyStyle">버킷이 없습니다</div>
      </div>

      <!-- 최근 이미지 -->
      <div :style="sectionStyle">
        <div :style="{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }">
          <span :style="{ fontSize: '14px', fontWeight: 500, color: '#111' }">최근 이미지</span>
          <router-link to="/images" :style="btnSmStyle">전체 보기</router-link>
        </div>
        <div v-if="loading" :style="emptyStyle">불러오는 중...</div>
        <div v-else-if="images.length" :style="{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '10px' }">
          <div v-for="img in images" :key="img.id"
            :style="{ border: '1px solid #e8e8e6', borderRadius: '8px', overflow: 'hidden', cursor: 'pointer' }"
            @click="$router.push('/images')">
            <div :style="{ height: '100px', background: '#f0f0ee', overflow: 'hidden' }">
              <img :src="`${BASE_URL}/api/v1/proxy/images/${img.id}/variants/thumb_xs.png`"
                :alt="getFilename(img.objectKey)"
                :style="{ width: '100%', height: '100%', objectFit: 'cover' }"
                @error="e => e.target.style.display = 'none'" />
            </div>
            <div :style="{ padding: '8px 10px' }">
              <div :style="{ fontSize: '12px', color: '#333', fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }">
                {{ getFilename(img.objectKey) }}
              </div>
              <div :style="{ fontSize: '11px', color: '#bbb', marginTop: '2px' }">{{ formatBytes(img.size) }}</div>
            </div>
          </div>
        </div>
        <div v-else :style="emptyStyle">이미지가 없습니다</div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import Sidebar from '../components/Sidebar.vue';
import api, { BASE_URL } from '../api/axios';

const stats = ref(null);
const images = ref([]);
const loading = ref(true);

function formatBytes(bytes) {
  if (!bytes) return '0 B';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + ' MB';
  return (bytes / 1024 / 1024 / 1024).toFixed(2) + ' GB';
}

function getFilename(objectKey) {
  return objectKey?.split('/').pop() || '-';
}

const totalSize = computed(() =>
  stats.value?.buckets?.reduce((sum, b) => sum + (b.totalSize || 0), 0) || 0
);

const statCards = computed(() => [
  { label: '총 이미지', value: stats.value?.totalImages ?? '-', sub: 'image-service' },
  { label: '총 오브젝트', value: stats.value?.totalObjects ?? '-', sub: '썸네일 포함' },
  { label: '버킷 수', value: stats.value?.buckets?.length ?? '-', sub: 'mini-s3' },
  { label: '총 용량', value: formatBytes(totalSize.value), sub: '저장 중' },
]);

onMounted(async () => {
  try {
    const [dashRes, imgRes] = await Promise.all([
      api.get('/api/v1/dashboard'),
      api.get('/api/v1/images?limit=10')
    ]);
    stats.value = dashRes.data;
    images.value = imgRes.data.images || [];
  } catch(e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
});

const sectionStyle = {
  background: '#fff', border: '1px solid #e8e8e6', borderRadius: '10px',
  padding: '1.25rem', marginBottom: '1.25rem'
};
const emptyStyle = { textAlign: 'center', padding: '3rem', color: '#bbb', fontSize: '14px' };
const btnSmStyle = {
  fontSize: '13px', padding: '6px 14px', border: '1px solid #e0e0de',
  borderRadius: '7px', background: '#fff', color: '#555', textDecoration: 'none'
};
</script>