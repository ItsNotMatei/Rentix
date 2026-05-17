import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { ensureAuth } from '@/services/api'
import { Skeleton } from '@/components/ui/skeleton'

export default function ProtectedRoute({ children }) {
  const [state, setState] = useState('loading')

  useEffect(() => {
    ensureAuth()
      .then(() => setState('ok'))
      .catch(() => setState('denied'))
  }, [])

  if (state === 'loading') {
    return (
      <div className="container-rentix py-20">
        <Skeleton className="mx-auto h-8 w-48" />
      </div>
    )
  }
  if (state === 'denied') return <Navigate to="/login" replace />
  return children
}
