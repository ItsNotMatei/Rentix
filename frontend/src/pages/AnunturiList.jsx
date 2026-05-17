import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { ShoppingBag } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import ListingCard from '@/components/listing/ListingCard'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import api from '@/services/api'

export default function AnunturiList() {
  const [searchParams] = useSearchParams()
  const searchTerm = searchParams.get('search') || ''
  const [anunturi, setAnunturi] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    const url = searchTerm.trim()
      ? `/api/products/search?query=${encodeURIComponent(searchTerm)}`
      : '/api/products'
    api.get(url)
      .then((res) => setAnunturi(res.data))
      .catch(() => setAnunturi([]))
      .finally(() => setLoading(false))
  }, [searchTerm])

  return (
    <AppLayout>
      <div className="container-rentix py-10">
        <header className="mb-8 border-b border-border pb-4">
          {searchTerm ? (
            <>
              <h1 className="text-2xl font-bold">Rezultate pentru „{searchTerm}”</h1>
              <p className="text-sm text-text-muted">{anunturi.length} rezultate</p>
            </>
          ) : (
            <h1 className="text-2xl font-bold">Toate anunțurile</h1>
          )}
        </header>

        {loading && (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {Array.from({ length: 8 }).map((_, i) => <Skeleton key={i} className="aspect-[4/5]" />)}
          </div>
        )}

        {!loading && anunturi.length === 0 && (
          <div className="rounded-2xl border border-dashed border-border bg-white p-12 text-center">
            <ShoppingBag className="mx-auto text-text-muted" size={48} />
            <h3 className="mt-4 font-semibold">Nu am găsit rezultate</h3>
            <p className="mt-2 text-sm text-text-muted">Încearcă alți termeni de căutare.</p>
            <Button className="mt-4" onClick={() => window.location.href = '/anunturi'}>Vezi toate</Button>
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
