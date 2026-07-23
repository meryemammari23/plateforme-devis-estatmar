import { createContext, useContext, useState, useCallback } from 'react'
import { authApi } from '../api/endpoints.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('quoteflow_user')
    return raw ? JSON.parse(raw) : null
  })
  const [loading, setLoading] = useState(false)

  const persistSession = (authResponse) => {
    const { token, ...userInfo } = authResponse
    localStorage.setItem('quoteflow_token', token)
    localStorage.setItem('quoteflow_user', JSON.stringify(userInfo))
    setUser(userInfo)
  }

  const login = useCallback(async (email, password) => {
    setLoading(true)
    try {
      const { data } = await authApi.login({ email, password })
      persistSession(data)
      return data
    } finally {
      setLoading(false)
    }
  }, [])

  const register = useCallback(async (payload) => {
    setLoading(true)
    try {
      const { data } = await authApi.register(payload)
      persistSession(data)
      return data
    } finally {
      setLoading(false)
    }
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('quoteflow_token')
    localStorage.removeItem('quoteflow_user')
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth doit être utilisé dans un <AuthProvider>')
  return ctx
}
