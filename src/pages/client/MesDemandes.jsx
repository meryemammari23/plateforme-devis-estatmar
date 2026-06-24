import { useEffect, useState } from 'react';
import { mesDemandes, telechargerPieceJointe } from '../../services/api/demandeApi';
import { telechargerDevisPdf } from '../../services/api/devisApi';
import StatutBadge from '../../components/common/StatutBadge';

export default function MesDemandes() {
  const [demandes, setDemandes] = useState([]);

  useEffect(() => { mesDemandes().then(setDemandes).catch(() => {}); }, []);

  return (
    <div className="card">
      <h2>Mes demandes</h2>
      {demandes.length === 0 ? <p>Aucune demande pour l'instant.</p> : (
        <table>
          <thead>
            <tr>
              <th>Référence</th><th>Description</th><th>Budget</th>
              <th>Statut</th><th>Pièces jointes</th><th>Devis</th>
            </tr>
          </thead>
          <tbody>
            {demandes.map((d) => (
              <tr key={d.id}>
                <td>{d.reference}</td>
                <td>{d.description}</td>
                <td>{d.budget} MAD</td>
                <td><StatutBadge statut={d.statut} /></td>
                <td>
                  {d.piecesJointes && d.piecesJointes.length > 0 ? (
                    d.piecesJointes.map((pj) => (
                      <div key={pj.id}>
                        <a href="#" onClick={(e) => {
                          e.preventDefault();
                          telechargerPieceJointe(pj.id, pj.nomOriginal);
                        }}>
                          📄 {pj.nomOriginal}
                        </a>
                      </div>
                    ))
                  ) : <span style={{ color: 'var(--muted)' }}>—</span>}
                </td>
                <td>
                  {d.devisId ? (
                    <button className="btn" style={{ padding: '6px 12px', fontSize: 13 }}
                      onClick={() => telechargerDevisPdf(d.devisId, d.reference)}>
                      ⬇ Télécharger le devis
                    </button>
                  ) : <span style={{ color: 'var(--muted)' }}>—</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
