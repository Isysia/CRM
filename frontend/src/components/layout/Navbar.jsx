import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useRole } from '../../hooks/useRole';

export default function Navbar() {
  const { user, logout } = useAuth();
  const { isAdmin } = useRole();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
      <nav className="bg-white shadow-md">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-8">
              <Link to="/" className="text-xl font-bold text-blue-600">
                🏢 CRM System
              </Link>
              <div className="hidden md:flex space-x-4">
                <Link to="/customers" className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md hover:bg-blue-50 transition-colors">
                  Klienci
                </Link>
                <Link to="/offers" className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md hover:bg-blue-50 transition-colors">
                  Oferty
                </Link>
                <Link to="/tasks" className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md hover:bg-blue-50 transition-colors">
                  Zadania
                </Link>
                {isAdmin() && (
                    <Link to="/users" className="text-purple-700 hover:text-purple-600 px-3 py-2 rounded-md hover:bg-purple-50 transition-colors font-medium">
                      👥 Użytkownicy
                    </Link>
                )}
              </div>
            </div>

            <div className="flex items-center space-x-4">
            <span className="text-sm text-gray-600">
              {user?.role} 👤 {user?.username}
            </span>
              <button
                  onClick={handleLogout}
                  className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors text-sm"
              >
                Wyloguj się
              </button>
            </div>
          </div>
        </div>
      </nav>
  );
}