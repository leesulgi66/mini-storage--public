<template>
  <div :style="{ display: 'flex', minHeight: '100vh', background: '#f5f5f3', fontFamily: `'DM Sans', sans-serif` }">
    <Sidebar />
    <main :style="{ marginLeft: '220px', flex: 1, padding: '2rem' }">
      <div :style="{ marginBottom: '1.75rem' }">
        <h1 :style="{ fontSize: '20px', fontWeight: 500, color: '#111', letterSpacing: '-0.4px' }">접속 로그</h1>
        <p :style="{ fontSize: '13px', color: '#999', marginTop: '3px' }">관리자 접속 기록을 확인하세요</p>
      </div>

      <div :style="{ background: '#fff', border: '1px solid #e8e8e6', borderRadius: '10px', padding: '1.25rem' }">
        <div v-if="loading" :style="emptyStyle">불러오는 중...</div>
        <table v-else-if="pageItems.length" :style="{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }">
          <thead>
            <tr>
              <th v-for="h in ['#', '시간', '액션', '대상', 'IP']" :key="h" :style="thStyle">{{ h }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in pageItems" :key="log.id"
              @mouseenter="e => e.currentTarget.style.background = '#fafaf8'"
              @mouseleave="e => e.currentTarget.style.background = ''">
              <td :style="tdStyle">{{ log.id }}</td>
              <td :style="tdStyle">{{ formatDate(log.createdAt) }}</td>
              <td :style="tdStyle">
                <span :style="{
                  display: 'inline-block', fontSize: '11px', padding: '2px 8px', borderRadius: '4px',
                  background: log.action === 'auth.login' ? '#eaf3de' : '#f0f0ee',
                  color: log.action === 'auth.login' ? '#3b6d11' : '#666'
                }">{{ log.action }}</span>
              </td>
              <td :style="tdStyle">{{ log.target || '-' }}</td>
              <td :style="tdStyle">{{ log.ip }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else :style="emptyStyle">로그가 없습니다</div>

        <!-- 페이지네이션 -->
        <div v-if="totalPages > 1" :style="{ display: 'flex', justifyContent: 'center', gap: '6px', marginTop: '1.5rem' }">
          <button @click="currentPage > 1 && currentPage--" :disabled="currentPage === 1" :style="pageBtnStyle(currentPage === 1)">이전</button>
          <button v-for="i in totalPages" :key="i" @click="currentPage = i" :style="pageBtnStyle(false, i === currentPage)">{{ i }}</button>
          <button @click="currentPage < totalPages && currentPage++" :disabled="currentPage === totalPages" :style="pageBtnStyle(currentPage === totalPages)">다음</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import Sidebar from '../components/Sidebar.vue';
import api from '../api/axios';

const PAGE_SIZE = 10;
const allLogs = ref([]);
const currentPage = ref(1);
const loading = ref(true);

const totalPages = computed(() => Math.ceil(allLogs.value.length / PAGE_SIZE));
const pageItems = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE;
  return allLogs.value.slice(start, start + PAGE_SIZE);
});

function formatDate(str) {
  if (!str) return '-';
  return new Date(str).toLocaleDateString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  });
}

onMounted(async () => {
  try {
    const res = await api.get('/api/v1/audit-logs?limit=200');
    allLogs.value = res.data || [];
  } catch(e) { console.error(e); }
  finally { loading.value = false; }
});

const thStyle = { textAlign: 'left', padding: '8px 12px', fontSize: '11px', color: '#999', fontWeight: 500, borderBottom: '1px solid #e8e8e6' };
const tdStyle = { padding: '10px 12px', borderBottom: '1px solid #f0f0ee', color: '#333' };
const emptyStyle = { textAlign: 'center', padding: '4rem', color: '#bbb', fontSize: '14px' };
const pageBtnStyle = (disabled, active) => ({
  padding: '6px 12px', fontSize: '13px',
  border: `1px solid ${active ? '#111' : '#e0e0de'}`,
  borderRadius: '6px', background: active ? '#111' : '#fff',
  color: disabled ? '#ccc' : active ? '#fff' : '#555',
  cursor: disabled ? 'not-allowed' : 'pointer', fontFamily: 'inherit'
});
</script>