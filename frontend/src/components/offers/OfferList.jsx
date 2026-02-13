import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { offerAPI, customerAPI } from '../../services/api';
import { useRole } from '../../hooks/useRole';

export default function OfferList() {
  const [offers, setOffers] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [filterCustomer, setFilterCustomer] = useState('ALL');

  const { isManager, isAdmin } = useRole();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [offersRes, customersRes] = await Promise.all([
        offerAPI.getAll(),
        customerAPI.getAll()
      ]);
      setOffers(offersRes.data || []);
      setCustomers(customersRes.data || []);
    } catch (err) {
      console.error('Błąd pobierania danych:', err);
      setError('Nie udało się załadować danych.');
    } finally {
      setLoading(false);
    }
  };

  // NOWA FUNKCJA: Zmiany statusu
  const handleStatusChange = async (offerId, newStatus) => {
    try {
      await offerAPI.updateStatus(offerId, newStatus);

      // Aktualizujemy lokalnie bez przeładowania
      setOffers(prevOffers =>
          prevOffers.map(offer =>
              offer.id === offerId
                  ? { ...offer, status: newStatus }
                  : offer
          )
      );
    } catch (err) {
      console.error('Status change error:', err);

      if (err.response?.status === 403) {
        alert('Nie masz uprawnień do zmiany statusu ofert');
      } else {
        alert('Nie udało się zmienić statusu: ' + (err.response?.data?.message || err.message || 'Błąd serwera'));
      }
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Czy na pewno chcesz usunąć tę ofertę?')) return;

    try {
      await offerAPI.delete(id);
      setOffers(prev => prev.filter(o => o.id !== id));
    } catch (err) {
      alert('Nie udało się usunąć oferty');
    }
  };

  const getCustomerName = (customerId) => {
    const customer = customers.find(c => c.id === customerId);
    return customer ? `${customer.firstName || ''} ${customer.lastName || ''}` : 'Nieznany klient';
  };

  const formatCurrency = (price) => {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN'
    }).format(price);
  };

  const getStatusBadge = (status) => {
    const styles = {
      DRAFT: 'bg-gray-100 text-gray-800',
      SENT: 'bg-blue-100 text-blue-800',
      ACCEPTED: 'bg-green-100 text-green-800',
      REJECTED: 'bg-red-100 text-red-800',
      CANCELLED: 'bg-orange-100 text-orange-800',
    };
    return styles[status] || 'bg-gray-100 text-gray-800';
  };

  // Helper: Tłumaczenie statusów
  const getStatusLabel = (status) => {
    const labels = {
      DRAFT: 'Szkic',
      SENT: 'Wysłana',
      ACCEPTED: 'Zaakceptowana',
      REJECTED: 'Odrzucona',
      CANCELLED: 'Anulowana'
    };
    return labels[status] || status;
  };

  const filteredOffers = offers.filter(offer => {
    const customerName = getCustomerName(offer.customerId).toLowerCase();
    const searchLower = searchTerm.toLowerCase();

    const matchesSearch =
        (offer.title && offer.title.toLowerCase().includes(searchLower)) ||
        customerName.includes(searchLower);

    const matchesStatus = filterStatus === 'ALL' || offer.status === filterStatus;
    const matchesCustomer = filterCustomer === 'ALL' || offer.customerId === Number(filterCustomer);

    return matchesSearch && matchesStatus && matchesCustomer;
  });

  const canModify = isManager();
  const canDelete = isAdmin();

  if (loading) {
    return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <p className="mt-4 text-gray-600">Ładowanie ofert...</p>
          </div>
        </div>
    );
  }

  if (error) return <div className="p-8 text-red-600 text-center">{error}</div>;

  return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Oferty</h1>
            <p className="text-gray-600 mt-1">Zarządzaj ofertami sprzedaży</p>
          </div>
          {canModify && (
              <Link
                  to="/offers/new"
                  className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors inline-block"
              >
                + Dodaj ofertę
              </Link>
          )}
        </div>

        {/* Filters Panel */}
        <div className="bg-white rounded-lg shadow p-4 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Szukaj</label>
              <input
                  type="text"
                  placeholder="Tytuł lub klient..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Status</label>
              <select
                  value={filterStatus}
                  onChange={(e) => setFilterStatus(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none"
              >
                <option value="ALL">Wszystkie</option>
                <option value="DRAFT">Szkic</option>
                <option value="SENT">Wysłana</option>
                <option value="ACCEPTED">Zaakceptowana</option>
                <option value="REJECTED">Odrzucona</option>
                <option value="CANCELLED">Anulowana</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Klient</label>
              <select
                  value={filterCustomer}
                  onChange={(e) => setFilterCustomer(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none"
              >
                <option value="ALL">Wszyscy klienci</option>
                {customers.map(customer => (
                    <option key={customer.id} value={customer.id}>
                      {customer.firstName} {customer.lastName} ({customer.email})
                    </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Table */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tytuł</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Klient</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Wartość</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Akcje</th>
            </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
            {filteredOffers.length === 0 ? (
                <tr>
                  <td colSpan="5" className="px-6 py-12 text-center text-gray-500">
                    Brak ofert spełniających kryteria.
                  </td>
                </tr>
            ) : (
                filteredOffers.map((offer) => (
                    <tr key={offer.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900">{offer.title}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-600">{getCustomerName(offer.customerId)}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-semibold text-gray-900">{formatCurrency(offer.value || offer.price)}</div>
                      </td>
                      {/* ✅ ЗМІНЕНО: Status тепер dropdown */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        {canModify ? (
                            <select
                                value={offer.status}
                                onChange={(e) => handleStatusChange(offer.id, e.target.value)}
                                className={`px-3 py-1 text-xs font-semibold rounded-full border-0 cursor-pointer focus:ring-2 focus:ring-blue-500 ${getStatusBadge(offer.status)}`}
                            >
                              <option value="DRAFT">📝 Szkic</option>
                              <option value="SENT">📤 Wysłana</option>
                              <option value="ACCEPTED">✅ Zaakceptowana</option>
                              <option value="REJECTED">❌ Odrzucona</option>
                              <option value="CANCELLED">🚫 Anulowana</option>
                            </select>
                        ) : (
                            <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusBadge(offer.status)}`}>
                            {getStatusLabel(offer.status)}
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <Link to={`/offers/${offer.id}`} className="text-blue-600 hover:text-blue-900 mr-4">
                          Szczegóły
                        </Link>
                        {canModify && (
                            <Link to={`/offers/${offer.id}/edit`} className="text-indigo-600 hover:text-indigo-900 mr-4">
                              Edytuj
                            </Link>
                        )}
                        {canDelete && (
                            <button onClick={() => handleDelete(offer.id)} className="text-red-600 hover:text-red-900">
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

        <div className="mt-4 text-xs text-gray-500 text-center">
          Wyświetlono {filteredOffers.length} z {offers.length} ofert
        </div>
      </div>
  );
}