import { useState } from 'react'
import { useAuth } from '../../context/AuthContext.jsx'
import NouvelleDemande from './NouvelleDemande.jsx'
import MesDemandes from './MesDemandes.jsx'

export default function ClientDashboard() {
  const { user } = useAuth()
  const [tab, setTab] = useState('demandes')
  const [refreshKey, setRefreshKey] = useState(0)

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <div className="eyebrow">Espace client</div>
          <h1>Bonjour {user?.prenom} </h1>
          <p>Suivez vos demandes de devis et soumettez de nouveaux projets.</p>
        </div>
      </div>

      <div className="tabs">
        <button className={`tab ${tab === 'demandes' ? 'active' : ''}`} onClick={() => setTab('demandes')}>
          Mes demandes
        </button>
        <button className={`tab ${tab === 'nouvelle' ? 'active' : ''}`} onClick={() => setTab('nouvelle')}>
          Nouvelle demande
        </button>
      </div>

      {tab === 'demandes' && <MesDemandes refreshKey={refreshKey} />}
      {tab === 'nouvelle' && (
        <NouvelleDemande onCreated={() => { setRefreshKey((k) => k + 1); setTab('demandes') }} />
      )}
    </div>
  )
}
