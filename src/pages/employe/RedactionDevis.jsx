import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { creerDevis } from '../../services/api/devisApi';

export default function RedactionDevis() {
  const { demandeId } = useParams();
  const navigate = useNavigate();
  const [commentaire, setCommentaire] = useState('');
  const [lignes, setLignes] = useState([{ designation: '', quantite: 1, prixUnitaire: 0 }]);
  const [error, setError] = useState('');

  const updateLigne = (i, champ, valeur) => {
    const copie = [...lignes];
    copie[i][champ] = valeur;
    setLignes(copie);
  };
  const ajouterLigne = () =>
    setLignes([...lignes, { designation: '', quantite: 1, prixUnitaire: 0 }]);
  const supprimerLigne = (i) => setLignes(lignes.filter((_, idx) => idx !== i));

  const total = lignes.reduce((s, l) => s + (l.quantite * l.prixUnitaire || 0), 0);

  const handleSubmit = async () => {
    setError('');
    try {
      await creerDevis({
        demandeId: parseInt(demandeId, 10),
        commentaire,
        lignes: lignes.map((l) => ({
          designation: l.designation,
          quantite: parseInt(l.quantite, 10),
          prixUnitaire: parseFloat(l.prixUnitaire),
        })),
      });
      navigate('/employe/dossiers');
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création du devis');
    }
  };

  return (
    <div className="card">
      <h2>Rédaction du devis</h2>
      {error && <div className="error">{error}</div>}
      <table>
        <thead><tr><th>Désignation</th><th>Qté</th><th>Prix unitaire</th><th>Sous-total</th><th></th></tr></thead>
        <tbody>
          {lignes.map((l, i) => (
            <tr key={i}>
              <td><input value={l.designation} onChange={(e) => updateLigne(i, 'designation', e.target.value)} /></td>
              <td><input type="number" value={l.quantite} onChange={(e) => updateLigne(i, 'quantite', e.target.value)} /></td>
              <td><input type="number" value={l.prixUnitaire} onChange={(e) => updateLigne(i, 'prixUnitaire', e.target.value)} /></td>
              <td>{(l.quantite * l.prixUnitaire || 0).toFixed(2)} MAD</td>
              <td><button className="btn danger" onClick={() => supprimerLigne(i)}>X</button></td>
            </tr>
          ))}
        </tbody>
      </table>
      <button className="btn secondary" onClick={ajouterLigne} style={{ marginTop: 8 }}>+ Ligne</button>

      <h3 style={{ textAlign: 'right' }}>Total : {total.toFixed(2)} MAD</h3>

      <label>Commentaire</label>
      <textarea rows={3} value={commentaire} onChange={(e) => setCommentaire(e.target.value)} />
      <button className="btn" onClick={handleSubmit}>Valider et générer le PDF</button>
    </div>
  );
}
