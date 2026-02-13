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

  const { isManager, isAdmin, isUser } = useRole();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [tasksRes, customersRes, offersRes] = await Promise.all([
        taskAPI.getAll(),
        customerAPI.getAll(),
        offerAPI.getAll()
      ]);
      setTasks(tasksRes.data || []);
      setCustomers(customersRes.data || []);
      setOffers(offersRes.data || []);
    } catch (err) {
      setError('Nie udało się załadować danych.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (task) => {
    try {
      const newStatus = task.status === 'DONE' ? 'TODO' : 'DONE';
      await taskAPI.updateStatus(task.id, newStatus);

      setTasks(tasks.map(t => t.id === task.id ? { ...t, status: newStatus } : t));
    } catch (err) {
      alert('Błąd: ' + (err.response?.status === 403 ? 'Brak uprawnień' : err.message));
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Czy na pewno chcesz usunąć to zadanie?')) return;
    try {
      await taskAPI.delete(id);
      setTasks(tasks.filter(t => t.id !== id));
    } catch (err) {
      alert('Nie udało się usunąć zadania.');
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
      year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
    });
  };

  const getPriorityBadge = (priority) => {
    const styles = { LOW: 'bg-blue-100 text-blue-800', MEDIUM: 'bg-yellow-100 text-yellow-800', HIGH: 'bg-red-100 text-red-800' };
    const labels = { LOW: 'Niski', MEDIUM: 'Średni', HIGH: 'Wysoki' };
    return { style: styles[priority] || styles.MEDIUM, label: labels[priority] || priority };
  };

  const getStatusBadge = (status) => {
    const styles = { TODO: 'bg-gray-100 text-gray-800', IN_PROGRESS: 'bg-blue-100 text-blue-800', DONE: 'bg-green-100 text-green-800' };
    const labels = { TODO: 'Do zrobienia', IN_PROGRESS: 'W trakcie', DONE: 'Zrobione' };
    return { style: styles[status] || styles.TODO, label: labels[status] || status };
  };

  const filteredTasks = tasks.filter(task => {
    const matchesSearch = task.title.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === 'ALL' || task.status === filterStatus;
    const matchesPriority = filterPriority === 'ALL' || task.priority === filterPriority;
    const matchesCustomer = filterCustomer === 'ALL' || task.customerId === parseInt(filterCustomer);
    return matchesSearch && matchesStatus && matchesPriority && matchesCustomer;
  });

  const canEdit = isManager(); // Для редагування/видалення
  const canDelete = isAdmin();

  const canComplete = isUser() || isManager() || isAdmin();

  if (loading) return <div className="p-8 text-center">Ładowanie...</div>;

  return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold text-gray-800">Zadania</h1>
          {canEdit && (
              <button onClick={() => window.location.href = '/tasks/new'} className="bg-blue-600 text-white px-4 py-2 rounded">
                + Dodaj zadanie
              </button>
          )}
        </div>

        <div className="bg-white rounded-lg shadow p-4 mb-6 grid grid-cols-4 gap-4">
          <input placeholder="Szukaj..." className="border p-2 rounded" value={searchTerm} onChange={e => setSearchTerm(e.target.value)} />
          <select className="border p-2 rounded" value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
            <option value="ALL">Wszystkie statusy</option>
            <option value="TODO">Do zrobienia</option>
            <option value="DONE">Zrobione</option>
          </select>
        </div>

        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">✓</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tytuł</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Klient</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Termin</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Priorytet</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Akcje</th>
            </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
            {filteredTasks.map((task) => (
                <tr key={task.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    {canComplete && (
                        <input
                            type="checkbox"
                            checked={task.status === 'DONE'}
                            onChange={() => handleToggleStatus(task)}
                            className="h-4 w-4 text-blue-600 cursor-pointer"
                        />
                    )}
                  </td>
                  <td className="px-6 py-4 font-medium">{task.title}</td>
                  <td className="px-6 py-4">{task.customerName || '-'}</td>
                  <td className="px-6 py-4">{formatDate(task.dueDate)}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded-full text-xs ${getPriorityBadge(task.priority).style}`}>
                        {getPriorityBadge(task.priority).label}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded-full text-xs ${getStatusBadge(task.status).style}`}>
                        {getStatusBadge(task.status).label}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <button onClick={() => window.location.href=`/tasks/${task.id}`} className="text-blue-600 mr-2">Szczegóły</button>
                    {canEdit && <button onClick={() => window.location.href=`/tasks/${task.id}/edit`} className="text-indigo-600 mr-2">Edytuj</button>}
                    {canDelete && <button onClick={() => handleDelete(task.id)} className="text-red-600">Usuń</button>}
                  </td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>
      </div>
  );
}