import axios from 'axios'
import api, { clearSession, setStoredUser } from './api'
import { API_BASE } from '@/lib/utils'
import { notifyError } from '@/lib/errors'

const authApi = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
})

const register = (username, email, password) =>
  authApi.post('/api/auth/signup', { nume: username, email, password })

const login = async (email, password) => {
  const response = await authApi.post('/api/auth/signin', { email, password })
  setStoredUser(response.data.user)
  return response.data.user
}

const logout = async () => {
  try {
    await api.post('/api/auth/logout', {})
  } catch {
    /* ignore */
  } finally {
    clearSession()
  }
}

const me = async () => {
  const response = await api.get('/api/auth/me')
  setStoredUser(response.data)
  return response.data
}

export default { register, login, logout, me }
