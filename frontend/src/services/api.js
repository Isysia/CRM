import axios from 'axios';
import { API_BASE_URL } from '../utils/constants';

// Create axios instance
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor (додає token до headers)
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('authToken');
        if (token && !config.headers.Authorization) {
            config.headers.Authorization = `Basic ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor (obsługa błędów)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // Unauthorized - wyloguj
            localStorage.removeItem('authToken');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default api;

// === AUTH API ===
export const authAPI = {
    login: async (username, password) => {
        const token = btoa(`${username}:${password}`);

        // ✅ ВИПРАВЛЕНО: використовуємо /users/me
        const response = await api.get('/users/me', {
            headers: {
                Authorization: `Basic ${token}`,
            },
        });

        // ✅ response.data тепер це UserResponseDTO з ролями
        const user = response.data;

        return { token, user };
    },
    logout: () => {
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
    },
};

// === CUSTOMER API ===
export const customerAPI = {
    getAll: () => api.get('/customers'),
    getById: (id) => api.get(`/customers/${id}`),
    create: (data) => api.post('/customers', data),
    update: (id, data) => api.put(`/customers/${id}`, data),
    delete: (id) => api.delete(`/customers/${id}`),
};

// === OFFER API ===
export const offerAPI = {
    getAll: () => api.get('/offers'),
    getById: (id) => api.get(`/offers/${id}`),
    create: (data) => api.post('/offers', data),
    update: (id, data) => api.put(`/offers/${id}`, data),
    delete: (id) => api.delete(`/offers/${id}`),
};

// === TASK API ===
export const taskAPI = {
    getAll: () => api.get('/tasks'),
    getById: (id) => api.get(`/tasks/${id}`),
    create: (data) => api.post('/tasks', data),
    update: (id, data) => api.put(`/tasks/${id}`, data),
    delete: (id) => api.delete(`/tasks/${id}`),
};
