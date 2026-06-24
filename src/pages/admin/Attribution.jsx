import { useEffect, useState } from 'react';
import { toutesDemandes, refuserDemande } from '../../services/api/demandeApi';
import { listerEmployes, attribuer } from '../../services/api/adminApi';
import StatutBadge from '../../components/common/StatutBadge';

export default function Attribution() {
  const [demandes, setDemandes] = useState([]);
  const [employes, setEmployes] = useState([]);
  const [selection, setSelection] = useState({});

  const charger = () => toutesDemandes().then(setDemandes).catch(() => {});
  useEffect(() => {
    charger();
    listerEmployes().then(setEmployes).catch(() => {});
  }, []);

  const handleAttribuer = async (demandeId) => {
    const employeId = selection[demandeId];
    if (!employeId) return;
    await attribuer(demandeId, employeId);
    charger();
  };

  const handleRefuser = async (id) => { await refuserDemande(id); charger(); };

  return (
    <div className="card">
      <h2>Demandes & attribution</h2>
      <table>
        <thead><tr><th>Réf.</th><th>Client</th><th>Statut</th><th>Employé</th><th>Actions</th></tr></thead>
        <tbody>
          {demandes.map((d) => (
            <tr key={d.id}>
              <td>{d.reference}</td>
              <td>{d.clientNomComplet}</td>
              <td><StatutBadge statut={d.statut} /></td>
              <td>{d.employeNomComplet || '—'}</td>
              <td>
                {d.statut === 'EN_ATTENTE' && (
                  <>
                    <select value={selection[d.id] || ''}
                      onChange={(e) => setSelection({ ...selection, [d.id]: e.target.value })}
                      style={{ width: 'auto', display: 'inline-block', marginRight: 8 }}>
                      <option value="">Choisir...</option>
                      {employes.filter((e) => e.actif).map((e) => (
                        <option key={e.id} value={e.id}>{e.prenom} {e.nom}</option>
                      ))}
                    </select>
                    <button className="btn" onClick={() => handleAttribuer(d.id)}>Attribuer</button>
                    <button className="btn danger" onClick={() => handleRefuser(d.id)} style={{ marginLeft: 6 }}>Refuser</button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
