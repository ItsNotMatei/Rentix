import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { BarChart3, MessageSquare, Package, Users } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import ConfirmDialog from '@/components/ConfirmDialog'
import api, { getStoredUser, hasRole } from '@/services/api'
import authService from '@/services/authService'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { toast } from '@/lib/toast'
import { notifyError } from '@/lib/errors'

const TABS = [
  { id: 'overview', label: 'Statistici' },
  { id: 'users', label: 'Utilizatori', min: 'ADMIN' },
  { id: 'listings', label: 'Anunțuri' },
  { id: 'reviews', label: 'Recenzii' },
  { id: 'reports', label: 'Rapoarte' },
  { id: 'bookings', label: 'Rezervări' },
  { id: 'chat', label: 'Chat' },
]

export default function AdminDashboard() {
  const navigate = useNavigate()
  const user = getStoredUser()
  const [tab, setTab] = useState('overview')
  const [stats, setStats] = useState(null)
  const [analytics, setAnalytics] = useState(null)
  const [users, setUsers] = useState([])
  const [listings, setListings] = useState({ content: [], totalPages: 0 })
  const [reviews, setReviews] = useState({ content: [], totalPages: 0 })
  const [reports, setReports] = useState([])
  const [bookings, setBookings] = useState([])
  const [conversations, setConversations] = useState([])
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [confirm, setConfirm] = useState({ open: false, type: null, id: null })
  const [deleteLoading, setDeleteLoading] = useState(false)

  const canDelete = hasRole('MODERATOR')
  const visibleTabs = TABS.filter((t) => !t.min || hasRole(t.min))

  useEffect(() => {
    setPage(0)
  }, [tab, search])

  useEffect(() => {
    loadTab()
  }, [tab, page, search])

  const loadTab = async () => {
    setLoading(true)
    setMessage('')
    try {
      if (tab === 'overview') {
        const [s, a] = await Promise.all([api.get('/api/admin/stats'), api.get('/api/admin/analytics')])
        setStats(s.data)
        setAnalytics(a.data)
      } else if (tab === 'users') {
        const res = await api.get(`/api/admin/users?q=${encodeURIComponent(search)}&page=${page}&size=15`)
        setUsers(res.data.content || [])
      } else if (tab === 'listings') {
        const res = await api.get(`/api/admin/listings?q=${encodeURIComponent(search)}&page=${page}&size=15`)
        setListings(res.data)
      } else if (tab === 'reviews') {
        const res = await api.get(`/api/admin/reviews?page=${page}&size=15`)
        setReviews(res.data)
      } else if (tab === 'reports') {
        const res = await api.get('/api/admin/reports')
        setReports(res.data)
      } else if (tab === 'bookings') {
        const res = await api.get('/api/admin/bookings')
        setBookings(res.data)
      } else if (tab === 'chat') {
        const res = await api.get('/api/admin/conversations')
        setConversations(res.data)
      }
    } catch (err) {
      setMessage(err.friendlyMessage || 'Eroare la încărcare.')
    } finally {
      setLoading(false)
    }
  }

  const askDeleteListing = (id) => setConfirm({ open: true, type: 'listing', id })
  const askDeleteReview = (id) => setConfirm({ open: true, type: 'review', id })

  const handleConfirmDelete = async () => {
    if (!confirm.id) return
    setDeleteLoading(true)
    try {
      if (confirm.type === 'listing') {
        await api.delete(`/api/admin/listings/${confirm.id}`)
        toast.success('Anunțul a fost șters.')
      } else if (confirm.type === 'review') {
        await api.delete(`/api/admin/reviews/${confirm.id}`)
        toast.success('Recenzia a fost ștearsă.')
      }
      loadTab()
    } catch (err) {
      notifyError(err, 'Ștergerea a eșuat.')
    } finally {
      setDeleteLoading(false)
      setConfirm({ open: false, type: null, id: null })
    }
  }

  const statCards = stats
    ? [
        { label: 'Utilizatori', value: stats.users, icon: Users },
        { label: 'Anunțuri', value: stats.listings, icon: Package },
        { label: 'Recenzii', value: stats.reviews, icon: BarChart3 },
        { label: 'Conversații', value: stats.conversations, icon: MessageSquare },
      ]
    : []

  return (
    <AppLayout hideFooter>
      <ConfirmDialog
        open={confirm.open}
        onOpenChange={(open) => !open && setConfirm({ open: false, type: null, id: null })}
        title="Șterge"
        description={confirm.type === 'review' ? 'Ești sigur că vrei să ștergi această recenzie?' : 'Ești sigur că vrei să ștergi acest anunț?'}
        onConfirm={handleConfirmDelete}
        loading={deleteLoading}
      />
      <div className="container-rentix py-8">
        <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
          <div>
            <Link to="/" className="text-sm text-brand-700 hover:underline">← Rentix</Link>
            <h1 className="text-2xl font-bold">Admin Panel</h1>
            <span className="text-sm text-text-muted">{user?.role}</span>
          </div>
          <Button variant="secondary" onClick={() => authService.logout().then(() => navigate('/login'))}>
            Logout
          </Button>
        </div>

        <div className="mb-4 flex flex-wrap gap-2">
          {visibleTabs.map((t) => (
            <button
              key={t.id}
              type="button"
              onClick={() => setTab(t.id)}
              className={`rounded-xl px-4 py-2 text-sm font-medium ${tab === t.id ? 'bg-brand-600 text-white' : 'bg-white border border-border'}`}
            >
              {t.label}
            </button>
          ))}
        </div>

        {(tab === 'users' || tab === 'listings') && (
          <Input className="mb-4 max-w-md" placeholder="Caută..." value={search} onChange={(e) => setSearch(e.target.value)} />
        )}

        {message && <p className="mb-4 text-sm text-red-600">{message}</p>}
        {loading && <p className="text-text-muted">Se încarcă...</p>}

        {tab === 'overview' && stats && (
          <div className="space-y-6">
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              {statCards.map(({ label, value, icon: Icon }) => (
                <Card key={label}>
                  <CardContent className="flex items-center gap-4 pt-6">
                    <Icon className="text-brand-600" size={28} />
                    <div>
                      <p className="text-2xl font-bold">{value}</p>
                      <p className="text-sm text-text-muted">{label}</p>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
            {analytics && (
              <Card>
                <CardHeader><CardTitle>Analytics</CardTitle></CardHeader>
                <CardContent className="grid gap-2 text-sm sm:grid-cols-3">
                  <p>Utilizatori verificați: <strong>{analytics.verifiedUsers}</strong></p>
                  <p>Conturi PRO: <strong>{analytics.proUsers}</strong></p>
                  <p>Rapoarte deschise: <strong>{analytics.openReports}</strong></p>
                </CardContent>
              </Card>
            )}
          </div>
        )}

        {tab === 'users' && (
          <AdminTable headers={['ID', 'Nume', 'Email', 'Rol']} rows={users.map((u) => [u.id, u.nume, u.email, u.role])} />
        )}

        {tab === 'listings' && (
          <>
            <AdminTable
              headers={['ID', 'Titlu', 'Preț', '']}
              rows={(listings.content || []).map((l) => [
                l.id,
                <Link key={`t-${l.id}`} to={`/product/${l.id}`} className="text-brand-700 hover:underline">{l.titlu}</Link>,
                l.pret,
                canDelete ? (
                  <button key={l.id} type="button" className="text-red-600 font-medium" onClick={() => askDeleteListing(l.id)}>
                    Șterge
                  </button>
                ) : '—',
              ])}
            />
            <Pager page={page} totalPages={listings.totalPages} onPage={setPage} />
          </>
        )}

        {tab === 'reviews' && (
          <>
            <AdminTable
              headers={['ID', 'Rating', 'Comentariu', '']}
              rows={(reviews.content || []).map((r) => [
                r.id,
                `${r.rating}★`,
                r.comment,
                canDelete ? (
                  <button key={r.id} type="button" className="text-red-600 font-medium" onClick={() => askDeleteReview(r.id)}>
                    Șterge
                  </button>
                ) : '—',
              ])}
            />
            <Pager page={page} totalPages={reviews.totalPages} onPage={setPage} />
          </>
        )}

        {tab === 'reports' && (
          <AdminTable
            headers={['ID', 'Motiv', 'Status', 'Anunț']}
            rows={(reports || []).map((r) => [r.id, r.reason || r.motiv || '—', r.status || '—', r.listingId || r.productId || '—'])}
          />
        )}

        {tab === 'bookings' && (
          <AdminTable
            headers={['ID', 'Anunț', 'Utilizator', 'Perioadă', 'Status']}
            rows={(bookings || []).map((b) => [
              b.id,
              b.anuntId || b.listingId || '—',
              b.userId || b.userName || '—',
              b.startDate && b.endDate ? `${b.startDate} → ${b.endDate}` : '—',
              b.status || '—',
            ])}
          />
        )}

        {tab === 'chat' && (
          <AdminTable
            headers={['Conversație', 'Participanți', 'Ultim mesaj']}
            rows={conversations.map((c) => [
              `#${c.id}`,
              `${c.participantOneName} ↔ ${c.participantTwoName}`,
              c.lastMessage || '—',
            ])}
          />
        )}
      </div>
    </AppLayout>
  )
}

function AdminTable({ headers, rows }) {
  return (
    <div className="overflow-x-auto rounded-2xl border border-border bg-white">
      <table className="w-full text-left text-sm">
        <thead className="border-b bg-slate-50">
          <tr>{headers.map((h) => <th key={h} className="px-4 py-3 font-semibold">{h}</th>)}</tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr><td colSpan={headers.length} className="px-4 py-8 text-center text-text-muted">Niciun rezultat.</td></tr>
          ) : (
            rows.map((row, i) => (
              <tr key={i} className="border-b last:border-0">
                {row.map((cell, j) => <td key={j} className="px-4 py-3">{cell}</td>)}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  )
}

function Pager({ page, totalPages, onPage }) {
  if (!totalPages || totalPages <= 1) return null
  return (
    <div className="mt-4 flex gap-2">
      <Button variant="secondary" size="sm" disabled={page <= 0} onClick={() => onPage(page - 1)}>Înapoi</Button>
      <span className="flex items-center text-sm text-text-muted">Pagina {page + 1} / {totalPages}</span>
      <Button variant="secondary" size="sm" disabled={page >= totalPages - 1} onClick={() => onPage(page + 1)}>Înainte</Button>
    </div>
  )
}
