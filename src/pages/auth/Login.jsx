import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login as loginApi } from '../../services/api/authApi';
import { useAuth } from '../../context/AuthContext';

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async () => {
    setError('');
    try {
      const data = await loginApi(form);
      login(data);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Connexion impossible');
    }
  };

  return (
    <div className="card" style={{ maxWidth: 400, margin: '40px auto' }}>
      <h2>Connexion</h2>
      {error && <div className="error">{error}</div>}
      <label>Email</label>
      <input type="email" value={form.email}
        onChange={(e) => setForm({ ...form, email: e.target.value })} />
      <label>Mot de passe</label>
      <input type="password" value={form.password}
        onChange={(e) => setForm({ ...form, password: e.target.value })} />
      <button className="btn" onClick={handleSubmit}>Se connecter</button>
      <p style={{ marginTop: 16 }}>Pas de compte ? <Link to="/register">S'inscrire</Link></p>
    </div>
  );
}
