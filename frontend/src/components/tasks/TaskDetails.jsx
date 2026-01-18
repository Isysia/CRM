import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { taskAPI, customerAPI, offerAPI } from '../../services/api';
import { useRole } from '../../hooks/useRole';

export default function TaskDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isManager, isAdmin } = useRole();

  const [task, setTask] = useState(null);
  const [customer, setCustomer] = useState(null);
  const [offer, setOffer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchTask();
  }, [id]);

  const fetchTask = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await taskAPI.getById(id);
      setTask(response.data);

      // Fetch customer details
      if (response.data.customerId) {
        const customerRes = await customerAPI.getById(response.data.customerId);
        setCustomer(customerRes.data);
      }

      // Fetch offer details if exists
      if (response.data.offerId) {
        const offerRes = await offerAPI.getById(response.data.offerId);
        setOffer(offerRes.data);
      }
    } catch (err) {
      setError('Nie udało się załadować zadania');
      console.error('Error fetching task:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Czy na pewno chcesz usunąć to zadanie?')) {
      return;
    }

    try {
      await taskAPI.delete(id);
      navigate('/tasks');
    } catch (err) {
      alert('Nie udało się usunąć zadania');
      console.error('Error deleting task:', err);
    }
  };

  const handleToggleStatus = async () => {
    try {
      const newStatus = task.status === 'DONE' ? 'TODO' : 'DONE';
      const updatedTask = { ...task, status: newStatus };
      await taskAPI.update(id, updatedTask);
      setTask({ ...task, status: newStatus });
    } catch (err) {
      alert('Nie udało się zaktualizować statusu');
      console.error('Error updating status:', err);
    }
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

  const formatDate = (dateValue) => {
    if (!dateValue) return '-';
    const date = parseDate(dateValue);
    return date.toLocaleString('pl-PL', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const isOverdue = () => {
    if (!task || task.status === 'DONE') return false;
    const dueDate = parseDate(task.dueDate);
    return dueDate < new Date();
  };

  const isDueToday = () => {
    if (!task || task.status === 'DONE') return false;
    const dueDate = parseDate(task.dueDate);
    const today = new Date();
    return dueDate.toDateString() === today.toDateString();
  };

  const getPriorityBadge = (priority) => {
    const styles = {
      LOW: { bg: 'bg-blue-100', text: 'text-blue-800', label: 'Niski' },
      MEDIUM: { bg: 'bg-yellow-100', text: 'text-yellow-800', label: 'Średni' },
      HIGH: { bg: 'bg-red-100', text: 'text-red-800', label: 'Wysoki' }
    };
    const style = styles[priority] || styles.MEDIUM;
    return (
      <span className={`px-3 py-1 inline-flex text-sm font-semibold rounded-full ${style.bg} ${style.text}`}>
        {style.label}
      </span>
    );
  };

  const getStatusBadge = (status) => {
    const styles = {
      TODO: { bg: 'bg-gray-100', text: 'text-gray-800', label: 'Do zrobienia' },
      IN_PROGRESS: { bg: 'bg-blue-100', text: 'text-blue-800', label: 'W trakcie' },
      DONE: { bg: 'bg-green-100', text: 'text-green-800', label: 'Zrobione' }
    };
    const style = styles[status] || styles.TODO;
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

  if (error || !task) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error || 'Nie znaleziono zadania'}
        </div>
        <button
          onClick={() => navigate('/tasks')}
          className="mt-4 text-blue-600 hover:text-blue-800"
        >
          ← Powrót do listy zadań
        </button>
      </div>
    );
  }

  const overdue = isOverdue();
  const dueToday = isDueToday();

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6">
        <button
          onClick={() => navigate('/tasks')}
          className="text-blue-600 hover:text-blue-800 mb-4 inline-flex items-center"
        >
          ← Powrót do listy
        </button>
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">{task.title}</h1>
            <p className="text-gray-600 mt-1">Szczegóły zadania #{task.id}</p>
          </div>
          <div className="flex space-x-2">
            {canModify && task.status !== 'DONE' && (
              <button
                onClick={handleToggleStatus}
                className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
              >
                ✓ Oznacz jako zrobione
              </button>
            )}
            {canModify && task.status === 'DONE' && (
              <button
                onClick={handleToggleStatus}
                className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
              >
                ↺ Przywróć
              </button>
            )}
            {canModify && (
              <button
                onClick={() => navigate(`/tasks/${id}/edit`)}
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
              <label className="text-sm font-medium text-gray-500">Termin wykonania</label>
              <p className={`mt-1 text-lg font-semibold ${
                overdue ? 'text-red-700' :
                dueToday ? 'text-yellow-700' :
                'text-gray-900'
              }`}>
                {formatDate(task.dueDate)}
                {overdue && <span className="ml-2">⚠️ Przeterminowane</span>}
                {dueToday && <span className="ml-2">⏰ Dziś</span>}
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Status</label>
              <p className="mt-1">{getStatusBadge(task.status)}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Priorytet</label>
              <p className="mt-1">{getPriorityBadge(task.priority)}</p>
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

      {/* Offer Info Card */}
      {offer && (
        <div className="bg-white rounded-lg shadow overflow-hidden mb-6">
          <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-800">Powiązana oferta</h2>
          </div>
          <div className="px-6 py-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="text-sm font-medium text-gray-500">Tytuł oferty</label>
                <p className="mt-1 text-lg text-gray-900">{offer.title}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">Status oferty</label>
                <p className="mt-1">
                  <span className={`px-3 py-1 inline-flex text-xs font-semibold rounded-full ${
                    offer.status === 'ACCEPTED' ? 'bg-green-100 text-green-800' :
                    offer.status === 'SENT' ? 'bg-blue-100 text-blue-800' :
                    offer.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
                    'bg-gray-100 text-gray-800'
                  }`}>
                    {offer.status}
                  </span>
                </p>
              </div>
            </div>
            <div className="mt-4">
              <button
                onClick={() => navigate(`/offers/${offer.id}`)}
                className="text-blue-600 hover:text-blue-800"
              >
                Zobacz szczegóły oferty →
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Description Card */}
      {task.description && (
        <div className="bg-white rounded-lg shadow overflow-hidden mb-6">
          <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-800">Opis</h2>
          </div>
          <div className="px-6 py-4">
            <p className="text-gray-700 whitespace-pre-wrap">{task.description}</p>
          </div>
        </div>
      )}

      {/* Timestamps */}
      <div className="mt-6 text-sm text-gray-500">
        {task.createdAt && (
          <p>Utworzono: {formatDate(task.createdAt)}</p>
        )}
        {task.updatedAt && (
          <p>Ostatnia aktualizacja: {formatDate(task.updatedAt)}</p>
        )}
      </div>
    </div>
  );
}
