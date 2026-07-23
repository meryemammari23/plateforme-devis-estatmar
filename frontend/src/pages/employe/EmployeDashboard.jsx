import { useState } from 'react'
import { useAuth } from '../../context/AuthContext.jsx'
import ListeDossiers from './ListeDossiers.jsx'
import CreerDevis from './CreerDevis.jsx'

export default function EmployeDashboard() {
  const { user } = useAuth()
  const [tab, setTab] = useState('dossiers')
  const [preselected, setPreselected] = useState(null)
  const [refreshKey, setRefreshKey] = useState(0)

  const goToCreerDevis = (demande) => {
    setPreselected(demande)
    setTab('devis')
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <div className="eyebrow">Espace employé</div>
          <h1>Bonjour {user?.prenom} 👋</h1>
          <p>Étudiez vos dossiers attribués et rédigez les devis correspondants.</p>
        </div>
      </div>

      <div className="tabs">
        <button className={`tab ${tab === 'dossiers' ? 'active' : ''}`} onClick={() => setTab('dossiers')}>
          Dossiers
        </button>
        <button
          className={`tab ${tab === 'devis' ? 'active' : ''}`}
          onClick={() => { setPreselected(null); setTab('devis') }}
        >
          Nouveau devis
        </button>
      </div>

      {tab === 'dossiers' && <ListeDossiers refreshKey={refreshKey} onCreerDevis={goToCreerDevis} />}
      {tab === 'devis' && (
        <CreerDevis
          preselected={preselected}
          onCreated={() => { setRefreshKey((k) => k + 1); setTab('dossiers') }}
        />
      )}
    </div>
  )
}
