<template>
  <aside :style="sidebarStyle">
    <!-- 로고 -->
    <div :style="logoWrapStyle">
      <div :style="logoIconStyle">
        <svg viewBox="0 0 18 18" fill="none" width="15" height="15">
          <rect x="2" y="2" width="6" height="6" rx="1.5" fill="white"/>
          <rect x="10" y="2" width="6" height="6" rx="1.5" fill="white" opacity="0.5"/>
          <rect x="2" y="10" width="6" height="6" rx="1.5" fill="white" opacity="0.5"/>
          <rect x="10" y="10" width="6" height="6" rx="1.5" fill="white" opacity="0.3"/>
        </svg>
      </div>
      <div>
        <div :style="{ fontSize: '14px', fontWeight: 500, color: '#111', letterSpacing: '-0.3px' }">mini-storage</div>
        <div :style="{ fontSize: '11px', color: '#999' }">admin console</div>
      </div>
    </div>

    <!-- 메뉴 -->
    <div :style="{ fontSize: '11px', color: '#bbb', letterSpacing: '0.5px', textTransform: 'uppercase', padding: '0 0.5rem', marginBottom: '0.5rem' }">메뉴</div>
    <router-link v-for="item in navItems" :key="item.to" :to="item.to" :style="navLinkStyle" active-class="active-nav">
      <span v-html="item.icon" :style="{ display: 'flex', alignItems: 'center' }"></span>
      {{ item.label }}
    </router-link>

    <div :style="{ flex: 1 }" />

    <!-- 유저 -->
    <div :style="{ display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 10px' }">
      <div :style="avatarStyle">{{ initials }}</div>
      <div>
        <div :style="{ fontSize: '13px', fontWeight: 500, color: '#111' }">{{ username }}</div>
        <div :style="{ fontSize: '11px', color: '#999' }">관리자</div>
      </div>
    </div>
    <button @click="logout" :style="logoutBtnStyle">로그아웃</button>
  </aside>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const token = localStorage.getItem('accessToken');
let email = '';
try {
  const payload = JSON.parse(atob(token.split('.')[1]));
  email = payload.email || '';
} catch(e) {}

const initials = computed(() => email.substring(0, 2).toUpperCase());
const username = computed(() => email.split('@')[0]);

const navItems = [
  {
    to: '/dashboard', label: '대시보드',
    icon: '<svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16"><rect x="1" y="1" width="6" height="6" rx="1"/><rect x="9" y="1" width="6" height="6" rx="1"/><rect x="1" y="9" width="6" height="6" rx="1"/><rect x="9" y="9" width="6" height="6" rx="1"/></svg>'
  },
  {
    to: '/logs', label: '접속 로그',
    icon: '<svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16"><path d="M2 4h12M2 8h12M2 12h8"/></svg>'
  },
  {
    to: '/images', label: '이미지 관리',
    icon: '<svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16"><rect x="1" y="3" width="14" height="10" rx="1.5"/><circle cx="5.5" cy="7.5" r="1.5"/><path d="M15 11l-4-4-6 6"/></svg>'
  },
  {
    to: '/upload', label: '업로드',
    icon: '<svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16"><path d="M8 11V3M5 6l3-3 3 3"/><path d="M2 13h12"/></svg>'
  },
];

function logout() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('role');
  router.push('/login');
}

const sidebarStyle = {
  width: '220px', background: '#fff', borderRight: '1px solid #e8e8e6',
  padding: '1.5rem 1rem', display: 'flex', flexDirection: 'column',
  position: 'fixed', top: 0, left: 0, height: '100vh',
  fontFamily: "'DM Sans', sans-serif", boxSizing: 'border-box', overflowY: 'auto'
};
const logoWrapStyle = {
  display: 'flex', alignItems: 'center', gap: '8px',
  padding: '0 0.5rem', marginBottom: '2rem'
};
const logoIconStyle = {
  width: '30px', height: '30px', background: '#111', borderRadius: '6px',
  display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0
};
const navLinkStyle = {
  display: 'flex', alignItems: 'center', gap: '8px',
  padding: '8px 10px', borderRadius: '7px', fontSize: '14px',
  color: '#555', marginBottom: '2px', textDecoration: 'none'
};
const avatarStyle = {
  width: '28px', height: '28px', background: '#e8e8e6', borderRadius: '50%',
  display: 'flex', alignItems: 'center', justifyContent: 'center',
  fontSize: '11px', fontWeight: 500, color: '#555', flexShrink: 0
};
const logoutBtnStyle = {
  width: '100%', marginTop: '0.5rem', padding: '8px 10px', fontSize: '13px',
  color: '#999', background: 'none', border: 'none', cursor: 'pointer',
  textAlign: 'left', fontFamily: 'inherit', borderRadius: '7px'
};
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500&display=swap');

.active-nav {
  background: #111 !important;
  color: #fff !important;
}

.active-nav svg {
  stroke: #fff;
}
</style>