import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const HOME_BY_ROLE = { CLIENT: '/client', EMPLOYE: '/employe', ADMIN: '/admin' }

const TITLES_BY_ROLE = {
  client: { title: 'Connexion Client', subtitle: 'Accédez à vos demandes et devis.' },
  employe: { title: 'Connexion Employé', subtitle: 'Accédez à vos dossiers attribués.' },
  admin: { title: 'Connexion Admin', subtitle: 'Accédez au tableau de bord QuoteFlow.' },
}

export default function LoginPage() {
  const { login, loading } = useAuth()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const roleParam = searchParams.get('role') || 'client'
  const { title, subtitle } = TITLES_BY_ROLE[roleParam] || TITLES_BY_ROLE.client

  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const res = await login(form.email, form.password)
      navigate(HOME_BY_ROLE[res.role] || '/')
    } catch (err) {
      setError(err.response?.data?.message || 'Email ou mot de passe incorrect.')
    }
  }

  return (
    <div className="auth-shell-centered">
      <div className="auth-card">
        <Link to="/" className="auth-back-link">← Changer d'espace</Link>
        <h1>{title}</h1>
        <p className="sub">{subtitle}</p>

        {error && <div className="form-alert error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="email">Adresse email</label>
            <input
              id="email"
              name="email"
              type="email"
              className="input"
              placeholder="vous@exemple.com"
              value={form.email}
              onChange={handleChange}
              required
            />
          </div>
          <div className="field">
            <label htmlFor="password">Mot de passe</label>
            <input
              id="password"
              name="password"
              type="password"
              className="input"
              placeholder="••••••••"
              value={form.password}
              onChange={handleChange}
              required
            />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Connexion…' : 'Se connecter'}
          </button>
        </form>

        {roleParam === 'client' && (
          <div className="auth-switch">
            Pas encore de compte client ? <Link to="/register">Créer un compte</Link>
          </div>
        )}
      </div>
    </div>
  )
}