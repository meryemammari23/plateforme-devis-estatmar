import api from './axios.js'

/* ---------------- Auth ---------------- */
export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
}

/* ---------------- Demandes de devis ---------------- */
export const demandeApi = {
  soumettre: (data) => api.post('/demandes', data),
  mesDemandes: () => api.get('/demandes/mes-demandes'),
  toutes: () => api.get('/demandes'),
  detail: (id) => api.get(`/demandes/${id}`),
  ajouterPiecesJointes: (id, fichiers) => {
    const form = new FormData()
    fichiers.forEach((f) => form.append('fichiers', f))
    return api.post(`/demandes/${id}/pieces-jointes`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  telechargerPieceJointe: (pieceJointeId) =>
    api.get(`/demandes/pieces-jointes/${pieceJointeId}`, { responseType: 'blob' }),
  enRetard: (jours = 3) => api.get('/demandes/en-retard', { params: { jours } }),
  refuser: (id) => api.put(`/demandes/${id}/refuser`),
}

/* ---------------- Devis ---------------- */
export const devisApi = {
  creer: (data) => api.post('/devis', data),
  voir: (id) => api.get(`/devis/${id}`),
  telechargerPdf: (id) => api.get(`/devis/${id}/pdf`, { responseType: 'blob' }),
}

/* ---------------- Administration ---------------- */
export const adminApi = {
  creerEmploye: (data) => api.post('/admin/employes', data),
  listerEmployes: () => api.get('/admin/employes'),
  changerActivation: (id) => api.put(`/admin/employes/${id}/activer`),
  attribuer: (demandeId, employeId) =>
    api.put(`/admin/demandes/${demandeId}/attribuer`, null, { params: { employeId } }),
}

/* ---------------- Dashboard ---------------- */
export const dashboardApi = {
  stats: () => api.get('/dashboard/stats'),
  statsEmploye: (id) => api.get(`/dashboard/employe/${id}/stats`),
}

/** Déclenche le téléchargement d'un blob dans le navigateur avec un nom de fichier donné */
export function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
