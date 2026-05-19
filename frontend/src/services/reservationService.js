import api from './api'

export const getPendingReturns = () =>
  api.get('/reservations/owner/pending-returns').then((r) => r.data)

export const confirmReturn = (reservationId, condition) =>
  api.post(`/reservations/${reservationId}/confirm-return`, { condition }).then((r) => r.data)
