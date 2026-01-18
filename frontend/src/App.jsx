import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './hooks/useAuth';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LoginPage from './components/auth/LoginPage';
import Navbar from './components/layout/Navbar';
import Dashboard from './components/layout/Dashboard';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          
          <Route
            path="/*"
            element={
              <ProtectedRoute>
                <div className="min-h-screen bg-gray-50">
                  <Navbar />
                  <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/customers" element={<PlaceholderPage title="Klienci" />} />
                    <Route path="/offers" element={<PlaceholderPage title="Oferty" />} />
                    <Route path="/tasks" element={<PlaceholderPage title="Zadania" />} />
                  </Routes>
                </div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

// Temporary placeholder component
function PlaceholderPage({ title }) {
  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-4">{title}</h1>
      <div className="bg-white rounded-lg shadow p-8 text-center">
        <p className="text-gray-600 text-lg">🚧 Strona w budowie</p>
        <p className="text-gray-500 mt-2">Ta sekcja zostanie wkrótce dodana</p>
      </div>
    </div>
  );
}

export default App;
