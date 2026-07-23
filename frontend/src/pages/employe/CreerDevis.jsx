import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext.jsx'
import { demandeApi, devisApi } from '../../api/endpoints.js'

const money = (v) =>
  new Intl.NumberFormat('fr-MA', { style: 'currency', currency: 'MAD' }).format(v || 0)

let uid = 0
const newLigne = () => ({ key: uid++, designation: '', quantite: 1, prixUnitaire: '' })

export default function CreerDevis({ preselected, onCreated }) {
  const { user } = useAuth()
  const [eligibles, setEligibles] = useState(null)
  const [demandeId, setDemandeId] = useState(preselected?.id || '')
  const [commentaire, setCommentaire] = useState('')
  const [lignes, setLignes] = useState([newLigne()])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (preselected) return
    demandeApi.toutes().then(({ data }) => {
      setEligibles(data.filter((d) => d.employeId === user.userId && d.statut === 'EN_COURS' && !d.devisId))
    })
  }, [preselected])

  useEffect(() => {
    if (preselected) setDemandeId(preselected.id)
  }, [preselected])

  const updateLigne = (key, field, value) => {
    setLignes((ls) => ls.map((l) => (l.key === key ? { ...l, [field]: value } : l)))
  }
  const addLigne = () => setLignes((ls) => [...ls, newLigne()])
  const removeLigne = (key) => setLignes((ls) => (ls.length > 1 ? ls.filter((l) => l.key !== key) : ls))

  const total = lignes.reduce((sum, l) => sum + (Number(l.quantite) || 0) * (Number(l.prixUnitaire) || 0), 0)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    if (!demandeId) {
      setError('Sélectionnez un dossier avant de continuer.')
      return
    }

    setLoading(true)
    try {
      const payload = {
        demandeId: Number(demandeId),
        commentaire,
        lignes: lignes.map((l) => ({
          designation: l.designation,
          quantite: Number(l.quantite),
          prixUnitaire: Number(l.prixUnitaire),
        })),
      }
      const { data: devis } = await devisApi.creer(payload)
      setSuccess(`Devis n°${devis.id} créé et envoyé au client (PDF généré automatiquement).`)
      setLignes([newLigne()])
      setCommentaire('')
      if (!preselected) setDemandeId('')
      onCreated?.()
    } catch (err) {
      setError(err.response?.data?.message || 'Impossible de créer le devis.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card" style={{ maxWidth: 720 }}>
      <h3 style={{ marginTop: 0 }}>Rédiger un devis</h3>
      <p className="text-muted text-sm" style={{ marginTop: -8, marginBottom: 20 }}>
        Le PDF est généré automatiquement et envoyé au client dès la soumission.
      </p>

      {error && <div className="form-alert error">{error}</div>}
      {success && <div className="form-alert success">{success}</div>}

      <form onSubmit={handleSubmit}>
        {!preselected && (
          <div className="field">
            <label htmlFor="demande">Dossier concerné</label>
            <select
              id="demande"
              className="select"
              value={demandeId}
              onChange={(e) => setDemandeId(e.target.value)}
              required
            >
              <option value="">— Sélectionner un dossier en cours —</option>
              {eligibles?.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.reference} — {d.clientNomComplet} ({money(d.budget)})
                </option>
              ))}
            </select>
            {eligibles?.length === 0 && (
              <div className="hint">Aucun dossier « en cours » sans devis ne vous est attribué actuellement.</div>
            )}
          </div>
        )}

        {preselected && (
          <div className="field">
            <label>Dossier concerné</label>
            <p style={{ margin: 0 }}><strong>{preselected.reference}</strong> — {preselected.clientNomComplet}</p>
          </div>
        )}

        <div className="field">
          <label>Lignes du devis</label>
          {lignes.map((l) => (
            <div className="ligne-row" key={l.key}>
              <input
                className="input" placeholder="Désignation (ex : Développement backend)"
                value={l.designation} onChange={(e) => updateLigne(l.key, 'designation', e.target.value)} required
              />
              <input
                className="input" type="number" min="1" placeholder="Qté"
                value={l.quantite} onChange={(e) => updateLigne(l.key, 'quantite', e.target.value)} required
              />
              <input
                className="input" type="number" min="0.01" step="0.01" placeholder="Prix unitaire"
                value={l.prixUnitaire} onChange={(e) => updateLigne(l.key, 'prixUnitaire', e.target.value)} required
              />
              <div className="text-sm" style={{ textAlign: 'right', fontWeight: 600 }}>
                {money((Number(l.quantite) || 0) * (Number(l.prixUnitaire) || 0))}
              </div>
              <button type="button" className="btn btn-ghost btn-sm" onClick={() => removeLigne(l.key)} aria-label="Supprimer la ligne">✕</button>
            </div>
          ))}
          <button type="button" className="btn btn-outline btn-sm mt-8" onClick={addLigne}>+ Ajouter une ligne</button>
        </div>

        <div className="field">
          <label htmlFor="commentaire">Commentaire (optionnel)</label>
          <textarea
            id="commentaire" className="textarea" placeholder="Précisions, conditions, délais…"
            value={commentaire} onChange={(e) => setCommentaire(e.target.value)}
          />
        </div>

        <div className="ligne-total-row">
          <span className="text-muted">Montant total</span>
          <strong>{money(total)}</strong>
        </div>

        <button type="submit" className="btn btn-primary mt-16" disabled={loading}>
          {loading ? 'Envoi en cours…' : 'Soumettre le devis'}
        </button>
      </form>
    </div>
  )
}
