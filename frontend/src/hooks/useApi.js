import { useState } from 'react';

export const useApi = (apiFunc) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const execute = async (...args) => {
        setLoading(true);
        setError(null);
        try {
            const response = await apiFunc(...args);
            return response.data;
        } catch (err) {
            // Беремо повідомлення з вашого GlobalExceptionHandler
            const message = err.response?.data?.message || 'Wystąpił nieoczekiwany błąd';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return { loading, error, execute };
};
