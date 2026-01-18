// src/services/api.js
import axios from 'axios';

const getBaseURL = () => {
    // Якщо є env variable (для development)
    if (import.meta.env.VITE_API_BASE_URL) {
        return import.meta.env.VITE_API_BASE_URL;
    }

    // Для production/Kubernetes - завжди використовуй /api
    return '/api';
};

// W Kubernetes używamy relative path (/api) który jest proxy przez Nginx
// W development mode używamy pełnego URL z .env
const API_BASE_URL = getBaseURL();

console.log('API_BASE_URL:', API_BASE_URL);

// Create axios instance with default config
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    // Timeout po 30 sekundach
    timeout: 30000,
});

// Request interceptor - dodaj Basic Auth jeśli istnieje
api.interceptors.request.use(
    (config) => {
        const auth = localStorage.getItem('auth');
        if (auth) {
            config.headers.Authorization = `Basic ${auth}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor - obsługa błędów
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // Jeśli 401 Unauthorized, wyloguj użytkownika
        if (error.response?.status === 401) {
            localStorage.removeItem('auth');
            localStorage.removeItem('username');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

// Export API functions
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
};

export const taskAPI = {
    getAll: (params) => api.get('/tasks', { params }),
    getById: (id) => api.get(`/tasks/${id}`),
    create: (data) => api.post('/tasks', data),
    update: (id, data) => api.put(`/tasks/${id}`, data),
    delete: (id) => api.delete(`/tasks/${id}`),
    toggleComplete: (id) => api.patch(`/tasks/${id}/toggle-complete`),
};



export const authAPI = {
    login: async (username, password) => {
        const credentials = btoa(`${username}:${password}`);

        try {
            // Test credentials
            await api.get('/customers', {
                headers: {
                    Authorization: `Basic ${credentials}`,
                },
            });

            // Зберігаємо auth в localStorage
            localStorage.setItem('auth', credentials);
            localStorage.setItem('username', username);

            // Повертаємо правильну структуру для useAuth
            return {
                token: credentials,
                user: {
                    username,
                    // Тут можна додати роль якщо backend повертає
                },
            };
        } catch (error) {
            if (error.response?.status === 401) {
                throw new Error('Nieprawidłowe dane logowania');
            }
            throw new Error('Błąd połączenia z serwerem');
        }
    },

    logout: () => {
        localStorage.removeItem('auth');
        localStorage.removeItem('username');
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
    },
};

export default api;
