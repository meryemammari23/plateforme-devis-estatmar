import client from './axiosClient';

export const creerDevis = (data) => client.post('/devis', data).then(r => r.data);
export const voirDevis = (id) => client.get(`/devis/${id}`).then(r => r.data);
export const urlPdf = (id) => `${import.meta.env.VITE_API_BASE_URL}/devis/${id}/pdf`;

// Telechargement du PDF du devis via axios : le token JWT est envoye
// automatiquement (evite le 403 obtenu en ouvrant l'URL directement).
export const telechargerDevisPdf = async (devisId, reference) => {
  const res = await client.get(`/devis/${devisId}/pdf`, { responseType: 'blob' });
  const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
  const a = document.createElement('a');
  a.href = url;
  a.download = `devis-${reference || devisId}.pdf`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
};
