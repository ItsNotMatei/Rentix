import axios from 'axios'
import api, { clearSession, setStoredUser } from './api'
import { API_BASE } from '@/lib/utils'

const authApi = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
})

const register = (username, email, password) =>
  authApi.post('/api/auth/signup', { nume: username, email, password })

/** Staff: AuthResponse cu user. Utilizatori: { requiresTwoFactor, challengeId, message } */
const login = async (email, password) => {
  const response = await authApi.post('/api/auth/signin', { email, password })
  const data = response.data
  if (data?.user) {
    setStoredUser(data.user)
  }
  return data
}

const verify2fa = async (challengeId, code) => {
  const response = await authApi.post('/api/auth/verify-2fa', { challengeId, code })
  setStoredUser(response.data.user)
  return response.data.user
}

const forgotPassword = (email) =>
  authApi.post('/api/auth/forgot-password', { email })

const resetPassword = (token, newPassword) =>
  authApi.post('/api/auth/reset-password', { token, newPassword })

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

export default { register, login, verify2fa, forgotPassword, resetPassword, logout, me }
