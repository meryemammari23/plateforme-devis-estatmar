import { useEffect, useState } from 'react'
import { demandeApi } from '../../api/endpoints.js'
import StatutBadge from '../../components/StatutBadge.jsx'
import Loader from '../../components/Loader.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import DemandeDetailModal from '../../components/DemandeDetailModal.jsx'

const money = (v) =>
  new Intl.NumberFormat('fr-MA', { style: 'currency', currency: 'MAD' }).format(v || 0)

export default function MesDemandes({ refreshKey }) {
  const [demandes, setDemandes] = useState(null)
  const [selected, setSelected] = useState(null)
  const [error, setError] = useState('')

  const load = async () => {
    try {
      const { data } = await demandeApi.mesDemandes()
      setDemandes(data)
    } catch {
      setError('Impossible de charger vos demandes.')
    }
  }

  useEffect(() => { load() }, [refreshKey])

  if (error) return <div className="form-alert error">{error}</div>
  if (demandes === null) return <Loader />

  if (demandes.length === 0) {
    return (
      <EmptyState
        title="Aucune demande pour le moment"
        description="Utilisez l'onglet « Nouvelle demande » pour soumettre votre premier projet."
      />
    )
  }

  return (
    <>
      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Référence</th>
              <th>Description</th>
              <th>Budget</th>
              <th>Statut</th>
              <th>Employé</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {demandes.map((d) => (
              <tr key={d.id}>
                <td><strong>{d.reference}</strong></td>
                <td style={{ maxWidth: 280, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {d.description}
                </td>
                <td>{money(d.budget)}</td>
                <td><StatutBadge statut={d.statut} /></td>
                <td>{d.employeNomComplet || <span className="text-muted">Non attribué</span>}</td>
                <td>
                  <button className="btn btn-outline btn-sm" onClick={() => setSelected(d)}>Voir</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <DemandeDetailModal
        demande={selected}
        onClose={() => setSelected(null)}
        onChanged={load}
        canRefuse
        refuseLabel="Annuler ma demande"
      />
    </>
  )
}