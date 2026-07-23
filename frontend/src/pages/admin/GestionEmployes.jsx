import { useEffect, useState } from 'react'
import { adminApi } from '../../api/endpoints.js'
import Loader from '../../components/Loader.jsx'
import EmptyState from '../../components/EmptyState.jsx'

const dateFmt = (v) => (v ? new Date(v).toLocaleDateString('fr-FR', { dateStyle: 'medium' }) : '—')

function NouvelEmployeModal({ onClose, onCreated }) {
  const [form, setForm] = useState({ nom: '', prenom: '', email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await adminApi.creerEmploye(form)
      onCreated()
      onClose()
    } catch (err) {
      setError(err.response?.data?.message || "Impossible de créer ce compte employé.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={(e) => e.stopPropagation()}>
        <h3 style={{ marginTop: 0 }}>Créer un compte employé</h3>
        {error && <div className="form-alert error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="flex gap-12">
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="prenom">Prénom</label>
              <input id="prenom" name="prenom" className="input" value={form.prenom} onChange={handleChange} required />
            </div>
            <div className="field" style={{ flex: 1 }}>
              <label htmlFor="nom">Nom</label>
              <input id="nom" name="nom" className="input" value={form.nom} onChange={handleChange} required />
            </div>
          </div>
          <div className="field">
            <label htmlFor="email">Email professionnel</label>
            <input id="email" name="email" type="email" className="input" value={form.email} onChange={handleChange} required />
          </div>
          <div className="field">
            <label htmlFor="password">Mot de passe provisoire</label>
            <input id="password" name="password" type="password" className="input" minLength={6} value={form.password} onChange={handleChange} required />
          </div>
          <div className="flex gap-8 justify-between mt-16">
            <button type="button" className="btn btn-outline" onClick={onClose}>Annuler</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Création…' : 'Créer le compte'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function GestionEmployes() {
  const [employes, setEmployes] = useState(null)
  const [error, setError] = useState('')
  const [showModal, setShowModal] = useState(false)

  const load = async () => {
    try {
      const { data } = await adminApi.listerEmployes()
      setEmployes(data)
    } catch {
      setError('Impossible de charger la liste des employés.')
    }
  }

  useEffect(() => { load() }, [])

  const toggleActivation = async (id) => {
    try {
      await adminApi.changerActivation(id)
      load()
    } catch {
      setError("Impossible de modifier l'état de ce compte.")
    }
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <div className="eyebrow">Espace administrateur</div>
          <h1>Employés</h1>
          <p>Créez des comptes employés et gérez leur accès à la plateforme.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>+ Nouvel employé</button>
      </div>

      {error && <div className="form-alert error">{error}</div>}

      {employes === null ? (
        <Loader />
      ) : employes.length === 0 ? (
        <EmptyState title="Aucun employé" description="Créez le premier compte employé pour commencer à attribuer des dossiers." />
      ) : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Email</th>
                <th>Créé le</th>
                <th>Statut</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {employes.map((e) => (
                <tr key={e.id}>
                  <td><strong>{e.prenom} {e.nom}</strong></td>
                  <td>{e.email}</td>
                  <td>{dateFmt(e.dateCreation)}</td>
                  <td>
                    <span className={`badge ${e.actif ? 'badge-valide' : 'badge-refuse'}`}>
                      {e.actif ? 'Actif' : 'Désactivé'}
                    </span>
                  </td>
                  <td>
                    <button className="btn btn-outline btn-sm" onClick={() => toggleActivation(e.id)}>
                      {e.actif ? 'Désactiver' : 'Activer'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <NouvelEmployeModal onClose={() => setShowModal(false)} onCreated={load} />
      )}
    </div>
  )
}
