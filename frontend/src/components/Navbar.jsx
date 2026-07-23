import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const LINKS_BY_ROLE = {
  CLIENT: [{ to: '/client', label: 'Mes demandes' }],
  EMPLOYE: [{ to: '/employe', label: 'Mes dossiers' }],
  ADMIN: [
    { to: '/admin', label: 'Tableau de bord' },
    { to: '/admin/employes', label: 'Employés' },
    { to: '/admin/demandes', label: 'Demandes' },
  ],
}

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  if (!user) return null
  const links = LINKS_BY_ROLE[user.role] || []

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <NavLink to={links[0]?.to || '/'} className="brand">
          <span>QuoteFlow</span>
        </NavLink>

        <nav className="nav-links">
          {links.map((l) => (
            <NavLink
              key={l.to}
              to={l.to}
              end={l.to === '/admin'}
              className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
            >
              {l.label}
            </NavLink>
          ))}
        </nav>

        <div className="nav-user">
          <div style={{ textAlign: 'right' }}>
            <div className="nav-user-name">{user.prenom} {user.nom}</div>
            <div className="nav-user-role">{user.role}</div>
          </div>
          <button className="btn btn-ghost btn-sm" onClick={handleLogout}>Déconnexion</button>
        </div>
      </div>
    </header>
  )
}