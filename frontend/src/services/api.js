import axios from 'axios';

const API_BASE = 'http://localhost:8080';

const api = axios.create({
    baseURL: API_BASE,
    headers: { 'Content-Type': 'application/json' }
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

let refreshing = false;
let queue = [];

const processQueue = (error, token = null) => {
    queue.forEach((prom) => {
        if (error) prom.reject(error);
        else prom.resolve(token);
    });
    queue = [];
};

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const original = error.config;
        if (error.response?.status !== 401 || original._retry) {
            return Promise.reject(error);
        }

        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
            clearSession();
            return Promise.reject(error);
        }

        if (refreshing) {
            return new Promise((resolve, reject) => {
                queue.push({ resolve, reject });
            }).then((token) => {
                original.headers.Authorization = `Bearer ${token}`;
                return api(original);
            });
        }

        original._retry = true;
        refreshing = true;

        try {
            const res = await axios.post(`${API_BASE}/api/auth/refresh`, { refreshToken });
            const { accessToken, refreshToken: newRefresh, user } = res.data;
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', newRefresh);
            localStorage.setItem('user', JSON.stringify(user));
            processQueue(null, accessToken);
            original.headers.Authorization = `Bearer ${accessToken}`;
            return api(original);
        } catch (refreshError) {
            processQueue(refreshError, null);
            clearSession();
            return Promise.reject(refreshError);
        } finally {
            refreshing = false;
        }
    }
);

export function clearSession() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
}

export function getStoredUser() {
    try {
        return JSON.parse(localStorage.getItem('user'));
    } catch {
        return null;
    }
}

export function hasRole(minRole) {
    const user = getStoredUser();
    if (!user?.role) return false;
    const order = ['USER', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN'];
    return order.indexOf(user.role) >= order.indexOf(minRole);
}

export default api;
