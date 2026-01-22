import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './hooks/useAuth';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LoginPage from './components/auth/LoginPage';
import RegisterPage from './components/auth/RegisterPage'; // Новий імпорт
import Navbar from './components/layout/Navbar';
import Dashboard from './components/layout/Dashboard';
import CustomerList from './components/customers/CustomerList';
import CustomerForm from './components/customers/CustomerForm';
import CustomerDetails from './components/customers/CustomerDetails';
import OfferList from './components/offers/OfferList';
import OfferForm from './components/offers/OfferForm';
import OfferDetails from './components/offers/OfferDetails';
import TaskList from './components/tasks/TaskList';
import TaskForm from './components/tasks/TaskForm';
import TaskDetails from './components/tasks/TaskDetails';
import UsersList from './components/admin/UsersList'; // Новий імпорт

function App() {
  return (
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            {/* Публічні роути */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Захищені роути */}
            <Route
                path="/*"
                element={
                  <ProtectedRoute>
                    <div className="min-h-screen bg-gray-50">
                      <Navbar />
                      <Routes>
                        <Route path="/" element={<Dashboard />} />

                        {/* Customers */}
                        <Route path="/customers" element={<CustomerList />} />
                        <Route path="/customers/new" element={<CustomerForm />} />
                        <Route path="/customers/:id" element={<CustomerDetails />} />
                        <Route path="/customers/:id/edit" element={<CustomerForm />} />

                        {/* Offers */}
                        <Route path="/offers" element={<OfferList />} />
                        <Route path="/offers/new" element={<OfferForm />} />
                        <Route path="/offers/:id" element={<OfferDetails />} />
                        <Route path="/offers/:id/edit" element={<OfferForm />} />

                        {/* Tasks */}
                        <Route path="/tasks" element={<TaskList />} />
                        <Route path="/tasks/new" element={<TaskForm />} />
                        <Route path="/tasks/:id" element={<TaskDetails />} />
                        <Route path="/tasks/:id/edit" element={<TaskForm />} />

                        {/* Admin only */}
                        <Route path="/users" element={<UsersList />} />
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

export default App;