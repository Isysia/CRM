import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { customerAPI } from '../../services/api';
import { useAuth } from '../../hooks/useAuth';

export default function CustomerDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [customer, setCustomer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchCustomer();
  }, [id]);

  const fetchCustomer = async () => {
    try {
      setLoading(true);
      const response = await customerAPI.getById(id);
      console.log('Customer data:', response.data); // DEBUG
      setCustomer(response.data);
    } catch (err) {
      console.error('Error fetching customer:', err); // DEBUG
      setError('Nie udało się załadować danych klienta');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Czy na pewno chcesz usunąć tego klienta?')) {
      return;
    }

    try {
      await customerAPI.delete(id);
      navigate('/customers');
    } catch (err) {
      alert('Nie udało się usunąć klienta');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('pl-PL', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    } catch (e) {
      console.error('Date parsing error:', e);
      return '-';
    }
  };

  const canModify = user?.roles?.includes('ROLE_ADMIN') || user?.roles?.includes('ROLE_MANAGER');
  const canDelete = user?.roles?.includes('ROLE_ADMIN');

  if (loading) {
    return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <p className="mt-4 text-gray-600">Ładowanie...</p>
          </div>
        </div>
    );
  }

  if (error || !customer) {
    return (
        <div className="max-w-7xl mx-auto px-4 py-8">
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
            {error || 'Klient nie został znaleziony'}
          </div>
          <button
              onClick={() => navigate('/customers')}
              className="mt-4 text-blue-600 hover:text-blue-800"
          >
            ← Powrót do listy klientów
          </button>
        </div>
    );
  }

  return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-6">
          <button
              onClick={() => navigate('/customers')}
              className="text-blue-600 hover:text-blue-800 mb-4 flex items-center"
          >
            ← Powrót do listy
          </button>
          <div className="flex justify-between items-start">
            <div>
              <h1 className="text-3xl font-bold text-gray-800">
                {customer.firstName} {customer.lastName}
              </h1>
              <p className="text-gray-600 mt-1">Szczegóły klienta</p>
            </div>
            <span className={`px-4 py-2 rounded-full text-sm font-semibold ${
                customer.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                    customer.status === 'LEAD' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-gray-100 text-gray-800'
            }`}>
            {customer.status}
          </span>
          </div>
        </div>

        {/* Customer Info */}
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Informacje kontaktowe</h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-500">Imię</label>
              <p className="mt-1 text-lg text-gray-900">{customer.firstName}</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-500">Nazwisko</label>
              <p className="mt-1 text-lg text-gray-900">{customer.lastName}</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-500">Email</label>
              <p className="mt-1 text-lg text-gray-900">
                <a href={`mailto:${customer.email}`} className="text-blue-600 hover:text-blue-800">
                  {customer.email}
                </a>
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-500">Telefon</label>
              <p className="mt-1 text-lg text-gray-900">
                {customer.phone ? (
                    <a href={`tel:${customer.phone}`} className="text-blue-600 hover:text-blue-800">
                      {customer.phone}
                    </a>
                ) : (
                    <span className="text-gray-400">Brak</span>
                )}
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-500">Data utworzenia</label>
              <p className="mt-1 text-lg text-gray-900">
                {formatDate(customer.createdAt)}
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-500">Ostatnia aktualizacja</label>
              <p className="mt-1 text-lg text-gray-900">
                {formatDate(customer.updatedAt)}
              </p>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-4">
          {canModify && (
              <button
                  onClick={() => navigate(`/customers/${id}/edit`)}
                  className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors"
              >
                Edytuj klienta
              </button>
          )}
          {canDelete && (
              <button
                  onClick={handleDelete}
                  className="bg-red-600 text-white py-2 px-6 rounded-lg hover:bg-red-700 transition-colors"
              >
                Usuń
              </button>
          )}
        </div>
      </div>
  );
}
