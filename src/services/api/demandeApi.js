import client from './axiosClient';

export const creerDemande = (data) => client.post('/demandes', data).then(r => r.data);
export const mesDemandes = () => client.get('/demandes/mes-demandes').then(r => r.data);
export const toutesDemandes = () => client.get('/demandes').then(r => r.data);
export const detailDemande = (id) => client.get(`/demandes/${id}`).then(r => r.data);
export const refuserDemande = (id) => client.put(`/demandes/${id}/refuser`).then(r => r.data);

// Upload de pieces jointes PDF (multipart). On laisse le navigateur poser le
// bon Content-Type avec boundary en mettant la valeur a undefined.
export const ajouterPiecesJointes = (demandeId, fichiers) => {
  const formData = new FormData();
  for (const f of fichiers) formData.append('fichiers', f);
  return client
    .post(`/demandes/${demandeId}/pieces-jointes`, formData, {
      headers: { 'Content-Type': undefined },
    })
    .then(r => r.data);
};

// Telechargement via axios (le token JWT est ainsi bien envoye).
export const telechargerPieceJointe = async (pieceJointeId, nomFichier) => {
  const res = await client.get(`/demandes/pieces-jointes/${pieceJointeId}`, {
    responseType: 'blob',
  });
  const url = window.URL.createObjectURL(new Blob([res.data]));
  const a = document.createElement('a');
  a.href = url;
  a.download = nomFichier || 'piece-jointe.pdf';
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
};
