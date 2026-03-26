import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090';

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
});

// 토큰 갱신 중인지 확인하는 플래그와 대기 중인 요청 큐
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

// 1. 요청 인터셉터
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => Promise.reject(error));

// 2. 응답 인터셉터
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 401 에러이고, 이미 재시도한 요청이 아닐 때
    if (error.response?.status === 401 && !originalRequest._retry) {
      
      if (isRefreshing) {
        // 이미 토큰 갱신 중이라면 요청을 큐에 담고 대기
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(token => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch(err => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) throw new Error('No refresh token');

        // 주의: 인스턴스(api)가 아닌 생 axios를 써야 무한 루프에 안 빠집니다.
        const res = await axios.post(`${BASE_URL}/api/v1/auth/refresh`, {
          refreshToken
        });

        const { accessToken, refreshToken: newRefreshToken, role } = res.data;

        localStorage.setItem('accessToken', accessToken);
        if (newRefreshToken) localStorage.setItem('refreshToken', newRefreshToken);
        if (role) localStorage.setItem('role', role);

        // 대기 중이던 다른 요청들에게 새 토큰 전달 및 실행
        processQueue(null, accessToken);
        
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        // 리프레시 토큰마저 만료된 경우
        processQueue(refreshError, null);
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;