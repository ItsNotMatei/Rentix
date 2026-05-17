import { useState } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { CalendarDays } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import BookingCalendar from '@/components/booking/BookingCalendar'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

export default function CalendarPage() {
  const [searchParams] = useSearchParams()
  const initialId = searchParams.get('productId') || searchParams.get('anuntId') || ''
  const [productId, setProductId] = useState(initialId)
  const [inputId, setInputId] = useState(initialId)

  const applyId = () => setProductId(inputId.trim())

  return (
    <AppLayout>
      <div className="container-rentix py-10">
        <div className="mb-8">
          <Link to="/" className="text-sm font-medium text-[#0070f3] hover:underline">← Înapoi la Rentix</Link>
          <h1 className="mt-2 flex items-center gap-2 text-3xl font-bold text-slate-900">
            <CalendarDays className="text-[#0070f3]" />
            Calendar disponibilitate
          </h1>
          <p className="mt-1 text-text-muted">Vizualizează zilele rezervate pentru orice anunț.</p>
        </div>

        <Card className="border-blue-100 shadow-sm">
          <CardHeader className="border-b border-blue-50 bg-gradient-to-r from-blue-50 to-white">
            <CardTitle className="text-[#1e40af]">Selectează anunțul</CardTitle>
          </CardHeader>
          <CardContent className="space-y-6 pt-6">
            <div className="flex flex-wrap gap-2">
              <Input
                type="number"
                placeholder="ID anunț"
                value={inputId}
                onChange={(e) => setInputId(e.target.value)}
                className="max-w-xs border-blue-200 focus-visible:ring-[#0070f3]"
              />
              <Button onClick={applyId} className="bg-[#0070f3] hover:bg-[#1e40af]">
                Încarcă calendar
              </Button>
              {productId && (
                <Button variant="secondary" asChild>
                  <Link to={`/product/${productId}`}>Vezi anunțul</Link>
                </Button>
              )}
            </div>
            <BookingCalendar productId={productId} />
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}
