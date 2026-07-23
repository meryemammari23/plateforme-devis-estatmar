import { useEffect, useState } from 'react'
import { dashboardApi } from '../../api/endpoints.js'
import Loader from '../../components/Loader.jsx'

const STATUT_LABELS = {
  EN_ATTENTE: 'En attente',
  EN_COURS: 'En cours',
  VALIDE: 'Validé',
  REFUSE: 'Refusé',
}

export default function AdminOverview() {
  const [stats, setStats] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    dashboardApi.stats()
      .then(({ data }) => setStats(data))
      .catch(() => setError('Impossible de charger les statistiques.'))
  }, [])

  if (error) return <div className="page-container"><div className="form-alert error">{error}</div></div>
  if (!stats) return <Loader />

  const repartition = stats.repartitionParStatut || {}
  const maxVal = Math.max(1, ...Object.values(repartition))

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <div className="eyebrow">Espace administrateur</div>
          <h1>Tableau de bord</h1>
          <p>Vue d'ensemble de l'activité de la plateforme QuoteFlow.</p>
        </div>
      </div>

      <div className="stats-grid">
        <div className="stat-card accent">
          <div className="stat-label">Demandes totales</div>
          <div className="stat-value">{stats.totalDemandes}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">En attente</div>
          <div className="stat-value">{stats.demandesEnAttente}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">En cours</div>
          <div className="stat-value">{stats.demandesEnCours}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Validées</div>
          <div className="stat-value">{stats.demandesValidees}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Refusées</div>
          <div className="stat-value">{stats.demandesRefusees}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Employés</div>
          <div className="stat-value">{stats.totalEmployes}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Clients</div>
          <div className="stat-value">{stats.totalClients}</div>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Répartition des demandes par statut</h3>
        {Object.keys(repartition).length === 0 ? (
          <p className="text-muted text-sm">Aucune donnée disponible pour le moment.</p>
        ) : (
          Object.entries(repartition).map(([statut, valeur]) => (
            <div className="bar-row" key={statut}>
              <div className="bar-label">{STATUT_LABELS[statut] || statut}</div>
              <div className="bar-track">
                <div className="bar-fill" style={{ width: `${(valeur / maxVal) * 100}%` }} />
              </div>
              <div className="bar-value">{valeur}</div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
