import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Plus, TrendingUp } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import ListingCard from '@/components/listing/ListingCard'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import api, { getStoredUser, hasRole } from '@/services/api'
import { LISTING_CATEGORIES } from '@/lib/listingMeta'

export default function Home() {
  const navigate = useNavigate()
  const [anunturi, setAnunturi] = useState([])
  const [loading, setLoading] = useState(true)
  const user = getStoredUser()

  useEffect(() => {
    api.get('/api/products')
      .then((res) => setAnunturi(res.data))
      .catch(() => setAnunturi([]))
      .finally(() => setLoading(false))
  }, [])

  const handleDelete = async (id) => {
    if (!window.confirm('Ștergi acest anunț?')) return
    await api.delete(`/api/products/${id}`)
    setAnunturi((prev) => prev.filter((a) => a.id !== id))
  }

  const featured = anunturi.slice(0, 8)
  const latest = [...anunturi].reverse().slice(0, 8)

  return (
    <AppLayout>
      <section className="container-rentix grid items-center gap-10 py-10 lg:grid-cols-2 lg:py-16">
        <motion.div initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }}>
          <h1 className="text-4xl font-bold leading-tight tracking-tight text-text md:text-5xl">
            Închiriază inteligent. <span className="text-brand-600">Folosește ce ai nevoie.</span>
          </h1>
          <p className="mt-4 max-w-lg text-text-muted">
            Rentix conectează proprietari și chiriași într-o experiență rapidă, sigură și family-friendly.
          </p>
          <div className="mt-6 flex flex-wrap gap-3">
            <Button size="lg" onClick={() => document.getElementById('listings')?.scrollIntoView({ behavior: 'smooth' })}>
              Explorează catalogul
            </Button>
            <Button variant="secondary" size="lg" onClick={() => navigate('/anunturi')}>
              Caută anunțuri
            </Button>
          </div>
        </motion.div>
        <motion.div
          initial={{ opacity: 0, scale: 0.96 }}
          animate={{ opacity: 1, scale: 1 }}
          className="overflow-hidden rounded-3xl shadow-xl"
        >
          <img
            src="https://images.unsplash.com/photo-1529139574466-a303027c1d8b?q=80&w=1200&auto=format&fit=crop"
            alt="Rentix hero"
            className="aspect-[4/3] w-full object-cover"
          />
        </motion.div>
      </section>

      <section className="container-rentix pb-8">
        <h2 className="mb-4 text-xl font-semibold">Categorii populare</h2>
        <div className="flex flex-wrap gap-2">
          {LISTING_CATEGORIES.map((cat) => (
            <button
              key={cat}
              type="button"
              onClick={() => navigate(`/anunturi?categorie=${encodeURIComponent(cat)}`)}
              className="rounded-full border border-border bg-white px-4 py-2 text-sm font-medium transition hover:border-brand-300 hover:bg-brand-50"
            >
              {cat}
            </button>
          ))}
        </div>
      </section>

      <section id="listings" className="container-rentix space-y-10 pb-16">
        <ListingSection
          title="Recomandate"
          icon={<TrendingUp className="text-brand-600" />}
          listings={featured}
          loading={loading}
          showAdminDelete={user?.role === 'ADMIN'}
          onDelete={handleDelete}
        />
        <ListingSection title="Cele mai noi" listings={latest} loading={loading} />
      </section>

      <Link
        to="/adauga"
        className="fixed bottom-6 right-6 z-40 flex h-14 w-14 items-center justify-center rounded-full bg-brand-600 text-white shadow-lg transition hover:scale-105 hover:bg-brand-700"
      >
        <Plus size={28} />
      </Link>
    </AppLayout>
  )
}

function ListingSection({ title, icon, listings, loading, showAdminDelete, onDelete }) {
  return (
    <div>
      <div className="mb-5 flex items-center gap-2">
        {icon}
        <h2 className="text-2xl font-bold">{title}</h2>
      </div>
      {loading ? (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="aspect-[4/5] w-full" />
          ))}
        </div>
      ) : listings.length === 0 ? (
        <p className="text-text-muted">Nu există anunțuri disponibile.</p>
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {listings.map((a) => (
            <ListingCard key={a.id} listing={a} showAdminDelete={showAdminDelete} onDelete={onDelete} />
          ))}
        </div>
      )}
    </div>
  )
}
