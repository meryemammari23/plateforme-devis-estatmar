import { useEffect, useState } from 'react';
import { creerEmploye, listerEmployes, changerActivation } from '../../services/api/adminApi';

export default function GestionEmployes() {
  const [employes, setEmployes] = useState([]);
  const [form, setForm] = useState({ nom: '', prenom: '', email: '', password: '' });
  const [error, setError] = useState('');

  const charger = () => listerEmployes().then(setEmployes).catch(() => {});
  useEffect(() => { charger(); }, []);

  const handleCreer = async () => {
    setError('');
    try {
      await creerEmploye(form);
      setForm({ nom: '', prenom: '', email: '', password: '' });
      charger();
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création');
    }
  };

  const handleToggle = async (id) => { await changerActivation(id); charger(); };

  return (
    <>
      <div className="card">
        <h2>Créer un employé</h2>
        {error && <div className="error">{error}</div>}
        <input placeholder="Nom" value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} />
        <input placeholder="Prénom" value={form.prenom} onChange={(e) => setForm({ ...form, prenom: e.target.value })} />
        <input placeholder="Email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        <input placeholder="Mot de passe" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
        <button className="btn" onClick={handleCreer}>Créer</button>
      </div>

      <div className="card">
        <h2>Employés</h2>
        <table>
          <thead><tr><th>Nom</th><th>Email</th><th>Actif</th><th></th></tr></thead>
          <tbody>
            {employes.map((e) => (
              <tr key={e.id}>
                <td>{e.prenom} {e.nom}</td>
                <td>{e.email}</td>
                <td>{e.actif ? 'Oui' : 'Non'}</td>
                <td>
                  <button className={`btn ${e.actif ? 'danger' : ''}`} onClick={() => handleToggle(e.id)}>
                    {e.actif ? 'Désactiver' : 'Activer'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
