const API_BASE = '';

async function apiFetch(url, options = {}) {
    let token = localStorage.getItem('accessToken');

    // 기본 헤더 설정
    const fetchOptions = {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            ...options.headers,
        }
    };

    let res = await fetch(`${API_BASE}${url}`, fetchOptions);

    // 401 에러 발생 시
    if (res.status === 401) {
        const refreshed = await tryRefresh();

        if (refreshed) {
            // 갱신 성공 시 새 토큰으로 헤더 교체 후 딱 한 번만 재시도
            const newToken = localStorage.getItem('accessToken');
            fetchOptions.headers['Authorization'] = `Bearer ${newToken}`;
            res = await fetch(`${API_BASE}${url}`, fetchOptions);
        } else {
            // 갱신 실패 시 로그아웃
            logout();
            return null;
        }
    }

    return res;
}

async function tryRefresh() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return false;

    try {
        const res = await fetch(`${API_BASE}/api/v1/auth/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
        });

        if (!res.ok) return false;

        const data = await res.json();
        localStorage.setItem('accessToken', data.accessToken);
        // 서버에서 새 리프레시 토큰을 주는 경우에만 저장
        if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken);
        if (data.role) localStorage.setItem('role', data.role);

        return true;
    } catch(e) {
        console.error("Refresh Error:", e);
        return false;
    }
}

function logout() {
    localStorage.clear(); // 깔끔하게 전체 삭제 권장
    location.href = '/index.html';
}