import { useState, useEffect, createContext, useContext } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem('auth');
    const username = localStorage.getItem('username');

    if (token && username) {
      setUser({ username });
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    try {
      const { token, user } = await authAPI.login(username, password);
      setUser(user);
      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: error.message || 'Nieprawidłowa nazwa użytkownika lub hasło'
      };
    }
  };

  const logout = () => {
    authAPI.logout();
    setUser(null);
  };

  return (
      <AuthContext.Provider value={{ user, login, logout, loading }}>
        {children}
      </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};