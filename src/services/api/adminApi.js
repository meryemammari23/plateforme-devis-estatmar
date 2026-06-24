import client from './axiosClient';

export const creerEmploye = (data) => client.post('/admin/employes', data).then(r => r.data);
export const listerEmployes = () => client.get('/admin/employes').then(r => r.data);
export const changerActivation = (id) => client.put(`/admin/employes/${id}/activer`).then(r => r.data);
export const attribuer = (demandeId, employeId) =>
  client.put(`/admin/demandes/${demandeId}/attribuer`, null, { params: { employeId } }).then(r => r.data);
