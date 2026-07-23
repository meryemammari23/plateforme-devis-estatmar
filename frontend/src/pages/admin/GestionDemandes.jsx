import { useEffect, useState } from 'react'
import { adminApi, demandeApi } from '../../api/endpoints.js'
import StatutBadge from '../../components/StatutBadge.jsx'
import Loader from '../../components/Loader.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import DemandeDetailModal from '../../components/DemandeDetailModal.jsx'

const money = (v) =>
  new Intl.NumberFormat('fr-MA', { style: 'currency', currency: 'MAD' }).format(v || 0)

function AttributionSelect({ demande, employes, onAttribuer }) {
  const [employeId, setEmployeId] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async () => {
    if (!employeId) return
    setBusy(true)
    try {
      await onAttribuer(demande.id, Number(employeId))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="flex gap-8">
      <select className="select" value={employeId} onChange={(e) => setEmployeId(e.target.value)} style={{ width: 170 }}>
        <option value="">Attribuer à…</option>
        {employes.filter((e) => e.actif).map((e) => (
          <option key={e.id} value={e.id}>{e.prenom} {e.nom}</option>
        ))}
      </select>
      <button className="btn btn-primary btn-sm" onClick={submit} disabled={!employeId || busy}>
        {busy ? '…' : 'Valider'}
      </button>
    </div>
  )
}

export default function GestionDemandes() {
  const [demandes, setDemandes] = useState(null)
  const [employes, setEmployes] = useState([])
  const [enRetard, setEnRetard] = useState(null)
  const [selected, setSelected] = useState(null)
  const [error, setError] = useState('')

  const load = async () => {
    try {
      const [d, e, r] = await Promise.all([
        demandeApi.toutes(),
        adminApi.listerEmployes(),
        demandeApi.enRetard(3),
      ])
      setDemandes(d.data)
      setEmployes(e.data)
      setEnRetard(r.data)
    } catch {
      setError('Impossible de charger les demandes.')
    }
  }

  useEffect(() => { load() }, [])

  const handleAttribuer = async (demandeId, employeId) => {
    setError('')
    try {
      await adminApi.attribuer(demandeId, employeId)
      load()
    } catch (e) {
      setError(e.response?.data?.message || "Impossible d'attribuer ce dossier.")
    }
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <div className="eyebrow">Espace administrateur</div>
          <h1>Demandes</h1>
          <p>Attribuez les dossiers aux employés et suivez les demandes en retard.</p>
        </div>
      </div>

      {error && <div className="form-alert error">{error}</div>}

      {enRetard?.length > 0 && (
        <div className="card" style={{ marginBottom: 24, borderColor: 'var(--status-attente-fg)' }}>
          <h3 style={{ marginTop: 0 }}>⏱ Demandes en attente depuis plus de 3 jours</h3>
          <div className="table-wrap" style={{ border: 'none' }}>
            <table className="data-table">
              <thead>
                <tr><th>Référence</th><th>Client</th><th>Jours d'attente</th></tr>
              </thead>
              <tbody>
                {enRetard.map((r) => (
                  <tr key={r.id}>
                    <td><strong>{r.reference}</strong></td>
                    <td>{r.clientNomComplet}</td>
                    <td>{r.joursEnAttente} j</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {demandes === null ? (
        <Loader />
      ) : demandes.length === 0 ? (
        <EmptyState title="Aucune demande" description="Les nouvelles demandes des clients apparaîtront ici." />
      ) : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Référence</th>
                <th>Client</th>
                <th>Budget</th>
                <th>Statut</th>
                <th>Employé</th>
                <th style={{ minWidth: 220 }}></th>
              </tr>
            </thead>
            <tbody>
              {demandes.map((d) => (
                <tr key={d.id}>
                  <td><strong>{d.reference}</strong></td>
                  <td>{d.clientNomComplet}</td>
                  <td>{money(d.budget)}</td>
                  <td><StatutBadge statut={d.statut} /></td>
                  <td>{d.employeNomComplet || <span className="text-muted">—</span>}</td>
                  <td>
                    <div className="flex gap-8" style={{ justifyContent: 'flex-end' }}>
                      <button className="btn btn-outline btn-sm" onClick={() => setSelected(d)}>Voir</button>
                      {d.statut === 'EN_ATTENTE' && (
                        <AttributionSelect demande={d} employes={employes} onAttribuer={handleAttribuer} />
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <DemandeDetailModal
        demande={selected}
        onClose={() => setSelected(null)}
        onChanged={load}
        canRefuse
      />
    </div>
  )
}
