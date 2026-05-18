import { useEffect } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { XCircle, Home } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import api from '@/services/api'

export default function CheckoutCancel() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const orderId = searchParams.get('orderId')

  useEffect(() => {
    if (!orderId) return
    api.post(`/api/payments/orders/${orderId}/cancel-checkout`).catch(() => {})
  }, [orderId])

  return (
    <AppLayout hideFooter>
      <div className="container-rentix flex min-h-[70vh] items-center justify-center py-12">
        <Card className="w-full max-w-lg">
          <CardContent className="flex flex-col items-center px-8 py-12 text-center">
            <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-full bg-slate-100">
              <XCircle className="h-12 w-12 text-slate-500" />
            </div>
            <h1 className="text-2xl font-bold">Plată anulată</h1>
            <p className="mt-3 text-slate-600">
              Nu ți-a fost retrasă suma. Poți încerca din nou când ești gata.
            </p>
            <Button className="mt-8" size="lg" onClick={() => navigate('/')}>
              <Home size={18} />
              Mergi înapoi acasă
            </Button>
            {orderId && (
              <Link to={`/product/${searchParams.get('listingId') || ''}`} className="mt-3 text-sm text-brand-700 hover:underline">
                Înapoi la produs
              </Link>
            )}
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}
