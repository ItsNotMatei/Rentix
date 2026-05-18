import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Calendar, Clock, MapPin, MessageSquare, Share2, ShieldCheck, Star, Tag } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import ImageGallery from '@/components/listing/ImageGallery'
import FavoriteButton from '@/components/listing/FavoriteButton'
import ProductMap from '@/components/maps/ProductMap'
import MakeOfferModal from '@/components/MakeOfferModal'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input, Textarea } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import api, { getStoredUser, hasRole } from '@/services/api'
import { notifyError } from '@/lib/errors'
import { toast } from '@/lib/toast'
import { buyNow, rentalCheckout } from '@/services/paymentService'
import BookingCalendar from '@/components/booking/BookingCalendar'
import ConfirmDialog from '@/components/ConfirmDialog'
import { conditionLabel } from '@/lib/listingMeta'

export default function ProductDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [reviews, setReviews] = useState([])
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [bookingError, setBookingError] = useState('')
  const [showOfferModal, setShowOfferModal] = useState(false)
  const [buyLoading, setBuyLoading] = useState(false)
  const [calendarRefresh, setCalendarRefresh] = useState(0)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const [canReview, setCanReview] = useState(false)
  const [reviewBlocked, setReviewBlocked] = useState('')
  const [rentLoading, setRentLoading] = useState(false)
  const canModerateDelete = hasRole('MODERATOR')

  const fetchProduct = async () => {
    const res = await api.get(`/api/products/${id}`)
    setProduct(res.data)
  }

  const fetchReviews = async () => {
    const res = await api.get(`/api/reviews/${id}`)
    setReviews(res.data)
  }

  useEffect(() => {
    setLoading(true)
    Promise.all([fetchProduct(), fetchReviews()]).finally(() => setLoading(false))
  }, [id])

  useEffect(() => {
    const user = getStoredUser()
    if (!user?.id) {
      setCanReview(false)
      setReviewBlocked('Autentifică-te pentru a lăsa o recenzie.')
      return
    }
    api.get(`/api/reviews/${id}/can-review`)
      .then((res) => {
        setCanReview(res.data.canReview === true)
        if (res.data.alreadyReviewed) setReviewBlocked('Ai lăsat deja o recenzie.')
        else if (!res.data.canReview) setReviewBlocked('Poți recenza după o rezervare sau comandă finalizată.')
        else setReviewBlocked('')
      })
      .catch(() => setCanReview(false))
  }, [id])

  const handleShare = async () => {
    try {
      await navigator.share({ title: product?.titlu, text: product?.descriere, url: window.location.href })
    } catch {
      /* cancelled */
    }
  }

  const handleReview = async () => {
    const user = getStoredUser()
    if (!user?.id) {
      navigate('/login')
      return
    }
    if (!canReview) {
      toast.error(reviewBlocked || 'Nu poți lăsa o recenzie acum.')
      return
    }
    try {
      await api.post(`/api/reviews/${id}`, { rating, comment })
      setComment('')
      setCanReview(false)
      setReviewBlocked('Ai lăsat deja o recenzie.')
      fetchReviews()
      fetchProduct()
      toast.success('Recenzia a fost publicată.')
    } catch (err) {
      notifyError(err, 'Nu s-a putut salva recenzia.')
    }
  }

  const handleBooking = async () => {
    setBookingError('')
    if (!startDate || !endDate) {
      setBookingError('Selectează perioada de închiriere.')
      return
    }
    const user = getStoredUser()
    if (!user?.id) {
      navigate('/login')
      return
    }
    if (!user?.isVerified && !user?.verified) {
      toast.info('Verifică-ți identitatea din profil înainte de a închiria.')
      navigate('/profile?tab=cont')
      return
    }
    setRentLoading(true)
    try {
      const res = await rentalCheckout(id, startDate, endDate)
      if (res.url) {
        window.location.href = res.url
        return
      }
      toast.success('Rezervare realizată cu succes!')
      setCalendarRefresh((k) => k + 1)
      setStartDate('')
      setEndDate('')
    } catch (err) {
      setBookingError(err.response?.data?.message || err.friendlyMessage || 'Perioada selectată nu este disponibilă.')
    } finally {
      setRentLoading(false)
    }
  }

  const handleDeleteListing = async () => {
    setDeleteLoading(true)
    try {
      await api.delete(`/api/admin/listings/${id}`)
      toast.success('Anunțul a fost șters.')
      navigate('/admin')
    } catch (err) {
      notifyError(err, 'Nu s-a putut șterge anunțul.')
    } finally {
      setDeleteLoading(false)
      setDeleteOpen(false)
    }
  }

  const handleCumparaAcum = async () => {
    const u = getStoredUser()
    if (!u?.isVerified && !u?.verified) {
      toast.info('Verifică-ți identitatea din profil înainte de a cumpăra.')
      navigate('/profile?tab=cont')
      return
    }
    setBuyLoading(true)
    try {
      const res = await buyNow(product.id)
      if (res.url) window.location.href = res.url
    } catch (err) {
      notifyError(err, 'Nu s-a putut iniția plata.')
    } finally {
      setBuyLoading(false)
    }
  }

  if (loading) {
    return (
      <AppLayout>
        <div className="container-rentix space-y-4 py-8">
          <Skeleton className="h-[400px] w-full" />
          <Skeleton className="h-40 w-full" />
        </div>
      </AppLayout>
    )
  }

  if (!product) {
    return (
      <AppLayout>
        <p className="container-rentix py-20 text-center text-text-muted">Produsul nu a fost găsit.</p>
      </AppLayout>
    )
  }

  const images = product.images?.length ? product.images : [product.imageUrl]
  const tipLower = (product.tip || '').toLowerCase()
  const esteVanzare = tipLower.includes('vânzare') || tipLower.includes('vanzare')

  return (
    <AppLayout>
      {showOfferModal && <MakeOfferModal product={product} onClose={() => setShowOfferModal(false)} />}
      <ConfirmDialog
        open={deleteOpen}
        onOpenChange={setDeleteOpen}
        title="Șterge anunțul"
        description="Ești sigur că vrei să ștergi acest anunț?"
        onConfirm={handleDeleteListing}
        loading={deleteLoading}
      />
      <div className="container-rentix space-y-10 py-8">
        <div className="grid gap-8 lg:grid-cols-[1.2fr_1fr]">
          <ImageGallery images={images} title={product.titlu} />
          <div className="space-y-5">
            <div className="flex items-start justify-between gap-3">
              <div>
                <div className="flex flex-wrap gap-2">
                  <Badge>{product.tip || 'Închiriere'}</Badge>
                  {product.categorie && <Badge variant="secondary">{product.categorie}</Badge>}
                  {product.stareProdus && <Badge variant="warning">{conditionLabel(product.stareProdus)}</Badge>}
                </div>
                <h1 className="mt-2 text-3xl font-bold">{product.titlu}</h1>
                <p className="mt-2 flex flex-wrap items-center gap-3 text-sm text-text-muted">
                  <span className="flex items-center gap-1"><Star size={14} className="fill-amber-400 text-amber-400" /> {product.averageRating || 0}</span>
                  <span>({product.reviewCount || 0} recenzii)</span>
                  <span className="flex items-center gap-1"><MapPin size={14} /> {product.adresa}</span>
                </p>
              </div>
              <div className="flex gap-2">
                <FavoriteButton productId={product.id} />
                <Button variant="secondary" size="icon" onClick={handleShare}><Share2 size={18} /></Button>
                {canModerateDelete && (
                  <Button variant="secondary" size="sm" className="text-red-600" onClick={() => setDeleteOpen(true)}>
                    Șterge
                  </Button>
                )}
              </div>
            </div>

            <Card>
              <CardHeader><CardTitle className="text-base">Descriere</CardTitle></CardHeader>
              <CardContent className="text-sm text-text-muted">{product.descriere || 'Fără descriere.'}</CardContent>
            </Card>

            <Card className="sticky top-20">
              <CardContent className="space-y-4 pt-6">
                <p className="text-3xl font-bold text-brand-700">
                  {product.pret} RON{!esteVanzare && <span className="text-base font-normal text-text-muted"> / zi</span>}
                </p>
                {esteVanzare ? (
                  <div className="space-y-2">
                    <p className="flex items-center gap-2 text-sm text-text-muted"><Tag size={16} /> Stare: Foarte bună</p>
                    <Button className="w-full" onClick={handleCumparaAcum} disabled={buyLoading}>
                      {buyLoading ? 'Se redirecționează...' : 'Cumpără acum'}
                    </Button>
                    <Button variant="secondary" className="w-full" onClick={() => setShowOfferModal(true)}>Fă o ofertă</Button>
                  </div>
                ) : (
                  <div className="space-y-3">
                    <div className="grid grid-cols-2 gap-2">
                      <label className="text-xs font-medium">De la<Input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} /></label>
                      <label className="text-xs font-medium">Până la<Input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} /></label>
                    </div>
                    {bookingError && <p className="text-sm text-red-600">{bookingError}</p>}
                    <p className="flex items-center gap-1 text-xs text-text-muted"><Clock size={14} /> Predare 09:00–18:00</p>
                    <Button className="w-full" onClick={handleBooking} disabled={rentLoading}>
                      {rentLoading ? 'Se redirecționează la plată...' : 'Închiriază acum'}
                    </Button>
                  </div>
                )}
                <p className="flex items-center gap-2 text-xs text-emerald-700"><ShieldCheck size={16} /> Protecție Rentix inclusă</p>
                <Button variant="outline" className="w-full" onClick={() => navigate(`/chat?cuUtilizator=${product.userId}&produs=${product.id}`)}>
                  <MessageSquare size={16} /> Întreabă proprietarul
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>

        {!esteVanzare && (
          <section>
            <div className="mb-3 flex items-center justify-between">
              <h2 className="text-xl font-semibold">Disponibilitate</h2>
              <Button variant="secondary" size="sm" asChild>
                <Link to={`/calendar?productId=${id}`}>Calendar complet</Link>
              </Button>
            </div>
            <Card className="border-blue-100 p-4">
              <BookingCalendar productId={id} refreshKey={calendarRefresh} />
            </Card>
          </section>
        )}

        <section>
          <h2 className="mb-3 text-xl font-semibold">Locație</h2>
          <ProductMap address={product.adresa} latitude={product.latitude} longitude={product.longitude} />
        </section>

        <section>
          <h2 className="mb-4 text-xl font-semibold">Recenzii</h2>
          <Card className="mb-4">
            <CardContent className="grid gap-3 pt-6 sm:grid-cols-[100px_1fr_auto]">
              {canReview ? (
                <>
                  <select value={rating} onChange={(e) => setRating(Number(e.target.value))} className="rounded-xl border border-border px-3 py-2">
                    {[5, 4, 3, 2, 1].map((n) => <option key={n} value={n}>{n} ★</option>)}
                  </select>
                  <Textarea placeholder="Scrie o recenzie..." value={comment} onChange={(e) => setComment(e.target.value)} />
                  <Button onClick={handleReview}>Trimite</Button>
                </>
              ) : (
                <p className="col-span-full text-sm text-text-muted sm:col-span-3">{reviewBlocked || 'Recenziile sunt disponibile după o tranzacție.'}</p>
              )}
            </CardContent>
          </Card>
          <div className="space-y-3">
            {reviews.map((r) => (
              <Card key={r.id}>
                <CardContent className="pt-5">
                  <div className="flex justify-between text-sm font-semibold">
                    <span>{r.userName || r.user?.nume || 'Utilizator'}</span>
                    <span>{r.rating} ★</span>
                  </div>
                  <p className="mt-2 text-sm text-text-muted">{r.comment}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </section>
      </div>
    </AppLayout>
  )
}
