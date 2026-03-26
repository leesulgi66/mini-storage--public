<template>
  <div :style="{ display: 'flex', minHeight: '100vh', background: '#f5f5f3', fontFamily: `'DM Sans', sans-serif` }">
    <Sidebar />
    <main :style="{ marginLeft: '220px', flex: 1, padding: '2rem', maxWidth: '860px' }">
      <div :style="{ marginBottom: '1.75rem' }">
        <h1 :style="{ fontSize: '20px', fontWeight: 500, color: '#111', letterSpacing: '-0.4px' }">이미지 업로드</h1>
        <p :style="{ fontSize: '13px', color: '#999', marginTop: '3px' }">이미지를 업로드하면 썸네일(xs/sm/md/lg)이 자동으로 생성됩니다</p>
      </div>

      <div :style="sectionStyle">
        <div :style="{ fontSize: '14px', fontWeight: 500, color: '#111', marginBottom: '1.25rem' }">파일 선택</div>

        <!-- 드롭존 -->
        <div @click="fileInput.click()" @dragover.prevent="dragging = true"
          @dragleave="dragging = false" @drop.prevent="onDrop"
          :style="{ border: `2px dashed ${dragging ? '#aaa' : '#e0e0de'}`, borderRadius: '10px', padding: '3rem 2rem', textAlign: 'center', cursor: 'pointer', background: dragging ? '#fafaf8' : '#fff', transition: 'all 0.15s' }">
          <div :style="{ width: '44px', height: '44px', margin: '0 auto 1rem', background: '#f0f0ee', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }">
            <svg viewBox="0 0 22 22" fill="none" stroke="#aaa" stroke-width="1.5" width="22" height="22">
              <path d="M11 14V4M8 7l3-3 3 3"/><path d="M3 15v3a1 1 0 001 1h14a1 1 0 001-1v-3"/>
            </svg>
          </div>
          <div :style="{ fontSize: '14px', fontWeight: 500, color: '#333', marginBottom: '4px' }">파일을 드래그하거나 클릭하여 선택</div>
          <div :style="{ fontSize: '13px', color: '#bbb' }">JPG, PNG, GIF, WEBP · 최대 20MB</div>
        </div>
        <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/gif,image/webp"
          multiple style="display:none" @change="onFileChange" />

        <!-- 미리보기 -->
        <div v-if="selectedFiles.length" :style="{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '10px', marginTop: '1.25rem' }">
          <div v-for="(file, i) in selectedFiles" :key="i"
            :style="{ border: '1px solid #e8e8e6', borderRadius: '8px', overflow: 'hidden', position: 'relative' }">
            <div :style="{ height: '90px', background: '#f0f0ee', overflow: 'hidden' }">
              <img :src="previewUrls[i]" :style="{ width: '100%', height: '100%', objectFit: 'cover' }" />
            </div>
            <div :style="{ padding: '8px 10px' }">
              <div :style="{ fontSize: '12px', color: '#333', fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }">{{ file.name }}</div>
              <div :style="{ fontSize: '11px', color: '#bbb', marginTop: '2px' }">{{ formatBytes(file.size) }}</div>
            </div>
            <button @click="removeFile(i)" :style="{ position: 'absolute', top: '5px', right: '5px', width: '20px', height: '20px', background: 'rgba(0,0,0,0.45)', borderRadius: '50%', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 0 }">
              <svg viewBox="0 0 10 10" fill="none" stroke="white" stroke-width="1.5" width="10" height="10"><path d="M2 2l6 6M8 2l-6 6"/></svg>
            </button>
          </div>
        </div>

        <!-- 업로드 버튼 -->
        <div v-if="selectedFiles.length" :style="{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '1.25rem' }">
          <span :style="{ fontSize: '13px', color: '#999' }">{{ selectedFiles.length }}개 파일 선택됨</span>
          <button @click="startUpload" :disabled="uploading"
            :style="{ padding: '10px 24px', fontSize: '14px', fontWeight: 500, background: uploading ? '#ccc' : '#111', color: '#fff', border: 'none', borderRadius: '8px', cursor: uploading ? 'not-allowed' : 'pointer', fontFamily: 'inherit' }">
            업로드 시작
          </button>
        </div>

        <!-- 프로그레스 -->
        <div v-if="uploading" :style="{ marginTop: '1rem' }">
          <div :style="{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#999', marginBottom: '6px' }">
            <span>{{ progressText }}</span>
            <span>{{ progress }}%</span>
          </div>
          <div :style="{ height: '4px', background: '#f0f0ee', borderRadius: '2px', overflow: 'hidden' }">
            <div :style="{ height: '100%', background: '#111', borderRadius: '2px', width: progress + '%', transition: 'width 0.3s' }" />
          </div>
        </div>
      </div>

      <!-- 결과 -->
      <div v-if="results.length" :style="sectionStyle">
        <div :style="{ fontSize: '14px', fontWeight: 500, color: '#111', marginBottom: '1.25rem' }">업로드 결과</div>
        <div :style="{ display: 'flex', flexDirection: 'column', gap: '8px' }">
          <div v-for="(r, i) in results" :key="i"
            :style="{ display: 'flex', alignItems: 'center', gap: '10px', padding: '10px 12px', border: '1px solid #e8e8e6', borderRadius: '8px' }">
            <div :style="{ width: '28px', height: '28px', borderRadius: '6px', background: r.success ? '#eaf3de' : '#fcebeb', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }">
              <svg v-if="r.success" viewBox="0 0 16 16" width="16" height="16" fill="none" stroke="#3b6d11" stroke-width="1.5"><path d="M3 8l4 4 6-6"/></svg>
              <svg v-else viewBox="0 0 16 16" width="16" height="16" fill="none" stroke="#a32d2d" stroke-width="1.5"><path d="M4 4l8 8M12 4l-8 8"/></svg>
            </div>
            <span :style="{ fontSize: '13px', color: '#333', flex: 1, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }">{{ r.name }}</span>
            <span v-if="r.msg" :style="{ fontSize: '11px', color: '#bbb', flexShrink: 0 }">{{ r.msg }}</span>
            <span :style="{ fontSize: '11px', padding: '3px 8px', borderRadius: '4px', background: r.success ? '#eaf3de' : '#fcebeb', color: r.success ? '#3b6d11' : '#a32d2d', flexShrink: 0 }">
              {{ r.success ? '완료' : '실패' }}
            </span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Sidebar from '../components/Sidebar.vue';
