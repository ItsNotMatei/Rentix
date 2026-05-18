import { useEffect, useState } from 'react'
import { CheckCircle2, Info, X, XCircle } from 'lucide-react'
import { subscribeToast } from '@/lib/toast'
import { cn } from '@/lib/utils'

const icons = {
  success: CheckCircle2,
  error: XCircle,
  info: Info,
}

const styles = {
  success: 'border-emerald-200 bg-emerald-50 text-emerald-900',
  error: 'border-red-200 bg-red-50 text-red-900',
  info: 'border-blue-200 bg-blue-50 text-blue-900',
}

export default function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  useEffect(() => {
    return subscribeToast((toast) => {
      setToasts((prev) => [...prev, toast])
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== toast.id))
      }, toast.duration)
    })
  }, [])

  const dismiss = (id) => setToasts((prev) => prev.filter((t) => t.id !== id))

  return (
    <>
      {children}
      <div className="pointer-events-none fixed bottom-4 right-4 z-[9999] flex flex-col gap-2">
        {toasts.map((t) => {
          const Icon = icons[t.type] || Info
          return (
            <div
              key={t.id}
              className={cn(
                'pointer-events-auto flex max-w-sm items-start gap-3 rounded-xl border px-4 py-3 text-sm shadow-lg',
                styles[t.type]
              )}
              role="status"
            >
              <Icon size={18} className="mt-0.5 shrink-0" />
              <p className="flex-1 font-medium">{t.message}</p>
              <button type="button" onClick={() => dismiss(t.id)} className="opacity-60 hover:opacity-100" aria-label="Închide">
                <X size={16} />
              </button>
            </div>
          )
        })}
      </div>
    </>
  )
}
