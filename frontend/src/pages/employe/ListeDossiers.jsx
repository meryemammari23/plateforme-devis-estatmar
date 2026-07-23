import { useEffect, useState } from 'react'
import { demandeApi } from '../../api/endpoints.js'
import { useAuth } from '../../context/AuthContext.jsx'
import StatutBadge from '../../components/StatutBadge.jsx'
import Loader from '../../components/Loader.jsx'
import EmptyState from '../../components/EmptyState.jsx'
import DemandeDetailModal from '../../components/DemandeDetailModal.jsx'

const money = (v) =>
  new Intl.NumberFormat('fr-MA', { style: 'currency', currency: 'MAD' }).format(v || 0)

export default function ListeDossiers({ refreshKey, onCreerDevis }) {
  const { user } = useAuth()
  const [demandes, setDemandes] = useState(null)
  const [selected, setSelected] = useState(null)
  const [filtre, setFiltre] = useState('mes') // 'mes' | 'toutes'
  const [error, setError] = useState('')

  const load = async () => {
    try {
      const { data } = await demandeApi.toutes()
      setDemandes(data)
    } catch {
      setError('Impossible de charger les dossiers.')
    }
  }

  useEffect(() => { load() }, [refreshKey])

  if (error) return <div className="form-alert error">{error}</div>
  if (demandes === null) return <Loader />

  const visibles = filtre === 'mes'
    ? demandes.filter((d) => d.employeId === user.userId)
    : demandes

  return (
    <>
      <div className="flex gap-8" style={{ marginBottom: 18 }}>
        <button className={`btn btn-sm ${filtre === 'mes' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setFiltre('mes')}>
          Mes dossiers attribués
        </button>
        <button className={`btn btn-sm ${filtre === 'toutes' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setFiltre('toutes')}>
          Toutes les demandes
        </button>
      </div>

      {visibles.length === 0 ? (
        <EmptyState
          title="Aucun dossier ici"
          description={filtre === 'mes' ? "Aucun dossier ne vous a encore été attribué par l'administrateur." : 'Aucune demande dans le système.'}
        />
      ) : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Référence</th>
                <th>Client</th>
                <th>Budget</th>
                <th>Statut</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {visibles.map((d) => (
                <tr key={d.id}>
                  <td><strong>{d.reference}</strong></td>
                  <td>{d.clientNomComplet}</td>
                  <td>{money(d.budget)}</td>
                  <td><StatutBadge statut={d.statut} /></td>
                  <td className="flex gap-8">
                    <button className="btn btn-outline btn-sm" onClick={() => setSelected(d)}>Voir</button>
                    {d.statut === 'EN_COURS' && !d.devisId && d.employeId === user.userId && (
                      <button className="btn btn-primary btn-sm" onClick={() => onCreerDevis(d)}>
                        Créer le devis
                      </button>
                    )}
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
        canRefuse={false}
      />
    </>
  )
}
