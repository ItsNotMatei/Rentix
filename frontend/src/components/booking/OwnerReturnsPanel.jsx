import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { PackageCheck } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Textarea } from '@/components/ui/input'
import { confirmReturn, getPendingReturns } from '@/services/reservationService'
import { toast } from '@/lib/toast'
import { notifyError } from '@/lib/errors'

const PRESETS = [
  'Primit în stare perfectă',
  'Uzură minoră, funcțional',
  'Obiect deteriorat la returnare',
]

export default function OwnerReturnsPanel() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [conditions, setConditions] = useState({})
  const [submittingId, setSubmittingId] = useState(null)

  const load = () => {
    setLoading(true)
    getPendingReturns()
      .then(setItems)
      .catch(() => setItems([]))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const handleConfirm = async (id) => {
    const condition = (conditions[id] || '').trim()
    if (!condition) {
      toast.info('Selectează sau descrie starea obiectului returnat.')
      return
    }
    setSubmittingId(id)
    try {
      await confirmReturn(id, condition)
      toast.success('Returnare confirmată.')
      load()
    } catch (err) {
      notifyError(err, err.response?.data?.message || 'Confirmarea a eșuat.')
    } finally {
      setSubmittingId(null)
    }
  }

  if (loading) {
    return <p className="text-text-muted">Se încarcă închirierile finalizate...</p>
  }

  if (items.length === 0) {
    return (
      <p className="rounded-xl border border-dashed border-border bg-slate-50 px-4 py-8 text-center text-text-muted">
        Nu ai închirieri care așteaptă confirmarea returnării.
      </p>
    )
  }

  return (
    <div className="space-y-4">
      <p className="text-sm text-text-muted">
        După încheierea perioadei, confirmă că ai primit obiectul înapoi. Anunțul devine din nou disponibil dacă totul este în regulă.
      </p>
      {items.map((item) => (
        <Card key={item.id} className="border-brand-100">
          <CardHeader className="pb-2">
            <CardTitle className="flex items-center gap-2 text-base">
              <PackageCheck className="text-brand-600" size={20} />
              {item.listingTitle}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <p className="text-sm text-text-muted">
              Chiriaș: <strong>{item.renterName}</strong> · {item.startDate} → {item.endDate}
            </p>
            <p className="font-medium text-brand-800">Ai primit obiectul înapoi?</p>
            <div className="flex flex-wrap gap-2">
              {PRESETS.map((preset) => (
                <button
                  key={preset}
                  type="button"
                  className={`rounded-full border px-3 py-1 text-xs transition ${
                    conditions[item.id] === preset
                      ? 'border-brand-600 bg-brand-50 text-brand-800'
                      : 'border-border bg-white hover:border-brand-300'
                  }`}
                  onClick={() => setConditions((c) => ({ ...c, [item.id]: preset }))}
                >
                  {preset}
                </button>
              ))}
            </div>
            <Textarea
              placeholder="Sau descrie starea obiectului..."
              value={conditions[item.id] || ''}
              onChange={(e) => setConditions((c) => ({ ...c, [item.id]: e.target.value }))}
              className="min-h-[80px]"
            />
            <div className="flex flex-wrap gap-2">
              <Button onClick={() => handleConfirm(item.id)} disabled={submittingId === item.id}>
                {submittingId === item.id ? 'Se salvează...' : 'Confirmă returnarea'}
              </Button>
              <Link
                to={`/product/${item.anuntId}`}
                className="inline-flex items-center justify-center rounded-xl border border-border px-4 py-2 text-sm font-medium hover:bg-slate-50"
              >
                Vezi anunțul
              </Link>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
