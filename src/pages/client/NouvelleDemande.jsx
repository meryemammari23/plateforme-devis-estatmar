import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { creerDemande, ajouterPiecesJointes } from '../../services/api/demandeApi';

export default function NouvelleDemande() {
  const [form, setForm] = useState({ description: '', budget: '' });
  const [fichiers, setFichiers] = useState([]);
  const [error, setError] = useState('');
  const [envoi, setEnvoi] = useState(false);
  const navigate = useNavigate();

  // N'accepte que des PDF cote interface.
  const handleFichiers = (e) => {
    const selection = Array.from(e.target.files);
    const nonPdf = selection.find((f) => f.type !== 'application/pdf');
    if (nonPdf) {
      setError('Seuls les fichiers PDF sont acceptes.');
      return;
    }
    setError('');
    setFichiers(selection);
  };

  const handleSubmit = async () => {
    setError('');
    setEnvoi(true);
    try {
      // 1. Creer la demande (description + budget)
      const demande = await creerDemande({
        description: form.description,
        budget: parseFloat(form.budget),
      });
      // 2. Si des PDF ont ete choisis, les uploader sur la demande creee
      if (fichiers.length > 0) {
        await ajouterPiecesJointes(demande.id, fichiers);
      }
      navigate('/client/mes-demandes');
    } catch (err) {
      // Affiche le detail par champ s'il existe (ex: budget positif), sinon le message global.
      const data = err.response?.data;
      const champs = data?.validationErrors
        ? Object.values(data.validationErrors).join(' ')
        : null;
      setError(champs || data?.message || "Erreur lors de l'envoi");
    } finally {
      setEnvoi(false);
    }
  };

  return (
    <div className="card">
      <h2>Nouvelle demande de devis</h2>
      {error && <div className="error">{error}</div>}

      <label>Description du projet</label>
      <textarea rows={5} value={form.description}
        onChange={(e) => setForm({ ...form, description: e.target.value })} />

      <label>Budget proposé (MAD)</label>
      <input type="number" value={form.budget}
        onChange={(e) => setForm({ ...form, budget: e.target.value })} />

      <label>Pièces jointes (PDF uniquement)</label>
      <input type="file" accept="application/pdf" multiple onChange={handleFichiers} />
      {fichiers.length > 0 && (
        <ul style={{ marginTop: -4, marginBottom: 12, fontSize: 13, color: 'var(--muted)' }}>
          {fichiers.map((f, i) => (
            <li key={i}>{f.name} ({Math.round(f.size / 1024)} Ko)</li>
          ))}
        </ul>
      )}

      <button className="btn" onClick={handleSubmit} disabled={envoi}>
        {envoi ? 'Envoi en cours...' : 'Soumettre'}
      </button>
    </div>
  );
}