import api from '../api/axios';

const fileInput = ref(null);
const selectedFiles = ref([]);
const previewUrls = ref([]);
const dragging = ref(false);
const uploading = ref(false);
const progress = ref(0);
const progressText = ref('');
const results = ref([]);

function formatBytes(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / 1024 / 1024).toFixed(1) + ' MB';
}

function addFiles(files) {
  const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  for (const file of files) {
    if (!allowed.includes(file.type) || file.size > 20 * 1024 * 1024) continue;
    if (!selectedFiles.value.find(f => f.name === file.name && f.size === file.size)) {
      selectedFiles.value.push(file);
      previewUrls.value.push(URL.createObjectURL(file));
    }
  }
}

function onFileChange(e) { addFiles(e.target.files); e.target.value = ''; }
function onDrop(e) { dragging.value = false; addFiles(e.dataTransfer.files); }
function removeFile(i) {
  selectedFiles.value.splice(i, 1);
  previewUrls.value.splice(i, 1);
}

async function startUpload() {
  if (localStorage.getItem('role') === 'VIEWER') {
    alert('열람 전용 계정은 업로드할 수 없습니다.');
    return;
  }
  if (!selectedFiles.value.length) return;
  uploading.value = true;
  results.value = [];

  const newResults = [];
  for (let i = 0; i < selectedFiles.value.length; i++) {
    const file = selectedFiles.value[i];
    progress.value = Math.round((i / selectedFiles.value.length) * 100);
    progressText.value = `업로드 중... (${i + 1}/${selectedFiles.value.length})`;

    try {
      const formData = new FormData();
      formData.append('file', file);
      await api.post('/api/v1/images', formData);
      newResults.push({ name: file.name, success: true });
    } catch(e) {
      newResults.push({ name: file.name, success: false, msg: e.response?.data?.message || '업로드 실패' });
    }
    results.value = [...newResults];
  }

  progress.value = 100;
  progressText.value = '업로드 완료';
  selectedFiles.value = [];
  previewUrls.value = [];
  uploading.value = false;
}

const sectionStyle = { background: '#fff', border: '1px solid #e8e8e6', borderRadius: '10px', padding: '1.5rem', marginBottom: '1.25rem' };
</script>