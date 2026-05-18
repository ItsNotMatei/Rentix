import { useCallback, useEffect, useState } from 'react'
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import api from '@/services/api'

/**
 * Calendar cu sloturi rezervate (albastru) pentru un anunț.
 * @param {number|string} productId
 * @param {() => void} [onAvailabilityChange] - apelat după refresh extern
 */
export default function BookingCalendar({ productId, refreshKey = 0, className = '' }) {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(false)

  const loadAvailability = useCallback(async () => {
    if (!productId) return
    setLoading(true)
    try {
      const res = await api.get(`/reservations/availability/${productId}`)
      const dates = res.data || []
      setEvents(
        dates.map((date) => ({
          title: 'Rezervat',
          start: date,
          allDay: true,
          display: 'background',
          backgroundColor: '#93c5fd',
          borderColor: '#2563eb',
        }))
      )
    } catch {
      setEvents([])
    } finally {
      setLoading(false)
    }
  }, [productId])

  useEffect(() => {
    loadAvailability()
  }, [loadAvailability, refreshKey])

  if (!productId) {
    return (
      <p className="rounded-xl border border-dashed border-blue-200 bg-blue-50/50 px-4 py-8 text-center text-sm text-blue-800">
        Selectează un anunț pentru a vedea calendarul.
      </p>
    )
  }

  return (
    <div className={`rentix-calendar ${className}`}>
      {loading && <p className="mb-2 text-xs text-blue-600">Se încarcă disponibilitatea...</p>}
      <FullCalendar
        plugins={[dayGridPlugin]}
        initialView="dayGridMonth"
        locale="ro"
        height="auto"
        headerToolbar={{
          left: 'prev,next today',
          center: 'title',
          right: '',
        }}
        events={events}
        dayMaxEvents
      />
      <p className="mt-2 flex items-center gap-2 text-xs text-slate-600">
        <span className="inline-block h-3 w-3 rounded bg-blue-400" />
        Zile deja rezervate
      </p>
    </div>
  )
}
