import { useState, useEffect } from 'react';
import { taskAPI, customerAPI, offerAPI } from '../../services/api';
import { useRole } from '../../hooks/useRole';

export default function TaskList() {
  const [tasks, setTasks] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [filterPriority, setFilterPriority] = useState('ALL');
  const [filterCustomer, setFilterCustomer] = useState('ALL');
  const { isManager, isAdmin } = useRole();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [tasksRes, customersRes, offersRes] = await Promise.all([
        taskAPI.getAll(),
        customerAPI.getAll(),
        offerAPI.getAll()
      ]);
      setTasks(tasksRes.data);
      setCustomers(customersRes.data);
      setOffers(offersRes.data);
    } catch (err) {
      setError('Nie udało się załadować zadań');
      console.error('Error fetching tasks:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Czy na pewno chcesz usunąć to zadanie?')) {
      return;
    }

    try {
      await taskAPI.delete(id);
      setTasks(tasks.filter(t => t.id !== id));
    } catch (err) {
      alert('Nie udało się usunąć zadania');
      console.error('Error deleting task:', err);
    }
  };

  const handleToggleStatus = async (task) => {
    try {
      const newStatus = task.status === 'DONE' ? 'TODO' : 'DONE';

      // ✅ Конвертуй дату в ISO string
      let dueDateISO;
      if (typeof task.dueDate === 'string') {
        dueDateISO = task.dueDate;
      } else if (Array.isArray(task.dueDate)) {
        const [year, month, day, hour = 0, minute = 0, second = 0] = task.dueDate;
        const date = new Date(year, month - 1, day, hour, minute, second);
        dueDateISO = date.toISOString();
      } else {
        dueDateISO = new Date().toISOString();
      }

      const taskRequest = {
        title: task.title,
        description: task.description || '',
        dueDate: dueDateISO,  // ← Тепер ISO string
        status: newStatus,
        priority: task.priority,
        customerId: task.customerId,
        offerId: task.offerId || null
      };

      console.log('Sending taskRequest:', taskRequest);

      await taskAPI.update(task.id, taskRequest);

      // Update local state
      setTasks(tasks.map(t => t.id === task.id ? { ...t, status: newStatus } : t));
    } catch (err) {
      alert('Nie udało się zaktualizować statusu');
      console.error('Error updating status:', err);
    }
  };

  const isOverdue = (task) => {
    if (task.status === 'DONE') return false;
    const dueDate = parseDate(task.dueDate);
    return dueDate < new Date();
  };

  const isDueToday = (task) => {
    if (task.status === 'DONE') return false;
    const dueDate = parseDate(task.dueDate);
    const today = new Date();
    return dueDate.toDateString() === today.toDateString();
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

  const getCustomerName = (customerId) => {
    const customer = customers.find(c => c.id === customerId);
    return customer ? `${customer.firstName} ${customer.lastName}` : '-';
  };

  const getOfferTitle = (offerId) => {
    if (!offerId) return '-';
    const offer = offers.find(o => o.id === offerId);
    return offer ? offer.title : '-';
  };

  const filteredTasks = tasks.filter(task => {
    const matchesSearch =
        task.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (task.customerName || getCustomerName(task.customerId)).toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus = filterStatus === 'ALL' || task.status === filterStatus;
    const matchesPriority = filterPriority === 'ALL' || task.priority === filterPriority;
    const matchesCustomer = filterCustomer === 'ALL' || task.customerId === parseInt(filterCustomer);

    return matchesSearch && matchesStatus && matchesPriority && matchesCustomer;
  });

  const canModify = isManager();
  const canDelete = isAdmin();

  const getPriorityBadge = (priority) => {
    const styles = {
      LOW: 'bg-blue-100 text-blue-800',
      MEDIUM: 'bg-yellow-100 text-yellow-800',
      HIGH: 'bg-red-100 text-red-800'
    };
    const labels = {
      LOW: 'Niski',
      MEDIUM: 'Średni',
      HIGH: 'Wysoki'
    };
    return { style: styles[priority] || styles.MEDIUM, label: labels[priority] || priority };
  };

  const getStatusBadge = (status) => {
    const styles = {
      TODO: 'bg-gray-100 text-gray-800',
      IN_PROGRESS: 'bg-blue-100 text-blue-800',
      DONE: 'bg-green-100 text-green-800'
    };
    const labels = {
      TODO: 'Do zrobienia',
      IN_PROGRESS: 'W trakcie',
      DONE: 'Zrobione'
    };
    return { style: styles[status] || styles.TODO, label: labels[status] || status };
  };

  if (loading) {
    return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <p className="mt-4 text-gray-600">Ładowanie zadań...</p>
          </div>
        </div>
    );
  }

  return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Zadania</h1>
            <p className="text-gray-600 mt-1">Zarządzaj zadaniami i terminami</p>
          </div>
          {canModify && (
              <button
                  onClick={() => window.location.href = '/tasks/new'}
                  className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                + Dodaj zadanie
              </button>
          )}
        </div>

        {/* Filters */}
        <div className="bg-white rounded-lg shadow p-4 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Szukaj
              </label>
              <input
                  type="text"
                  placeholder="Szukaj po tytule lub kliencie..."
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
                <option value="TODO">Do zrobienia</option>
                <option value="IN_PROGRESS">W trakcie</option>
                <option value="DONE">Zrobione</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Priorytet
              </label>
              <select
                  value={filterPriority}
                  onChange={(e) => setFilterPriority(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="ALL">Wszystkie</option>
                <option value="HIGH">Wysoki</option>
                <option value="MEDIUM">Średni</option>
                <option value="LOW">Niski</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Klient
              </label>
              <select
                  value={filterCustomer}
                  onChange={(e) => setFilterCustomer(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="ALL">Wszyscy klienci</option>
                {customers.map(customer => (
                    <option key={customer.id} value={customer.id}>
                      {customer.firstName} {customer.lastName}
                    </option>
                ))}
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

        {/* Tasks Table */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-12">
                ✓
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tytuł
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Klient
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Termin
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Priorytet
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
            {filteredTasks.length === 0 ? (
                <tr>
                  <td colSpan="7" className="px-6 py-12 text-center text-gray-500">
                    Brak zadań do wyświetlenia
                  </td>
                </tr>
            ) : (
                filteredTasks.map((task) => {
                  const overdue = isOverdue(task);
                  const dueToday = isDueToday(task);
                  const priorityBadge = getPriorityBadge(task.priority);
                  const statusBadge = getStatusBadge(task.status);

                  return (
                      <tr
                          key={task.id}
                          className={`hover:bg-gray-50 ${
                              overdue ? 'bg-red-50' :
                                  dueToday ? 'bg-yellow-50' : ''
                          }`}
                      >
                        <td className="px-6 py-4 whitespace-nowrap">
                          {canModify && (
                              <input
                                  type="checkbox"
                                  checked={task.status === 'DONE'}
                                  onChange={() => handleToggleStatus(task)}
                                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded cursor-pointer"
                              />
                          )}
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm font-medium text-gray-900">
                            {task.title}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-600">
                            {task.customerName || getCustomerName(task.customerId)}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className={`text-sm ${
                              overdue ? 'text-red-700 font-semibold' :
                                  dueToday ? 'text-yellow-700 font-semibold' :
                                      'text-gray-600'
                          }`}>
                            {formatDate(task.dueDate)}
                            {overdue && <span className="ml-2">⚠️</span>}
                            {dueToday && <span className="ml-2">⏰</span>}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${priorityBadge.style}`}>
                        {priorityBadge.label}
                      </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${statusBadge.style}`}>
                        {statusBadge.label}
                      </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <button
                              onClick={() => window.location.href = `/tasks/${task.id}`}
                              className="text-blue-600 hover:text-blue-900 mr-4"
                          >
                            Szczegóły
                          </button>
                          {canModify && (
                              <button
                                  onClick={() => window.location.href = `/tasks/${task.id}/edit`}
                                  className="text-indigo-600 hover:text-indigo-900 mr-4"
                              >
                                Edytuj
                              </button>
                          )}
                          {canDelete && (
                              <button
                                  onClick={() => handleDelete(task.id)}
                                  className="text-red-600 hover:text-red-900"
                              >
                                Usuń
                              </button>
                          )}
                        </td>
                      </tr>
                  );
                })
            )}
            </tbody>
          </table>
        </div>

        {/* Stats */}
        <div className="mt-6 text-sm text-gray-600">
          Wyświetlanie {filteredTasks.length} z {tasks.length} zadań
        </div>
      </div>
  );
}
