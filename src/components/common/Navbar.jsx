import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Navbar() {
  const { auth, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <div className="navbar">
      <div>
        <Link to="/" style={{ fontWeight: 700, color: '#fff' }}>QuoteFlow</Link>
        {auth?.role === 'CLIENT' && (
          <>
            <Link to="/client/nouvelle-demande">Nouvelle demande</Link>
            <Link to="/client/mes-demandes">Mes demandes</Link>
          </>
        )}
        {auth?.role === 'EMPLOYE' && <Link to="/employe/dossiers">Mes dossiers</Link>}
        {auth?.role === 'ADMIN' && (
          <>
            <Link to="/admin/dashboard">Dashboard</Link>
            <Link to="/admin/employes">Employés</Link>
            <Link to="/admin/demandes">Demandes</Link>
          </>
        )}
      </div>
      <div>
        {auth ? (
          <>
            <span style={{ marginRight: 16 }}>{auth.prenom} {auth.nom} ({auth.role})</span>
            <button className="btn secondary" onClick={handleLogout}>Déconnexion</button>
          </>
        ) : <Link to="/login">Connexion</Link>}
      </div>
    </div>
  );
}
