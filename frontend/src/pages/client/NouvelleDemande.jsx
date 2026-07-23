import { useState } from 'react'
import { demandeApi } from '../../api/endpoints.js'

export default function NouvelleDemande({ onCreated }) {
  const [form, setForm] = useState({ description: '', budget: '' })
  const [fichiers, setFichiers] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)
    try {
      const { data: demande } = await demandeApi.soumettre({
        description: form.description,
        budget: Number(form.budget),
      })

      if (fichiers.length > 0) {
        await demandeApi.ajouterPiecesJointes(demande.id, fichiers)
      }

      setSuccess(`Demande ${demande.reference} envoyée avec succès. Vous recevrez un email de confirmation.`)
      setForm({ description: '', budget: '' })
      setFichiers([])
      onCreated?.()
    } catch (err) {
      setError(err.response?.data?.message || "Impossible d'envoyer la demande. Réessayez.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card" style={{ maxWidth: 620 }}>
      <h3 style={{ marginTop: 0 }}>Décrivez votre projet</h3>
      <p className="text-muted text-sm" style={{ marginTop: -8, marginBottom: 20 }}>
        Un employé étudiera votre dossier et vous enverra un devis détaillé par email.
      </p>

      {error && <div className="form-alert error">{error}</div>}
      {success && <div className="form-alert success">{success}</div>}

      <form onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="description">Description du projet</label>
          <textarea
            id="description"
            name="description"
            className="textarea"
            placeholder="Ex : Développement d'un site vitrine avec formulaire de contact et back-office..."
            value={form.description}
            onChange={handleChange}
            required
          />
        </div>

        <div className="field">
          <label htmlFor="budget">Budget proposé (MAD)</label>
          <input
            id="budget"
            name="budget"
            type="number"
            min="1"
            step="0.01"
            className="input"
            placeholder="15000"
            value={form.budget}
            onChange={handleChange}
            required
          />
        </div>

        <div className="field">
          <label htmlFor="fichiers">Pièces jointes (optionnel)</label>
          <input
            id="fichiers"
            type="file"
            multiple
            className="input"
            onChange={(e) => setFichiers(Array.from(e.target.files))}
          />
          {fichiers.length > 0 && (
            <div className="hint">{fichiers.length} fichier(s) sélectionné(s)</div>
          )}
        </div>

        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Envoi en cours…' : 'Envoyer ma demande'}
        </button>
      </form>
    </div>
  )
}
