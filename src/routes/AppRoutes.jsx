import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import { useAuth } from '../context/AuthContext';

import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
import NouvelleDemande from '../pages/client/NouvelleDemande';
import MesDemandes from '../pages/client/MesDemandes';
import DossiersAttribues from '../pages/employe/DossiersAttribues';
import RedactionDevis from '../pages/employe/RedactionDevis';
import GestionEmployes from '../pages/admin/GestionEmployes';
import Attribution from '../pages/admin/Attribution';
import Dashboard from '../pages/admin/Dashboard';

/** Redirige l'utilisateur connecte vers l'accueil de son role. */
function Home() {
  const { auth } = useAuth();
  if (!auth) return <Navigate to="/login" replace />;
  if (auth.role === 'CLIENT') return <Navigate to="/client/mes-demandes" replace />;
  if (auth.role === 'EMPLOYE') return <Navigate to="/employe/dossiers" replace />;
  if (auth.role === 'ADMIN') return <Navigate to="/admin/dashboard" replace />;
  return <Navigate to="/login" replace />;
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/" element={<Home />} />

      {/* CLIENT */}
      <Route path="/client/nouvelle-demande" element={
        <ProtectedRoute roles={['CLIENT']}><NouvelleDemande /></ProtectedRoute>} />
      <Route path="/client/mes-demandes" element={
        <ProtectedRoute roles={['CLIENT']}><MesDemandes /></ProtectedRoute>} />

      {/* EMPLOYE */}
      <Route path="/employe/dossiers" element={
        <ProtectedRoute roles={['EMPLOYE']}><DossiersAttribues /></ProtectedRoute>} />
      <Route path="/employe/devis/:demandeId" element={
        <ProtectedRoute roles={['EMPLOYE']}><RedactionDevis /></ProtectedRoute>} />

      {/* ADMIN */}
      <Route path="/admin/dashboard" element={
        <ProtectedRoute roles={['ADMIN']}><Dashboard /></ProtectedRoute>} />
      <Route path="/admin/employes" element={
        <ProtectedRoute roles={['ADMIN']}><GestionEmployes /></ProtectedRoute>} />
      <Route path="/admin/demandes" element={
        <ProtectedRoute roles={['ADMIN']}><Attribution /></ProtectedRoute>} />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
