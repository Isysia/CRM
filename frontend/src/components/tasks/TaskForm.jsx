import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { taskAPI, customerAPI, offerAPI } from '../../services/api';

export default function TaskForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditMode = Boolean(id);

  const [customers, setCustomers] = useState([]);
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(isEditMode);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    dueDate: '',
    status: 'TODO',
    priority: 'MEDIUM',
    customerId: '',
    offerId: ''
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    fetchCustomersAndOffers();
    if (isEditMode) {
      fetchTask();
    }
  }, [id]);

  const fetchCustomersAndOffers = async () => {
    try {
      const [customersRes, offersRes] = await Promise.all([
        customerAPI.getAll(),
        offerAPI.getAll()
      ]);
      setCustomers(customersRes.data);
      setOffers(offersRes.data);
    } catch (err) {
      setError('Nie udało się załadować danych');
      console.error('Error fetching data:', err);
    }
  };

  const fetchTask = async () => {
    try {
      setLoading(true);
      const response = await taskAPI.getById(id);
      const task = response.data;
      
      // Convert dueDate to input format
      const dueDateFormatted = formatDateForInput(task.dueDate);
      
      setFormData({
        title: task.title,
        description: task.description || '',
        dueDate: dueDateFormatted,
        status: task.status,
        priority: task.priority,
        customerId: task.customerId,
        offerId: task.offerId || ''
      });
    } catch (err) {
      setError('Nie udało się załadować zadania');
      console.error('Error fetching task:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDateForInput = (dateValue) => {
    if (!dateValue) return '';
    
    let date;
    if (typeof dateValue === 'string') {
      date = new Date(dateValue);
    } else if (Array.isArray(dateValue)) {
      const [year, month, day, hour = 0, minute = 0] = dateValue;
      date = new Date(year, month - 1, day, hour, minute);
    } else {
      return '';
    }
    
    // Format: YYYY-MM-DDTHH:MM
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: null }));
    }
  };

  const validate = () => {
    const newErrors = {};

    if (!formData.title.trim()) {
      newErrors.title = 'Tytuł jest wymagany';
    }

    if (!formData.customerId) {
      newErrors.customerId = 'Wybierz klienta';
    }

    if (!formData.dueDate) {
      newErrors.dueDate = 'Termin jest wymagany';
    } else {
      const dueDate = new Date(formData.dueDate);
      const now = new Date();
      if (!isEditMode && dueDate < now) {
        newErrors.dueDate = 'Termin musi być w przyszłości';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    try {
      setSaving(true);
      setError(null);

      const dataToSend = {
        ...formData,
        customerId: parseInt(formData.customerId),
        offerId: formData.offerId ? parseInt(formData.offerId) : null,
        dueDate: new Date(formData.dueDate).toISOString()
      };

      if (isEditMode) {
        await taskAPI.update(id, dataToSend);
      } else {
        await taskAPI.create(dataToSend);
      }

      navigate('/tasks');
    } catch (err) {
      setError(isEditMode ? 'Nie udało się zaktualizować zadania' : 'Nie udało się utworzyć zadania');
      console.error('Error saving task:', err);
    } finally {
      setSaving(false);
    }
  };

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

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-800">
          {isEditMode ? 'Edytuj zadanie' : 'Nowe zadanie'}
        </h1>
        <p className="text-gray-600 mt-1">
          {isEditMode ? 'Zaktualizuj dane zadania' : 'Wypełnij poniższe pola aby utworzyć zadanie'}
        </p>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6">
          {error}
        </div>
      )}

      <div className="bg-white rounded-lg shadow p-6">
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Title */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Tytuł zadania *
            </label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.title ? 'border-red-500' : 'border-gray-300'
              }`}
              placeholder="np. Przygotować ofertę dla klienta"
            />
            {errors.title && (
              <p className="mt-1 text-sm text-red-600">{errors.title}</p>
            )}
          </div>

          {/* Customer */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Klient *
            </label>
            <select
              name="customerId"
              value={formData.customerId}
              onChange={handleChange}
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.customerId ? 'border-red-500' : 'border-gray-300'
              }`}
            >
              <option value="">Wybierz klienta</option>
              {customers.map(customer => (
                <option key={customer.id} value={customer.id}>
                  {customer.firstName} {customer.lastName} - {customer.email}
                </option>
              ))}
            </select>
            {errors.customerId && (
              <p className="mt-1 text-sm text-red-600">{errors.customerId}</p>
            )}
          </div>

          {/* Offer (Optional) */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Oferta (opcjonalnie)
            </label>
            <select
              name="offerId"
              value={formData.offerId}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">Brak powiązanej oferty</option>
              {offers.map(offer => (
                <option key={offer.id} value={offer.id}>
                  {offer.title} - {offer.customerName || customers.find(c => c.id === offer.customerId)?.firstName}
                </option>
              ))}
            </select>
          </div>

          {/* Due Date */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Termin wykonania *
            </label>
            <input
              type="datetime-local"
              name="dueDate"
              value={formData.dueDate}
              onChange={handleChange}
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.dueDate ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.dueDate && (
              <p className="mt-1 text-sm text-red-600">{errors.dueDate}</p>
            )}
          </div>

          {/* Priority */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Priorytet
            </label>
            <select
              name="priority"
              value={formData.priority}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="LOW">Niski</option>
              <option value="MEDIUM">Średni</option>
              <option value="HIGH">Wysoki</option>
            </select>
          </div>

          {/* Status */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Status
            </label>
            <select
              name="status"
              value={formData.status}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="TODO">Do zrobienia</option>
              <option value="IN_PROGRESS">W trakcie</option>
              <option value="DONE">Zrobione</option>
            </select>
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Opis
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows="4"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Dodatkowe informacje o zadaniu..."
            />
          </div>

          {/* Buttons */}
          <div className="flex justify-end space-x-4 pt-4">
            <button
              type="button"
              onClick={() => navigate('/tasks')}
              className="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
            >
              Anuluj
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {saving ? 'Zapisywanie...' : (isEditMode ? 'Zaktualizuj' : 'Utwórz')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
