import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/Login.vue';
import Dashboard from '../views/Dashboard.vue';
import Images from '../views/Images.vue';
import Upload from '../views/Upload.vue';
import Logs from '@/views/Logs.vue';

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', component: Login },
  {
    path: '/dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/images',
    component: Images,
    meta: { requiresAuth: true }
  },
  {
    path: '/upload',
    component: Upload,
    meta: { requiresAuth: true }
  },
  { 
    path: '/logs', 
    component: Logs, 
    meta: { requiresAuth: true } 
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !localStorage.getItem('accessToken')) {
    next('/login');
  } else {
    next();
  }
});

export default router;