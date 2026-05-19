import api from './api'

export const submitListingReport = (anuntId, reason) =>
  api.post('/api/reports', { anuntId, reason }).then((r) => r.data)
