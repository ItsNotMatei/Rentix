import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { ensureAuth, hasRole } from '@/services/api'

export default function AdminRoute({ children }) {
  const [state, setState] = useState('loading')

  useEffect(() => {
    ensureAuth()
      .then(() => setState(hasRole('MODERATOR') ? 'ok' : 'denied'))
      .catch(() => setState('denied'))
  }, [])

  if (state === 'loading') return null
  if (state === 'denied') return <Navigate to="/" replace />
  return children
}
