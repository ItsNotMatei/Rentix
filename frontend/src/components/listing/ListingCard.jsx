import { useNavigate } from 'react-router-dom'
import { CheckCircle, MapPin } from 'lucide-react'
import { motion } from 'framer-motion'
import FavoriteButton from './FavoriteButton'
import { Badge } from '@/components/ui/badge'
import { conditionLabel } from '@/lib/listingMeta'

export default function ListingCard({ listing, showAdminDelete, onDelete }) {
  const navigate = useNavigate()
  const image = listing.imageUrl || listing.images?.[0]

  return (
    <motion.article
      layout
      whileHover={{ y: -4 }}
      className="group relative cursor-pointer overflow-hidden rounded-2xl border border-border bg-white shadow-[var(--shadow-card)]"
      onClick={() => navigate(`/product/${listing.id}`)}
    >
      <div className="absolute right-3 top-3 z-10" onClick={(e) => e.stopPropagation()}>
        <FavoriteButton productId={listing.id} />
      </div>
      {showAdminDelete && (
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation()
            onDelete?.(listing.id)
          }}
          className="absolute left-3 top-3 z-10 rounded-full bg-red-500 px-2 py-1 text-xs text-white"
        >
          Șterge
        </button>
      )}
      <div className="relative aspect-[4/3] overflow-hidden bg-slate-100">
        <img
          src={image}
          alt={listing.titlu}
          loading="lazy"
          className="h-full w-full object-cover transition duration-500 group-hover:scale-105"
        />
        <div className="absolute bottom-3 left-3 flex flex-wrap gap-1">
          <Badge>{listing.categorie || 'Altele'}</Badge>
          {listing.stareProdus && <Badge variant="secondary" className="bg-white/90 text-xs">{conditionLabel(listing.stareProdus)}</Badge>}
        </div>
      </div>
      <div className="space-y-2 p-4">
        <h3 className="line-clamp-2 font-semibold text-text">{listing.titlu}</h3>
        <div className="flex items-center gap-1 text-sm text-text-muted">
          <span className="flex h-7 w-7 items-center justify-center rounded-full bg-brand-100 text-xs font-bold text-brand-700">
            {(listing.user?.nume || listing.ownerName || 'U')[0]}
          </span>
          <span>{listing.user?.nume || listing.ownerName || 'Proprietar'}</span>
          {(listing.user?.verified || listing.ownerVerified) && (
            <CheckCircle size={14} className="text-brand-600" />
          )}
        </div>
        <div className="flex items-end justify-between gap-2">
          <span className="flex items-center gap-1 text-xs text-text-muted">
            <MapPin size={14} />
            {listing.adresa || 'România'}
          </span>
          <p className="text-right">
            <span className="text-lg font-bold text-brand-700">{listing.pret}</span>
            <span className="text-xs text-text-muted"> RON/zi</span>
          </p>
        </div>
      </div>
    </motion.article>
  )
}
