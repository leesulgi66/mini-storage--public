<template>
  <div :style="pageStyle">
    <div :style="cardStyle">
      <!-- 로고 -->
      <div :style="{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '2rem' }">
        <div :style="logoIconStyle">
          <svg viewBox="0 0 18 18" fill="none" width="18" height="18">
            <rect x="2" y="2" width="6" height="6" rx="1.5" fill="white"/>
            <rect x="10" y="2" width="6" height="6" rx="1.5" fill="white" opacity="0.5"/>
            <rect x="2" y="10" width="6" height="6" rx="1.5" fill="white" opacity="0.5"/>
            <rect x="10" y="10" width="6" height="6" rx="1.5" fill="white" opacity="0.3"/>
          </svg>
        </div>
        <div>
          <div :style="{ fontSize: '15px', fontWeight: 500, color: '#111' }">mini-storage</div>
          <div :style="{ fontSize: '12px', color: '#888' }">admin console</div>
        </div>
      </div>

      <h1 :style="{ fontSize: '22px', fontWeight: 500, color: '#111', letterSpacing: '-0.5px', marginBottom: '0.4rem' }">로그인</h1>
      <p :style="{ fontSize: '14px', color: '#888', marginBottom: '2rem' }">관리자 계정으로 로그인하세요</p>

      <div :style="{ marginBottom: '1.25rem' }">
        <label :style="labelStyle">이메일</label>
        <input v-model="email" type="email" placeholder="admin@example.com"
          :style="inputStyle" @keydown.enter="handleLogin" />
      </div>

      <div :style="{ marginBottom: '1.25rem' }">
        <label :style="labelStyle">비밀번호</label>
        <input v-model="password" type="password" placeholder="••••••••"
          :style="inputStyle" @keydown.enter="handleLogin" />
      </div>

      <p v-if="error" :style="{ fontSize: '13px', color: '#d85a30', marginBottom: '0.75rem' }">{{ error }}</p>

      <button @click="handleLogin" :disabled="loading" :style="btnStyle">
        {{ loading ? '로그인 중...' : '로그인' }}
      </button>

      <button @click="handleViewerLogin" :disabled="viewerLoading"
        :style="{ ...btnStyle, background: '#fff', color: '#555', border: '1px solid #ddd', marginTop: '8px' }">
        {{ viewerLoading ? '로그인 중...' : 'Viewer로 둘러보기' }}
      </button>

      <hr :style="{ border: 'none', borderTop: '1px solid #f0f0ee', margin: '1.75rem 0' }" />
      <p :style="{ fontSize: '12px', color: '#bbb', textAlign: 'center' }">mini-storage v1.0.0</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api/axios';

const router = useRouter();
const email = ref('');
const password = ref('');
const error = ref('');
const loading = ref(false);
const viewerLoading = ref(false);

async function handleLogin() {
  if (!email.value || !password.value) { error.value = '이메일과 비밀번호를 입력해주세요'; return; }
  loading.value = true;
  error.value = '';
  try {
    const res = await api.post('/api/v1/auth/login', { email: email.value, password: password.value });
    localStorage.setItem('accessToken', res.data.accessToken);
    localStorage.setItem('refreshToken', res.data.refreshToken);
    localStorage.setItem('role', res.data.role);
    router.push('/dashboard');
  } catch(e) {
    error.value = e.response?.data?.message || '로그인에 실패했습니다';
  } finally {
    loading.value = false;
  }
}

async function handleViewerLogin() {
  viewerLoading.value = true;
  error.value = '';
  try {
    const res = await api.post('/api/v1/auth/login', {
      email: 'viewer@example.com',
      password: 'viewer1234'
    });
    localStorage.setItem('accessToken', res.data.accessToken);
    localStorage.setItem('refreshToken', res.data.refreshToken);
    localStorage.setItem('role', res.data.role);
    router.push('/dashboard');
  } catch(e) {
    error.value = 'Viewer 로그인에 실패했습니다';
  } finally {
    viewerLoading.value = false;
  }
}

const pageStyle = { minHeight: '100vh', background: '#f5f5f3', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: "'DM Sans', sans-serif" };
const cardStyle = { background: '#fff', border: '1px solid #e8e8e6', borderRadius: '12px', padding: '2.5rem', width: '100%', maxWidth: '400px' };
const logoIconStyle = { width: '36px', height: '36px', background: '#111', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center' };
const labelStyle = { display: 'block', fontSize: '13px', color: '#666', marginBottom: '6px' };
const inputStyle = { width: '100%', padding: '10px 12px', fontSize: '14px', border: '1px solid #ddd', borderRadius: '8px', outline: 'none', fontFamily: 'inherit', boxSizing: 'border-box' };
const btnStyle = { width: '100%', padding: '11px', fontSize: '14px', fontWeight: 500, background: '#111', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer', fontFamily: 'inherit' };
</script>