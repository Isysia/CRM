import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { offerAPI, customerAPI } from '../../services/api';
import { useRole } from '../../hooks/useRole';

export default function OfferDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isManager, isAdmin } = useRole();

  const [offer, setOffer] = useState(null);
  const [customer, setCustomer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOffer();
  }, [id]);

  const fetchOffer = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await offerAPI.getById(id);
      setOffer(response.data);

      // Fetch customer details
      if (response.data.customerId) {
        const customerRes = await customerAPI.getById(response.data.customerId);
        setCustomer(customerRes.data);
      }
    } catch (err) {
      setError('Nie udało się załadować oferty');
      console.error('Error fetching offer:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Czy na pewno chcesz usunąć tę ofertę?')) {
      return;
    }

    try {
      await offerAPI.delete(id);
      navigate('/offers');
    } catch (err) {
      alert('Nie udało się usunąć oferty');
      console.error('Error deleting offer:', err);
    }
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(value);
  };

  const getStatusBadge = (status) => {
    const styles = {
      DRAFT: { bg: 'bg-gray-100', text: 'text-gray-800', label: 'Szkic' },
      SENT: { bg: 'bg-blue-100', text: 'text-blue-800', label: 'Wysłana' },
      ACCEPTED: { bg: 'bg-green-100', text: 'text-green-800', label: 'Zaakceptowana' },
      REJECTED: { bg: 'bg-red-100', text: 'text-red-800', label: 'Odrzucona' }
    };
    const style = styles[status] || styles.DRAFT;
    return (
        <span className={`px-3 py-1 inline-flex text-sm font-semibold rounded-full ${style.bg} ${style.text}`}>
        {style.label}
      </span>
    );
  };

  const canModify = isManager();
  const canDelete = isAdmin();

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

  if (error || !offer) {
    return (
        <div className="max-w-4xl mx-auto px-4 py-8">
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
            {error || 'Nie znaleziono oferty'}
          </div>
          <button
              onClick={() => navigate('/offers')}
              className="mt-4 text-blue-600 hover:text-blue-800"
          >
            ← Powrót do listy ofert
          </button>
        </div>
    );
  }

  return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-6">
          <button
              onClick={() => navigate('/offers')}
              className="text-blue-600 hover:text-blue-800 mb-4 inline-flex items-center"
          >
            ← Powrót do listy
          </button>
          <div className="flex justify-between items-start">
            <div>
              <h1 className="text-3xl font-bold text-gray-800">{offer.title}</h1>
              <p className="text-gray-600 mt-1">Szczegóły oferty #{offer.id}</p>
            </div>
            <div className="flex space-x-2">
              {canModify && (
                  <button
                      onClick={() => navigate(`/offers/${id}/edit`)}
                      className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                  >
                    Edytuj
                  </button>
              )}
              {canDelete && (
                  <button
                      onClick={handleDelete}
                      className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                  >
                    Usuń
                  </button>
              )}
            </div>
          </div>
        </div>

        {/* Main Info Card */}
        <div className="bg-white rounded-lg shadow overflow-hidden mb-6">
          <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-800">Informacje podstawowe</h2>
          </div>
          <div className="px-6 py-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="text-sm font-medium text-gray-500">Wartość</label>
                <p className="mt-1 text-2xl font-bold text-gray-900">
                  {formatCurrency(offer.price)}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">Status</label>
                <p className="mt-1">{getStatusBadge(offer.status)}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Customer Info Card */}
        {customer && (
            <div className="bg-white rounded-lg shadow overflow-hidden mb-6">
              <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-800">Klient</h2>
              </div>
              <div className="px-6 py-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="text-sm font-medium text-gray-500">Imię i nazwisko</label>
                    <p className="mt-1 text-lg text-gray-900">
                      {customer.firstName} {customer.lastName}
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Email</label>
                    <p className="mt-1 text-lg text-gray-900">{customer.email}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Telefon</label>
                    <p className="mt-1 text-lg text-gray-900">{customer.phone || '-'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Status klienta</label>
                    <p className="mt-1">
                  <span className={`px-3 py-1 inline-flex text-xs font-semibold rounded-full ${
                      customer.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                          customer.status === 'LEAD' ? 'bg-yellow-100 text-yellow-800' :
                              'bg-gray-100 text-gray-800'
                  }`}>
                    {customer.status}
                  </span>
                    </p>
                  </div>
                </div>
                <div className="mt-4">
                  <button
                      onClick={() => navigate(`/customers/${customer.id}`)}
                      className="text-blue-600 hover:text-blue-800"
                  >
                    Zobacz profil klienta →
                  </button>
                </div>
              </div>
            </div>
        )}

        {/* Description Card */}
        {offer.description && (
            <div className="bg-white rounded-lg shadow overflow-hidden">
              <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-800">Opis</h2>
              </div>
              <div className="px-6 py-4">
                <p className="text-gray-700 whitespace-pre-wrap">{offer.description}</p>
              </div>
            </div>
        )}

        {/* Timestamps */}
        <div className="mt-6 text-sm text-gray-500">
          {offer.createdAt && (
              <p>Utworzono: {new Date(offer.createdAt).toLocaleString('pl-PL')}</p>
          )}
          {offer.updatedAt && (
              <p>Ostatnia aktualizacja: {new Date(offer.updatedAt).toLocaleString('pl-PL')}</p>
          )}
        </div>
      </div>
  );
}
