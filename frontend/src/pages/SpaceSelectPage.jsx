import { useNavigate } from 'react-router-dom'

const ESPACES = [
  {
    role: 'client',
    label: 'Espace Client',
    description: 'Soumettez une demande de devis et suivez son avancement.',
    initiale: 'C',
  },
  {
    role: 'employe',
    label: 'Espace Employé',
    description: 'Consultez vos dossiers attribués et rédigez des devis.',
    initiale: 'E',
  },
  {
    role: 'admin',
    label: 'Espace Admin',
    description: 'Gérez les comptes, attribuez les dossiers, suivez les statistiques.',
    initiale: 'A',
  },
]

export default function SpaceSelectPage() {
  const navigate = useNavigate()

  return (
    <div className="space-select-shell">
      <div className="space-select-header">
        <div className="eyebrow">QuoteFlow</div>
        <h1>Quel espace souhaitez-vous ouvrir ?</h1>
        <p>Choisissez votre profil pour accéder à la connexion adaptée.</p>
      </div>

      <div className="space-select-grid">
        {ESPACES.map((e) => (
          <button
            key={e.role}
            className="space-card"
            onClick={() => navigate(`/login?role=${e.role}`)}
          >
            <div className="space-card-icon">{e.initiale}</div>
            <h3>{e.label}</h3>
            <p>{e.description}</p>
            <span className="space-card-cta">Continuer →</span>
          </button>
        ))}
      </div>
    </div>
  )
}