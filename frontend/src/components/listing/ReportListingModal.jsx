import { useState } from 'react'
import { Flag, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/input'
import { submitListingReport } from '@/services/reportService'
import { toast } from '@/lib/toast'
import { notifyError } from '@/lib/errors'

export default function ReportListingModal({ listing, open, onClose }) {
  const [reason, setReason] = useState('')
  const [loading, setLoading] = useState(false)

  if (!open || !listing) return null

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!reason.trim()) {
      toast.info('Descrie motivul raportului.')
      return
    }
    setLoading(true)
    try {
      const res = await submitListingReport(listing.id, reason.trim())
      toast.success(res.message || 'Raport trimis.')
      setReason('')
      onClose()
    } catch (err) {
      const msg = err.response?.data?.message
      notifyError(err, typeof msg === 'string' ? msg : 'Nu s-a putut trimite raportul.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm" onClick={onClose}>
      <div
        className="w-full max-w-lg overflow-hidden rounded-2xl border border-brand-100 bg-white shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between border-b border-brand-50 bg-gradient-to-r from-brand-50 to-white px-6 py-4">
          <div className="flex items-center gap-2 text-brand-800">
            <Flag size={20} />
            <h2 className="text-lg font-bold">Raportează anunțul</h2>
          </div>
          <button type="button" onClick={onClose} className="rounded-full p-1 text-slate-500 hover:bg-slate-100" aria-label="Închide">
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 px-6 py-5">
          <p className="text-sm text-slate-600">
            Raportezi: <span className="font-semibold text-brand-800">{listing.titlu}</span>
          </p>
          <label className="block text-sm font-medium text-slate-700">
            Motivul raportului
            <Textarea
              className="mt-2 min-h-[140px] resize-y border-brand-100 focus:border-brand-500"
              placeholder="Descrie problema (conținut înșelător, produs interzis, comportament abuziv...)"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              maxLength={2000}
              required
            />
          </label>
          <p className="text-xs text-slate-500">{reason.length}/2000</p>
          <div className="flex flex-wrap justify-end gap-2 pt-2">
            <Button type="button" variant="secondary" onClick={onClose} disabled={loading}>
              Anulează
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? 'Se trimite...' : 'Trimite raportul'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
