import { useAuth } from './useAuth';

export const useRole = () => {
    const { user } = useAuth();

    const getUserRole = () => {
        if (!user) return null;

        // Якщо є roles array (старий код)
        if (user.roles && Array.isArray(user.roles)) {
            if (user.roles.includes('ROLE_ADMIN') || user.roles.includes('ADMIN')) {
                return 'ROLE_ADMIN';
            }
            if (user.roles.includes('ROLE_MANAGER') || user.roles.includes('MANAGER')) {
                return 'ROLE_MANAGER';
            }
            if (user.roles.includes('ROLE_USER') || user.roles.includes('USER')) {
                return 'ROLE_USER';
            }
        }

        // Якщо немає roles, визначаємо по username (для Kubernetes)
        const username = user.username?.toLowerCase();
        if (username === 'admin') return 'ROLE_ADMIN';
        if (username === 'manager') return 'ROLE_MANAGER';
        if (username === 'user') return 'ROLE_USER';

        return null;
    };

    const role = getUserRole();

    const hasRole = (checkRole) => {
        if (!role) return false;
        const normalizedRole = checkRole.startsWith('ROLE_') ? checkRole : `ROLE_${checkRole}`;
        return role === normalizedRole;
    };

    const isAdmin = () => role === 'ROLE_ADMIN';
    const isManager = () => role === 'ROLE_MANAGER' || role === 'ROLE_ADMIN';
    const isUser = () => role === 'ROLE_USER' || role === 'ROLE_MANAGER' || role === 'ROLE_ADMIN';

    return { hasRole, isAdmin, isManager, isUser, role };
};