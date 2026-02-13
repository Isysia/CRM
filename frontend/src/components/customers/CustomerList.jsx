import { useState, useEffect } from 'react';
import { customerAPI } from '../../services/api';
import { useRole } from '../../hooks/useRole';
import { useApi } from '../../hooks/useApi'; // Додаємо імпорт
import { useNavigate } from 'react-router-dom';

export default function CustomerList() {
  const [customers, setCustomers] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');

  const { isManager, isAdmin } = useRole();
  const navigate = useNavigate();

  const { loading, error, execute: fetchCustomersExec } = useApi(customerAPI.getAll);

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    const data = await fetchCustomersExec();
    if (data) setCustomers(data);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Czy na pewno chcesz usunąć tego klienta?')) return;
    try {
      await customerAPI.delete(id);
      setCustomers(customers.filter(c => c.id !== id));
    } catch (err) {
      alert('Nie udało się usunąć klienta');
    }
  };

  // Filter customers
  const filteredCustomers = customers.filter(customer => {
    const matchesSearch =
        customer.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        customer.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        customer.email.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus = filterStatus === 'ALL' || customer.status === filterStatus;

    return matchesSearch && matchesStatus;
  });

  // Check if user can modify
  const canModify = isManager();
  const canDelete = isAdmin();

  if (loading) {
    return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <p className="mt-4 text-gray-600">Ładowanie klientów...</p>
          </div>
        </div>
    );
  }

  return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Klienci</h1>
            <p className="text-gray-600 mt-1">Zarządzaj bazą klientów</p>
          </div>
          {canModify && (
              <button
                  onClick={() => window.location.href = '/customers/new'}
                  className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                + Dodaj klienta
              </button>
          )}
        </div>

        {/* Search and Filter */}
        <div className="bg-white rounded-lg shadow p-4 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Szukaj
              </label>
              <input
                  type="text"
                  placeholder="Szukaj po imieniu, nazwisku lub email..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Status
              </label>
              <select
                  value={filterStatus}
                  onChange={(e) => setFilterStatus(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="ALL">Wszystkie</option>
                <option value="LEAD">Lead</option>
                <option value="ACTIVE">Aktywny</option>
                <option value="INACTIVE">Nieaktywny</option>
              </select>
            </div>
          </div>
        </div>

        {/* Error Message */}
        {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6">
              {error}
            </div>
        )}

        {/* Customers Table */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Imię i Nazwisko
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Email
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Telefon
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Akcje
              </th>
            </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
            {filteredCustomers.length === 0 ? (
                <tr>
                  <td colSpan="5" className="px-6 py-12 text-center text-gray-500">
                    Brak klientów do wyświetlenia
                  </td>
                </tr>
            ) : (
                filteredCustomers.map((customer) => (
                    <tr key={customer.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900">
                          {customer.firstName} {customer.lastName}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-600">{customer.email}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-600">{customer.phone || '-'}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        customer.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                            customer.status === 'LEAD' ? 'bg-yellow-100 text-yellow-800' :
                                'bg-gray-100 text-gray-800'
                    }`}>
                      {customer.status}
                    </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <button
                            onClick={() => navigate(`/customers/${customer.id}`)}
                            className="text-blue-600 hover:text-blue-900 mr-4"
                        >
                          Szczegóły
                        </button>
                        {canModify && (
                            <button
                                onClick={() => navigate(`/customers/${customer.id}/edit`)}
                                className="text-indigo-600 hover:text-indigo-900 mr-4"
                            >
                              Edytuj
                            </button>
                        )}
                        {canDelete && (
                            <button
                                onClick={() => handleDelete(customer.id)}
                                className="text-red-600 hover:text-red-900"
                            >
                              Usuń
                            </button>
                        )}
                      </td>
                    </tr>
                ))
            )}
            </tbody>
          </table>
        </div>

        {/* Stats */}
        <div className="mt-6 text-sm text-gray-600">
          Wyświetlanie {filteredCustomers.length} z {customers.length} klientów
        </div>
      </div>
  );
}
