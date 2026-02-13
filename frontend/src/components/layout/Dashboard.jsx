import { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { customerAPI, offerAPI, taskAPI } from '../../services/api';

export default function Dashboard() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [stats, setStats] = useState({
    totalCustomers: 0,
    activeCustomers: 0,
    totalOffers: 0,
    activeOffers: 0,
    totalTasks: 0,
    todoTasks: 0,
    overdueTasks: 0
  });

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      setLoading(true);
      setError(null);

      const [customersRes, offersRes, tasksRes] = await Promise.all([
        customerAPI.getAll(),
        offerAPI.getAll(),
        taskAPI.getAll()
      ]);

      processStats(
          customersRes.data || [],
          offersRes.data || [],
          tasksRes.data || []
      );
    } catch (err) {
      console.error('Failed to load dashboard stats:', err);
      setError('Nie udało się załadować statystyk');
    } finally {
      setLoading(false);
    }
  };

  const processStats = (customers, offers, tasks) => {
    const now = new Date();

    setStats({
      totalCustomers: customers.length,
      activeCustomers: customers.filter(c => c.status === 'ACTIVE').length,
      totalOffers: offers.length,
      activeOffers: offers.filter(o => ['SENT', 'DRAFT'].includes(o.status)).length,
      totalTasks: tasks.length,
      todoTasks: tasks.filter(t => ['TODO', 'IN_PROGRESS'].includes(t.status)).length,
      overdueTasks: tasks.filter(t => {
        if (t.status === 'DONE') return false;
        const dueDate = parseDate(t.dueDate);
        return dueDate < now;
      }).length
    });
  };

  const parseDate = (dateValue) => {
    if (!dateValue) return new Date();
    if (typeof dateValue === 'string') return new Date(dateValue);
    if (Array.isArray(dateValue)) {
      const [year, month, day, hour = 0, minute = 0] = dateValue;
      return new Date(year, month - 1, day, hour, minute);
    }
    return new Date();
  };

  if (loading) {
    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-50">
          <div className="text-center">
            <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-xl font-medium text-gray-700">Ładowanie statystyk...</p>
          </div>
        </div>
    );
  }

  if (error) {
    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-50">
          <div className="text-center">
            <div className="text-red-500 text-6xl mb-4">⚠️</div>
            <p className="text-xl font-medium text-gray-700 mb-2">{error}</p>
            <button
                onClick={loadStats}
                className="mt-4 bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
            >
              Spróbuj ponownie
            </button>
          </div>
        </div>
    );
  }

  return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 py-8">
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-800 mb-2">
              Witaj, {user?.username}! 👋
            </h1>
            <p className="text-gray-600">Panel zarządzania CRM</p>
          </div>

          {/* Sekcja Statystyk */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-700">Klienci</h3>
                <span className="text-3xl">👥</span>
              </div>
              <p className="text-3xl font-bold text-blue-600">{stats.totalCustomers}</p>
              <p className="text-sm text-gray-500 mt-2">{stats.activeCustomers} aktywnych</p>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-700">Oferty</h3>
                <span className="text-3xl">💼</span>
              </div>
              <p className="text-3xl font-bold text-green-600">{stats.totalOffers}</p>
              <p className="text-sm text-gray-500 mt-2">{stats.activeOffers} aktywnych</p>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-700">Zadania</h3>
                <span className="text-3xl">✅</span>
              </div>
              <p className="text-3xl font-bold text-orange-600">{stats.todoTasks}</p>
              <p className="text-sm text-gray-500 mt-2">Do zrobienia</p>
            </div>
          </div>

          {/* Alert o zaległych zadaniach */}
          {stats.overdueTasks > 0 && (
              <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-8">
                <div className="flex items-center">
                  <span className="text-2xl mr-3">⚠️</span>
                  <div>
                    <p className="text-red-800 font-semibold">
                      Masz {stats.overdueTasks} przeterminowanych zadań!
                    </p>
                    <p className="text-red-600 text-sm">
                      Sprawdź sekcję zadań, aby zaktualizować ich status.
                    </p>
                  </div>
                </div>
              </div>
          )}

          {/* Szybkie akcje */}
          <div className="bg-white rounded-lg shadow p-6 mb-8">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">
              Szybkie akcje 🚀
            </h2>
            <p className="text-gray-600 mb-4">
              Wybierz sekcję z menu powyżej lub użyj szybkich akcji poniżej.
            </p>
            <div className="flex flex-wrap gap-4">
              <a
                  href="/customers"
                  className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                👥 Zarządzaj klientami
              </a>

              <a
                  href="/offers"
                  className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 transition-colors"
              >
                💼 Zobacz oferty
              </a>

              <a
                  href="/tasks"
                  className="bg-orange-600 text-white px-6 py-2 rounded-lg hover:bg-orange-700 transition-colors"
              >
                ✅ Zadania do wykonania
              </a>
            </div>
          </div>

          {/* Podsumowanie szczegółowe */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-800 mb-4">
                Podsumowanie klientów
              </h3>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-gray-600">Wszystkich:</span>
                  <span className="font-semibold">{stats.totalCustomers}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Aktywnych:</span>
                  <span className="font-semibold text-green-600">{stats.activeCustomers}</span>
                </div>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-800 mb-4">
                Podsumowanie zadań
              </h3>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-gray-600">Wszystkich:</span>
                  <span className="font-semibold">{stats.totalTasks}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Do zrobienia:</span>
                  <span className="font-semibold text-orange-600">{stats.todoTasks}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Przeterminowanych:</span>
                  <span className="font-semibold text-red-600">{stats.overdueTasks}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  );
}