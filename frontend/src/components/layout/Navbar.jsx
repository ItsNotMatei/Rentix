import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Heart, Menu, MessageCircle, Package, Search, User, X } from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import api, { getStoredUser, hasRole } from '@/services/api'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'

export default function Navbar() {
  const navigate = useNavigate()
  const searchRef = useRef(null)
  const [user, setUser] = useState(null)
  const [query, setQuery] = useState('')
  const [suggestions, setSuggestions] = useState([])
  const [showDropdown, setShowDropdown] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)

  useEffect(() => {
    setUser(getStoredUser())
  }, [])

  useEffect(() => {
    const onClick = (e) => {
      if (searchRef.current && !searchRef.current.contains(e.target)) setShowDropdown(false)
    }
    document.addEventListener('mousedown', onClick)
    return () => document.removeEventListener('mousedown', onClick)
  }, [])

  useEffect(() => {
    if (query.trim().length < 2) {
      setSuggestions([])
      setShowDropdown(false)
      return
    }
    const t = setTimeout(async () => {
      try {
        const res = await api.get(`/api/products/search?query=${encodeURIComponent(query)}`)
        setSuggestions(res.data.slice(0, 5))
        setShowDropdown(true)
      } catch {
        setSuggestions([])
      }
    }, 300)
    return () => clearTimeout(t)
  }, [query])

  const executeSearch = (term) => {
    if (!term?.trim()) return
    setShowDropdown(false)
    setMobileOpen(false)
    navigate(`/anunturi?search=${encodeURIComponent(term)}`)
  }

  return (
    <header className="glass-nav sticky top-0 z-50">
      <motion.div
        initial={{ y: -12, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        className="container-rentix flex h-16 items-center gap-3 md:gap-6"
      >
        <Link to="/" className="shrink-0 text-xl font-bold tracking-tight text-brand-700">
          Rentix
        </Link>

        <motion.div ref={searchRef} className="relative hidden flex-1 md:block" layout>
          <div className="relative">
            <Input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && executeSearch(query)}
              onFocus={() => query.trim().length >= 2 && setShowDropdown(true)}
              placeholder="Caută echipamente, unelte, haine..."
              className="pr-24"
            />
            {query && (
              <button
                type="button"
                onClick={() => setQuery('')}
                className="absolute right-14 top-1/2 -translate-y-1/2 text-text-muted"
              >
                <X size={16} />
              </button>
            )}
            <Button
              size="sm"
              className="absolute right-1.5 top-1/2 h-8 -translate-y-1/2"
              onClick={() => executeSearch(query)}
            >
              <Search size={16} />
            </Button>
          </div>
          <AnimatePresence>
            {showDropdown && (
              <motion.div
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 6 }}
                className="absolute top-[calc(100%+8px)] left-0 w-full overflow-hidden rounded-2xl border border-border bg-white shadow-xl"
              >
                {suggestions.length > 0 ? (
                  suggestions.map((s) => (
                    <button
                      key={s.id}
                      type="button"
                      onClick={() => executeSearch(s.titlu)}
                      className="flex w-full items-center gap-3 border-b border-border/60 px-4 py-3 text-left text-sm hover:bg-brand-50"
                    >
                      <Package size={16} className="text-brand-600" />
                      {s.titlu}
                    </button>
                  ))
                ) : (
                  <p className="px-4 py-3 text-sm text-text-muted">
                    Apasă Enter pentru „{query}”
                  </p>
                )}
              </motion.div>
            )}
          </AnimatePresence>
        </motion.div>

        <nav className="ml-auto hidden items-center gap-1 md:flex">
          <NavIcon to="/chat" icon={MessageCircle} label="Mesaje" />
          <NavIcon to="/profile?tab=favorite" icon={Heart} label="Favorite" />
          {hasRole('MODERATOR') && (
            <Link to="/admin" className="rounded-lg px-3 py-2 text-sm font-medium text-brand-700 hover:bg-brand-50">
              Admin
            </Link>
          )}
          {user ? (
            <Link
              to="/profile"
              className="flex items-center gap-2 rounded-xl border border-border px-3 py-2 text-sm font-medium hover:bg-white"
            >
              <User size={18} />
              {user.nume || user.email?.split('@')[0]}
            </Link>
          ) : (
            <motion.div className="flex items-center gap-2">
              <Link to="/login" className="rounded-xl px-3 py-2 text-sm font-medium hover:bg-brand-50">
                Autentificare
              </Link>
              <Button asChild size="sm">
                <Link to="/signup">Cont nou</Link>
              </Button>
            </motion.div>
          )}
        </nav>

        <button
          type="button"
          className="ml-auto rounded-xl border border-border p-2 md:hidden"
          onClick={() => setMobileOpen((v) => !v)}
        >
          {mobileOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
      </motion.div>

      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            className="border-t border-border bg-white md:hidden"
          >
            <div className="container-rentix space-y-3 py-4">
              <Input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && executeSearch(query)}
                placeholder="Caută..."
              />
              <div className="grid grid-cols-2 gap-2">
                <MobileLink to="/chat" onClick={() => setMobileOpen(false)}>Mesaje</MobileLink>
                <MobileLink to="/profile?tab=favorite" onClick={() => setMobileOpen(false)}>Favorite</MobileLink>
                <MobileLink to="/profile" onClick={() => setMobileOpen(false)}>Profil</MobileLink>
                <MobileLink to={user ? '/adauga' : '/login'} onClick={() => setMobileOpen(false)}>
                  Adaugă anunț
                </MobileLink>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </header>
  )
}

function NavIcon({ to, icon: Icon, label }) {
  return (
    <Link
      to={to}
      title={label}
      className="rounded-xl p-2.5 text-text-muted transition-colors hover:bg-brand-50 hover:text-brand-700"
    >
      <Icon size={20} />
    </Link>
  )
}

function MobileLink({ to, children, onClick }) {
  return (
    <Link
      to={to}
      onClick={onClick}
      className="rounded-xl border border-border px-3 py-2 text-center text-sm font-medium"
    >
      {children}
    </Link>
  )
}
