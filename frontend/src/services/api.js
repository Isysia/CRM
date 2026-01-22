import axios from 'axios';

const getBaseURL = () => {
    if (import.meta.env.VITE_API_BASE_URL) {
        return import.meta.env.VITE_API_BASE_URL;
    }
    return '/api';
};

const API_BASE_URL = getBaseURL();
console.log('API_BASE_URL:', API_BASE_URL);

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: { 'Content-Type': 'application/json' },
    timeout: 30000,
});

api.interceptors.request.use(
    (config) => {
        const auth = localStorage.getItem('auth');
        if (auth) {
            config.headers.Authorization = `Basic ${auth}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 && !window.location.pathname.includes('/login')) {
            localStorage.removeItem('auth');
            localStorage.removeItem('username');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export const customerAPI = {
    getAll: (params) => api.get('/customers', { params }),
    getById: (id) => api.get(`/customers/${id}`),
    create: (data) => api.post('/customers', data),
    update: (id, data) => api.put(`/customers/${id}`, data),
    delete: (id) => api.delete(`/customers/${id}`),
};

export const offerAPI = {
    getAll: (params) => api.get('/offers', { params }),
    getById: (id) => api.get(`/offers/${id}`),
    create: (data) => api.post('/offers', data),
    update: (id, data) => api.put(`/offers/${id}`, data),
    delete: (id) => api.delete(`/offers/${id}`),
    // ✅ ДОДАНО: Метод для зміни статусу
    updateStatus: (id, status) => api.patch(`/offers/${id}/status`, { status }),
};

export const taskAPI = {
    getAll: (params) => api.get('/tasks', { params }),
    getById: (id) => api.get(`/tasks/${id}`),
    create: (data) => api.post('/tasks', data),
    update: (id, data) => api.put(`/tasks/${id}`, data),
    delete: (id) => api.delete(`/tasks/${id}`),
    updateStatus: (id, status) => api.patch(`/tasks/${id}/status`, { status }),
};

export const userAPI = {
    getAll: () => api.get('/users'),
    changeRole: (id, role) => api.patch(`/users/${id}/role`, { role }),
};

export const authAPI = {
    login: async (username, password) => {
        const credentials = btoa(`${username}:${password}`);
        try {
            const res = await api.get('/users/me', {
                headers: { Authorization: `Basic ${credentials}` },
            });

            localStorage.setItem('auth', credentials);
            localStorage.setItem('username', username);

            return { token: credentials, user: res.data };
        } catch (error) {
            if (error.response?.status === 401) throw new Error('Nieprawidłowe dane logowania');
            throw new Error('Błąd połączenia z serwerem');
        }
    },
    register: async (data) => {
        return api.post('/auth/register', data);
    },
    logout: () => {
        localStorage.removeItem('auth');
        localStorage.removeItem('username');
    },
};

export default api;