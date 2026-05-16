import axios from 'axios';
import api, { clearSession } from './api';

const AUTH_URL = 'http://localhost:8080/api/auth/';

const persistSession = (data) => {
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('user', JSON.stringify(data.user));
};

const register = (username, email, password) => {
    return axios.post(AUTH_URL + 'signup', {
        nume: username,
        email,
        password
    });
};

const login = async (email, password) => {
    const response = await axios.post(AUTH_URL + 'signin', { email, password });
    persistSession(response.data);
    return response.data.user;
};

const logout = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    try {
        if (refreshToken) {
            await api.post('/api/auth/logout', { refreshToken });
        }
    } finally {
        clearSession();
    }
};

const me = async () => {
    const response = await api.get('/api/auth/me');
    localStorage.setItem('user', JSON.stringify(response.data));
    return response.data;
};

export default { register, login, logout, me, persistSession };
