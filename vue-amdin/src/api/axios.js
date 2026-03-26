import axios from 'axios';
import router from '@/router';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090';

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  // 💡 중요: 인스턴스 생성 시 기본 Content-Type을 설정하지 않습니다.
});

// 1. 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // 💡 핵심 로직: 
    // 데이터가 FormData가 아닐 때만 application/json 설정
    // FormData인 경우 Axios가 헤더를 비워두면 브라우저가 자동으로 Multipart 형식을 맞춤
    if (config.data && !(config.data instanceof FormData)) {
      config.headers['Content-Type'] = 'application/json';
    } else {
      // FormData인 경우 수동으로 설정된 Content-Type이 있다면 제거
      delete config.headers['Content-Type'];
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// 2. 응답 인터셉터
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 401 Unauthorized 처리 및 토큰 재발급
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) throw new Error('No refresh token');

        // 기본 axios 객체로 갱신 요청 (무한 루프 방지)
        const res = await axios.post(`${BASE_URL}/api/v1/auth/refresh`, {
          refreshToken,
        });

        const { accessToken, refreshToken: newRefreshToken, role } = res.data;

        localStorage.setItem('accessToken', accessToken);
        if (newRefreshToken) localStorage.setItem('refreshToken', newRefreshToken);
        if (role) localStorage.setItem('role', role);

        // 새 토큰으로 기존 요청 재시도
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        handleLogout();
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

function handleLogout() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('role');
  router.push('/login');
}

export default api;
export { BASE_URL };