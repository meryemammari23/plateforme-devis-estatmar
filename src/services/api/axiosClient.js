import axios from 'axios';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// Intercepteur : injecte le token JWT dans chaque requete.
axiosClient.interceptors.request.use((config) => {
  const saved = localStorage.getItem('quoteflow_auth');
  if (saved) {
    const { token } = JSON.parse(saved);
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Deconnexion automatique si le token est rejete.
axiosClient.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('quoteflow_auth');
    }
    return Promise.reject(err);
  }
);

export default axiosClient;
