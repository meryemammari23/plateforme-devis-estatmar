import { STATUT_LABELS } from '../../utils/constants';

export default function StatutBadge({ statut }) {
  return <span className={`badge ${statut}`}>{STATUT_LABELS[statut] || statut}</span>;
}
