import { useAuth } from '../../hooks/useAuth';

export default function Dashboard() {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-800 mb-2">
            Witaj, {user?.username}! 👋
          </h1>
          <p className="text-gray-600">Panel zarządzania CRM</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-700">Klienci</h3>
              <span className="text-3xl">👥</span>
            </div>
            <p className="text-3xl font-bold text-blue-600">-</p>
            <p className="text-sm text-gray-500 mt-2">Wszystkich klientów</p>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-700">Oferty</h3>
              <span className="text-3xl">💼</span>
            </div>
            <p className="text-3xl font-bold text-green-600">-</p>
            <p className="text-sm text-gray-500 mt-2">Aktywnych ofert</p>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-700">Zadania</h3>
              <span className="text-3xl">✅</span>
            </div>
            <p className="text-3xl font-bold text-orange-600">-</p>
            <p className="text-sm text-gray-500 mt-2">Do zrobienia</p>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">
            System gotowy do pracy! 🚀
          </h2>
          <p className="text-gray-600 mb-4">
            Wybierz sekcję z menu powyżej, aby rozpocząć pracę z systemem CRM.
          </p>
          <div className="flex gap-4">
            <a 
              href="/customers" 
              className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Zarządzaj klientami
            </a>
            <a 
              href="/offers" 
              className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 transition-colors"
            >
              Zobacz oferty
            </a>
            <a 
              href="/tasks" 
              className="bg-orange-600 text-white px-6 py-2 rounded-lg hover:bg-orange-700 transition-colors"
            >
              Zadania do wykonania
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
