import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { customerAPI } from '../../services/api';

export default function CustomerForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditMode = !!id;

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    status: 'LEAD'
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(isEditMode);

  // Fetch customer data if editing
  useEffect(() => {
    if (isEditMode) {
      fetchCustomer();
    }
  }, [id]);

  const fetchCustomer = async () => {
    try {
      const response = await customerAPI.getById(id);
      setFormData(response.data);
    } catch (err) {
      alert('Nie udało się załadować danych klienta');
      navigate('/customers');
    } finally {
      setFetchLoading(false);
    }
  };

  const formatPhoneNumber = (value) => {
    const digits = value.replace(/\D/g, '');

    let number = digits;
    if (digits.startsWith('48')) {
      number = digits.substring(2);
    }

    const part1 = number.substring(0, 3);
    const part2 = number.substring(3, 6);
    const part3 = number.substring(6, 9);

    if (number.length > 6) {
      return `+48 ${part1} ${part2} ${part3}`.trim();
    } else if (number.length > 3) {
      return `+48 ${part1} ${part2}`.trim();
    } else if (number.length > 0) {
      return `+48 ${part1}`.trim();
    }

    return value.startsWith('+') ? '+' : ''; // Дозволяємо почати з +
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    let finalValue = value;

    if (name === 'phone') {
      if (value.length < formData.phone.length) {
        finalValue = value;
      } else {
        finalValue = formatPhoneNumber(value);
      }
    }

    setFormData(prev => ({ ...prev, [name]: finalValue }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.firstName.trim()) {
      newErrors.firstName = 'Imię jest wymagane';
    }

    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Nazwisko jest wymagane';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Email jest wymagany';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email jest nieprawidłowy';
    }

    if (formData.phone) {
      const digitsOnly = formData.phone.replace(/\D/g, '');
      if (digitsOnly.length !== 11 || !formData.phone.startsWith('+48')) {
        newErrors.phone = 'Numer telefonu musi mieć format +48 123 456 789';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      if (isEditMode) {
        await customerAPI.update(id, formData);
      } else {
        await customerAPI.create(formData);
      }
      navigate('/customers');
    } catch (err) {
      const errorMessage = err.response?.data?.message ||
          `Nie udało się ${isEditMode ? 'zaktualizować' : 'dodać'} klienta`;
      alert(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  if (fetchLoading) {
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
            {isEditMode ? 'Edytuj klienta' : 'Dodaj nowego klienta'}
          </h1>
          <p className="text-gray-600 mt-1">
            {isEditMode ? 'Zaktualizuj dane klienta' : 'Wypełnij formularz, aby dodać klienta'}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-6">
          {/* First Name */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Imię <span className="text-red-500">*</span>
            </label>
            <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.firstName ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Jan"
            />
            {errors.firstName && (
                <p className="mt-1 text-sm text-red-500">{errors.firstName}</p>
            )}
          </div>

          {/* Last Name */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Nazwisko <span className="text-red-500">*</span>
            </label>
            <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.lastName ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Kowalski"
            />
            {errors.lastName && (
                <p className="mt-1 text-sm text-red-500">{errors.lastName}</p>
            )}
          </div>

          {/* Email */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Email <span className="text-red-500">*</span>
            </label>
            <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.email ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="jan.kowalski@example.com"
            />
            {errors.email && (
                <p className="mt-1 text-sm text-red-500">{errors.email}</p>
            )}
          </div>

          {/* Phone */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Telefon
            </label>
            <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.phone ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="+48 123 456 789"
            />
            {errors.phone && (
                <p className="mt-1 text-sm text-red-500">{errors.phone}</p>
            )}
          </div>

          {/* Status */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Status <span className="text-red-500">*</span>
            </label>
            <select
                name="status"
                value={formData.status}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="LEAD">Lead</option>
              <option value="ACTIVE">Aktywny</option>
              <option value="INACTIVE">Nieaktywny</option>
            </select>
          </div>

          {/* Buttons */}
          <div className="flex gap-4">
            <button
                type="submit"
                disabled={loading}
                className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? 'Zapisywanie...' : isEditMode ? 'Zaktualizuj' : 'Dodaj klienta'}
            </button>
            <button
                type="button"
                onClick={() => navigate('/customers')}
                className="flex-1 bg-gray-200 text-gray-700 py-2 px-4 rounded-lg hover:bg-gray-300 transition-colors"
            >
              Anuluj
            </button>
          </div>
        </form>
      </div>
  );
}
