import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext.jsx'
import Navbar from './components/Navbar.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'

import LoginPage from './pages/LoginPage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'
import SpaceSelectPage from './pages/SpaceSelectPage.jsx'
import ClientDashboard from './pages/client/ClientDashboard.jsx'
import EmployeDashboard from './pages/employe/EmployeDashboard.jsx'
import AdminOverview from './pages/admin/AdminOverview.jsx'
import GestionEmployes from './pages/admin/GestionEmployes.jsx'
import GestionDemandes from './pages/admin/GestionDemandes.jsx'

const HOME_BY_ROLE = { CLIENT: '/client', EMPLOYE: '/employe', ADMIN: '/admin' }

export default function App() {
  const { user } = useAuth()

  return (
    <div className="app-shell">
      <Navbar />
      <Routes>
        <Route path="/login" element={user ? <Navigate to={HOME_BY_ROLE[user.role]} replace /> : <LoginPage />} />
        <Route path="/register" element={user ? <Navigate to={HOME_BY_ROLE[user.role]} replace /> : <RegisterPage />} />

        <Route
          path="/client"
          element={
            <ProtectedRoute roles={['CLIENT']}>
              <ClientDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/employe"
          element={
            <ProtectedRoute roles={['EMPLOYE']}>
              <EmployeDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <AdminOverview />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/employes"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <GestionEmployes />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/demandes"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <GestionDemandes />
            </ProtectedRoute>
          }
        />

        <Route
          path="/"
          element={user ? <Navigate to={HOME_BY_ROLE[user.role]} replace /> : <SpaceSelectPage />}
        />
        <Route path="*" element={<Navigate to={user ? HOME_BY_ROLE[user.role] : '/'} replace />} />
      </Routes>
    </div>
  )
}