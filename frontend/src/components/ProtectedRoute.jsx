import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const HOME_BY_ROLE = { CLIENT: '/client', EMPLOYE: '/employe', ADMIN: '/admin' }

export default function ProtectedRoute({ children, roles }) {
  const { user } = useAuth()

  if (!user) return <Navigate to="/login" replace />

  if (roles && !roles.includes(user.role)) {
    return <Navigate to={HOME_BY_ROLE[user.role] || '/login'} replace />
  }

  return children
}
