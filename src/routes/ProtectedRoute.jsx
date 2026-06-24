import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/** Protege une route : redirige si non connecte ou role non autorise. */
export default function ProtectedRoute({ children, roles }) {
  const { auth } = useAuth();
  if (!auth) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(auth.role)) return <Navigate to="/" replace />;
  return children;
}
