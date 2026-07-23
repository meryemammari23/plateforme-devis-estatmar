import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function RegisterPage() {
  const { register, loading } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ nom: '', prenom: '', email: '', password: '' })
  const [error, setError] = useState('')

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await register(form)
      navigate('/client')
    } catch (err) {
      setError(err.response?.data?.message || "L'inscription a échoué. Vérifiez vos informations.")
    }
  }

  return (
    <div className="auth-shell-centered">
      <div className="auth-card">
        <Link to="/" className="auth-back-link">← Changer d'espace</Link>
        <h1>Créer un compte client</h1>
        <p className="sub">Quelques informations pour commencer.</p>

        {error && <div className="form-alert error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="flex gap-12">
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="prenom">Prénom</label>
              <input
                id="prenom" name="prenom" className="input" placeholder="Prénom"
                value={form.prenom} onChange={handleChange} required
              />
            </div>
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="nom">Nom</label>
              <input
                id="nom" name="nom" className="input" placeholder="Nom"
                value={form.nom} onChange={handleChange} required
              />
            </div>
          </div>
          <div className="field">
            <label htmlFor="email">Adresse email</label>
            <input
              id="email" name="email" type="email" className="input"
              placeholder="vous@exemple.com" value={form.email} onChange={handleChange} required
            />
          </div>
          <div className="field">
            <label htmlFor="password">Mot de passe</label>
            <input
              id="password" name="password" type="password" className="input"
              placeholder="6 caractères minimum" value={form.password} onChange={handleChange}
              minLength={6} required
            />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Création…' : 'Créer mon compte'}
          </button>
        </form>

        <div className="auth-switch">
          Déjà un compte ? <Link to="/login">Se connecter</Link>
        </div>
      </div>
    </div>
  )
}