import api from './api'

export async function getMyFavorites() {
  const res = await api.get('/api/favorites')
  return res.data
}

export async function checkFavorite(productId) {
  const res = await api.get(`/api/favorites/check/${productId}`)
  return res.data.favorited
}

export async function toggleFavorite(productId) {
  const res = await api.post(`/api/favorites/toggle/${productId}`)
  return res.data
}
