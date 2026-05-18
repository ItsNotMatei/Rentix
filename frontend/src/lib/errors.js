import { toast } from '@/lib/toast'

export function getErrorMessage(error, fallback = 'A apărut o eroare. Încearcă din nou.') {
  const data = error?.response?.data
  if (typeof data === 'string' && data.length < 200) return data
  if (data?.message) return data.message
  if (data?.error && typeof data.error === 'string') return data.error
  return fallback
}

export function notifyError(error, fallback) {
  toast.error(getErrorMessage(error, fallback))
}
