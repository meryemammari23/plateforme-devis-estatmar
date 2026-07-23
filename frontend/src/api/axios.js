import axios from 'axios'

// Base URL de l'API Spring Boot (voir .env -> VITE_API_URL)
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
})

// Attache automatiquement le token JWT (stocké après login/register) à chaque requête
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('quoteflow_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Si le token est invalide/expiré, on déconnecte proprement l'utilisateur
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('quoteflow_token')
      localStorage.removeItem('quoteflow_user')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api
