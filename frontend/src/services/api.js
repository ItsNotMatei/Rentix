import axios from 'axios'
import { API_BASE } from '@/lib/utils'
import { getErrorMessage } from '@/lib/errors'

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
})

let refreshing = false
let queue = []

const processQueue = (error) => {
  queue.forEach((prom) => {
    if (error) prom.reject(error)
    else prom.resolve()
  })
  queue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.data && typeof error.response.data === 'object') {
      error.friendlyMessage = getErrorMessage(error)
    }
    const original = error.config
    if (error.response?.status !== 401 || original?._retry) {
      return Promise.reject(error)
    }

    if (refreshing) {
      return new Promise((resolve, reject) => {
        queue.push({ resolve, reject })
      }).then(() => api(original))
    }

    original._retry = true
    refreshing = true

    try {
      await axios.post(`${API_BASE}/api/auth/refresh`, {}, { withCredentials: true })
      processQueue(null)
      return api(original)
    } catch (refreshError) {
      processQueue(refreshError)
      clearSession()
      return Promise.reject(refreshError)
    } finally {
      refreshing = false
    }
  }
)

export function clearSession() {
  sessionStorage.removeItem('user')
}

export function getStoredUser() {
  try {
    return JSON.parse(sessionStorage.getItem('user'))
  } catch {
    return null
  }
}

export function setStoredUser(user) {
  if (user) sessionStorage.setItem('user', JSON.stringify(user))
  else sessionStorage.removeItem('user')
}

export function hasRole(minRole) {
  const user = getStoredUser()
  if (!user?.role) return false
  const order = ['USER', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN']
  return order.indexOf(user.role) >= order.indexOf(minRole)
}

export async function ensureAuth() {
  const cached = getStoredUser()
  if (cached?.id) return cached
  const res = await api.get('/api/auth/me')
  setStoredUser(res.data)
  return res.data
}

export default api
