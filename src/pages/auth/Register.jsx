import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register as registerApi } from '../../services/api/authApi';
import { useAuth } from '../../context/AuthContext';

export default function Register() {
  const [form, setForm] = useState({ nom: '', prenom: '', email: '', password: '' });
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async () => {
    setError('');
    try {
      const data = await registerApi(form);
      login(data);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || "Inscription impossible");
    }
  };

  return (
    <div className="card" style={{ maxWidth: 400, margin: '40px auto' }}>
      <h2>Inscription client</h2>
      {error && <div className="error">{error}</div>}
      <label>Nom</label>
      <input value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} />
      <label>Prénom</label>
      <input value={form.prenom} onChange={(e) => setForm({ ...form, prenom: e.target.value })} />
      <label>Email</label>
      <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
      <label>Mot de passe</label>
      <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
      <button className="btn" onClick={handleSubmit}>S'inscrire</button>
      <p style={{ marginTop: 16 }}>Déjà inscrit ? <Link to="/login">Se connecter</Link></p>
    </div>
  );
}
