import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { CheckCircle2, Home, Package } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import api from '@/services/api'

export default function CheckoutSuccess() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const orderId = searchParams.get('orderId')
  const type = searchParams.get('type')
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(!!orderId)

  useEffect(() => {
    if (!orderId) return
    let attempts = 0
    const poll = () => {
      api.get('/api/payments/orders')
        .then((res) => {
          const found = res.data.find((o) => String(o.id) === String(orderId))
          if (found) setOrder(found)
          if (found?.escrowStatus === 'ESCROW_ACTIVE' || found?.escrowStatus === 'COMPLETED' || attempts >= 8) {
            setLoading(false)
            return
          }
          attempts += 1
          setTimeout(poll, 1500)
        })
        .catch(() => setLoading(false))
    }
    poll()
  }, [orderId])

  const isRental = type === 'rental' || order?.rental

  return (
    <AppLayout hideFooter>
      <div className="container-rentix flex min-h-[70vh] items-center justify-center py-12">
        <Card className="w-full max-w-lg border-emerald-100 shadow-lg">
          <CardContent className="flex flex-col items-center px-8 py-12 text-center">
            <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-full bg-emerald-100">
              <CheckCircle2 className="h-12 w-12 text-emerald-600" strokeWidth={2} />
            </div>
            <h1 className="text-3xl font-bold text-slate-900">Plată cu succes</h1>
            <p className="mt-3 max-w-sm text-base text-slate-600">
              {isRental
                ? 'Rezervarea ta a fost confirmată. Proprietarul a fost notificat.'
                : 'Banii sunt în escrow până confirmi primirea produsului.'}
            </p>
            {loading && <p className="mt-4 text-sm text-slate-500">Se actualizează statusul comenzii…</p>}
            {order && (
              <p className="mt-4 rounded-full bg-slate-100 px-4 py-1.5 text-sm font-medium text-slate-700">
                Comandă #{order.id} · {order.escrowStatus}
              </p>
            )}
            <div className="mt-8 flex w-full flex-col gap-3 sm:flex-row sm:justify-center">
              <Button className="w-full sm:w-auto" size="lg" onClick={() => navigate('/')}>
                <Home size={18} />
                Mergi înapoi acasă
              </Button>
              <Button variant="secondary" className="w-full sm:w-auto" size="lg" asChild>
                <Link to="/profile?tab=comenzi">
                  <Package size={18} />
                  Vezi comenzile mele
                </Link>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}
