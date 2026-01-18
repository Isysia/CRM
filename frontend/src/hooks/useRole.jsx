import { useAuth } from './useAuth';

export const useRole = () => {
    const { user } = useAuth();

    const hasRole = (role) => {
        if (!user || !user.roles) return false;
        // Перевіряємо з ROLE_ і без
        return user.roles.includes(`ROLE_${role}`) || user.roles.includes(role);
    };

    const isAdmin = () => hasRole('ADMIN');
    const isManager = () => hasRole('MANAGER') || hasRole('ADMIN');
    const isUser = () => hasRole('USER') || hasRole('MANAGER') || hasRole('ADMIN');

    return { hasRole, isAdmin, isManager, isUser };
};