import { useEffect, useState } from 'react'
import { Heart } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { checkFavorite, toggleFavorite } from '@/services/favoriteService'
import { getStoredUser } from '@/services/api'
import { cn } from '@/lib/utils'

export default function FavoriteButton({ productId, className, size = 20 }) {
  const navigate = useNavigate()
  const [active, setActive] = useState(false)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const user = getStoredUser()
    if (!user || !productId) return
    checkFavorite(productId).then(setActive).catch(() => setActive(false))
  }, [productId])

  const onClick = async (e) => {
    e?.stopPropagation?.()
    const user = getStoredUser()
    if (!user) {
      navigate('/login')
      return
    }
    setLoading(true)
    try {
      const res = await toggleFavorite(productId)
      setActive(res.favorited)
    } catch {
      /* ignore */
    } finally {
      setLoading(false)
    }
  }

  return (
    <button
      type="button"
      onClick={onClick}
      disabled={loading}
      aria-label={active ? 'Elimină din favorite' : 'Adaugă la favorite'}
      className={cn(
        'flex h-10 w-10 items-center justify-center rounded-full border border-border bg-white/95 shadow-sm transition hover:scale-105',
        active && 'border-red-200 bg-red-50',
        className
      )}
    >
      <Heart
        size={size}
        className={active ? 'fill-red-500 text-red-500' : 'text-slate-600'}
      />
    </button>
  )
}
