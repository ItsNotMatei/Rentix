import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { ShoppingBag } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import ListingCard from '@/components/listing/ListingCard'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import api from '@/services/api'
import { LISTING_CATEGORIES } from '@/lib/listingMeta'

export default function AnunturiList() {
  const [searchParams] = useSearchParams()
  const searchTerm = searchParams.get('search') || ''
  const categorie = searchParams.get('categorie') || ''
  const [anunturi, setAnunturi] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    let url = '/api/products'
    if (categorie.trim()) {
      url = `/api/products/by-category?categorie=${encodeURIComponent(categorie)}`
    } else if (searchTerm.trim()) {
      url = `/api/products/search?query=${encodeURIComponent(searchTerm)}`
    }
    api.get(url)
      .then((res) => setAnunturi(res.data))
      .catch(() => setAnunturi([]))
      .finally(() => setLoading(false))
  }, [searchTerm, categorie])

  const title = categorie
    ? `Categorie: ${categorie}`
    : searchTerm
      ? `Rezultate pentru „${searchTerm}”`
      : 'Toate anunțurile'

  return (
    <AppLayout>
      <div className="container-rentix py-10">
        <header className="mb-6 border-b border-border pb-4">
          <h1 className="text-2xl font-bold">{title}</h1>
          {!loading && <p className="text-sm text-text-muted">{anunturi.length} anunțuri</p>}
          <div className="mt-4 flex flex-wrap gap-2">
            {LISTING_CATEGORIES.map((cat) => (
              <Link
                key={cat}
                to={`/anunturi?categorie=${encodeURIComponent(cat)}`}
                className={`rounded-full border px-3 py-1 text-sm transition ${
                  categorie === cat ? 'border-brand-600 bg-brand-600 text-white' : 'border-border bg-white hover:bg-brand-50'
                }`}
              >
                {cat}
              </Link>
            ))}
          </div>
        </header>

        {loading && (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {Array.from({ length: 8 }).map((_, i) => <Skeleton key={i} className="aspect-[4/5]" />)}
          </div>
        )}

        {!loading && anunturi.length === 0 && (
          <div className="rounded-2xl border border-dashed border-border bg-white p-12 text-center">
            <ShoppingBag className="mx-auto text-text-muted" size={48} />
            <h3 className="mt-4 font-semibold">Nu am găsit anunțuri</h3>
            <p className="mt-2 text-sm text-text-muted">Încearcă altă categorie sau publică primul anunț.</p>
            <Link to="/anunturi" className="mt-4 inline-flex"><Button>Vezi toate</Button></Link>
          </div>
        )}

        {!loading && anunturi.length > 0 && (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {anunturi.map((a) => <ListingCard key={a.id} listing={a} />)}
          </div>
        )}
      </div>
    </AppLayout>
  )
}
