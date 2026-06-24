import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { toutesDemandes } from '../../services/api/demandeApi';
import { useAuth } from '../../context/AuthContext';
import StatutBadge from '../../components/common/StatutBadge';

export default function DossiersAttribues() {
  const [dossiers, setDossiers] = useState([]);
  const { auth } = useAuth();

  useEffect(() => {
    // Filtre cote front : l'employe ne voit que ses dossiers attribues.
    toutesDemandes()
      .then((all) => setDossiers(all.filter((d) => d.employeId === auth.userId)))
      .catch(() => {});
  }, [auth.userId]);

  return (
    <div className="card">
      <h2>Mes dossiers attribués</h2>
      {dossiers.length === 0 ? <p>Aucun dossier attribué.</p> : (
        <table>
          <thead><tr><th>Référence</th><th>Description</th><th>Budget</th><th>Statut</th><th></th></tr></thead>
          <tbody>
            {dossiers.map((d) => (
              <tr key={d.id}>
                <td>{d.reference}</td>
                <td>{d.description}</td>
                <td>{d.budget} MAD</td>
                <td><StatutBadge statut={d.statut} /></td>
                <td>
                  {d.statut === 'EN_COURS' &&
                    <Link className="btn" to={`/employe/devis/${d.id}`}>Rédiger le devis</Link>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
