import { useState, useEffect } from 'react';
import { userAPI } from '../../services/api';

export default function UsersList() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        try {
            setLoading(true);
            const response = await userAPI.getAll();
            setUsers(response.data || []);
        } catch (err) {
            console.error("Failed to load users:", err);
            setError('Nie udało się pobrać listy użytkowników.');
        } finally {
            setLoading(false);
        }
    };

    const handleRoleChange = async (userId, newRole) => {
        const confirmMsg = `Czy na pewno chcesz zmienić rolę tego użytkownika na ${newRole}?`;
        if (!window.confirm(confirmMsg)) return;

        try {
            await userAPI.changeRole(userId, newRole);
            await loadUsers();
            alert('Rola została zmieniona pomyślnie!');
        } catch (err) {
            console.error('Role change error:', err);

            if (err.response?.status === 403) {
                alert('Nie masz uprawnień do zmiany ról użytkowników');
            } else if (err.response?.status === 404) {
                alert('Użytkownik nie został znaleziony');
            } else {
                alert('Nie udało się zmienić roli: ' + (err.response?.data?.message || err.message || 'Błąd serwera'));
            }
        }
    };

    // ✅ Допоміжна функція для очищення ролі від "ROLE_"
    const getRoleValue = (roles) => {
        if (!roles || roles.length === 0) return 'USER';
        const role = roles[0];
        return role.replace('ROLE_', '');
    };

    // ✅ Перевірка, чи користувач має роль ADMIN
    const isAdmin = (roles) => {
        if (!roles) return false;
        return roles.some(role => role === 'ROLE_ADMIN' || role === 'ADMIN');
    };

    if (loading) return <div className="p-8 text-center text-gray-500">Ładowanie użytkowników...</div>;
    if (error) return <div className="p-8 text-red-600 text-center">{error}</div>;

    return (
        <div className="max-w-7xl mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold text-gray-800 mb-6">Zarządzanie użytkownikami</h1>

            <div className="bg-white rounded-lg shadow overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Użytkownik</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Obecna rola</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Zmień rolę</th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {users.length === 0 ? (
                        <tr>
                            <td colSpan="5" className="px-6 py-4 text-center text-gray-500">Brak użytkowników</td>
                        </tr>
                    ) : (
                        users.map((user) => {
                            const userIsAdmin = isAdmin(user.roles);

                            return (
                                <tr key={user.id} className="hover:bg-gray-50">
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">#{user.id}</td>
                                    <td className="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{user.username}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{user.email}</td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                            userIsAdmin ? 'bg-purple-100 text-purple-800' :
                                                JSON.stringify(user.roles).includes('MANAGER') ? 'bg-blue-100 text-blue-800' :
                                                    'bg-gray-100 text-gray-800'
                                        }`}>
                                            {user.roles ? user.roles.map(r => r.replace('ROLE_', '')).join(', ') : 'BRAK'}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                                        {/* ✅ Якщо користувач ADMIN - показуємо текст замість селекту */}
                                        {userIsAdmin ? (
                                            <span className="text-gray-500 italic">Nie można zmienić</span>
                                        ) : (
                                            <select
                                                className="border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm p-1 border"
                                                value={getRoleValue(user.roles)}
                                                onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                            >
                                                <option value="USER">USER</option>
                                                <option value="MANAGER">MANAGER</option>
                                                {/* ✅ ADMIN опцію прибрано */}
                                            </select>
                                        )}
                                    </td>
                                </tr>
                            );
                        })
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}