import client from './axiosClient';

export const statsGlobales = () => client.get('/dashboard/stats').then(r => r.data);
export const statsEmploye = (id) => client.get(`/dashboard/employe/${id}/stats`).then(r => r.data);
