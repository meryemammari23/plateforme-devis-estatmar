import { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(null);

  // Restaure la session depuis le stockage navigateur au chargement.
  useEffect(() => {
    const saved = localStorage.getItem('quoteflow_auth');
    if (saved) setAuth(JSON.parse(saved));
  }, []);

  const login = (data) => {
    setAuth(data);
    localStorage.setItem('quoteflow_auth', JSON.stringify(data));
  };

  const logout = () => {
    setAuth(null);
    localStorage.removeItem('quoteflow_auth');
  };

  return (
    <AuthContext.Provider value={{ auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
