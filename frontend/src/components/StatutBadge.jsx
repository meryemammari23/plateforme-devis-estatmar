const CONFIG = {
  EN_ATTENTE: { label: 'En attente', className: 'badge-attente' },
  EN_COURS: { label: 'En cours', className: 'badge-cours' },
  VALIDE: { label: 'Validé', className: 'badge-valide' },
  REFUSE: { label: 'Refusé', className: 'badge-refuse' },
}

export default function StatutBadge({ statut }) {
  const cfg = CONFIG[statut] || { label: statut, className: 'badge-attente' }
  return <span className={`badge ${cfg.className}`}>{cfg.label}</span>
}
