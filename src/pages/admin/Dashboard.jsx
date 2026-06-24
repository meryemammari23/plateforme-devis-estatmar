import { useEffect, useState } from 'react';
import { statsGlobales } from '../../services/api/dashboardApi';
import {
  BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend,
} from 'recharts';

// Couleurs alignées sur les badges de statut : EN_ATTENTE, EN_COURS, VALIDE, REFUSE
const COULEURS = ['#fbbf24', '#a5b4fc', '#34d6ad', '#f87171'];

export default function Dashboard() {
  const [stats, setStats] = useState(null);

  useEffect(() => { statsGlobales().then(setStats).catch(() => {}); }, []);

  if (!stats) return <div className="card">Chargement...</div>;

  const data = Object.entries(stats.repartitionParStatut).map(([statut, nombre]) => ({
    statut, nombre,
  }));

  return (
    <>
      <div className="card grid">
        <div className="stat"><div className="num">{stats.totalDemandes}</div><div>Demandes</div></div>
        <div className="stat"><div className="num">{stats.demandesValidees}</div><div>Validées</div></div>
        <div className="stat"><div className="num">{stats.demandesRefusees}</div><div>Refusées</div></div>
        <div className="stat"><div className="num">{stats.totalEmployes}</div><div>Employés</div></div>
        <div className="stat"><div className="num">{stats.totalClients}</div><div>Clients</div></div>
      </div>

      <div className="card">
        <h3>Demandes par statut</h3>
        <ResponsiveContainer width="100%" height={280}>
          <BarChart data={data}>
            <XAxis dataKey="statut" tick={{ fill: '#93a0b8' }} />
            <YAxis allowDecimals={false} tick={{ fill: '#93a0b8' }} />
            <Tooltip contentStyle={{ background: '#161f33', border: '1px solid #283450', borderRadius: 8, color: '#e6eaf2' }} />
            <Bar dataKey="nombre" fill="#15c39a" radius={[6, 6, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="card">
        <h3>Répartition</h3>
        <ResponsiveContainer width="100%" height={280}>
          <PieChart>
            <Pie data={data} dataKey="nombre" nameKey="statut" outerRadius={100} label>
              {data.map((_, i) => <Cell key={i} fill={COULEURS[i % COULEURS.length]} />)}
            </Pie>
            <Legend /><Tooltip contentStyle={{ background: '#161f33', border: '1px solid #283450', borderRadius: 8, color: '#e6eaf2' }} />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </>
  );
}
