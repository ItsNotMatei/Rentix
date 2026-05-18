import { useEffect, useRef, useState } from 'react'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'

const NOMINATIM_HEADERS = {
  'Accept-Language': 'ro',
  'User-Agent': 'RentixApp/1.0 (contact@rentix.test)',
}

/**
 * @param {object} props
 * @param {string} props.value
 * @param {(address: string, coords?: { lat: number, lng: number } | null) => void} props.onChange
 */
export default function AddressAutocomplete({ value, onChange, placeholder = 'Oraș, stradă...', className }) {
  const [suggestions, setSuggestions] = useState([])
  const [open, setOpen] = useState(false)
  const ref = useRef(null)

  useEffect(() => {
    const onClick = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false)
    }
    document.addEventListener('mousedown', onClick)
    return () => document.removeEventListener('mousedown', onClick)
  }, [])

  useEffect(() => {
    if (!value || value.trim().length < 3) {
      setSuggestions([])
      return
    }
    const t = setTimeout(async () => {
      try {
        const q = encodeURIComponent(
          value.toLowerCase().includes('romania') || value.toLowerCase().includes('românia')
            ? value
            : `${value}, Romania`
        )
        const res = await fetch(
          `https://nominatim.openstreetmap.org/search?q=${q}&format=json&limit=5&countrycodes=ro`,
          { headers: NOMINATIM_HEADERS }
        )
        const data = await res.json()
        setSuggestions(data || [])
        setOpen(true)
      } catch {
        setSuggestions([])
      }
    }, 350)
    return () => clearTimeout(t)
  }, [value])

  return (
    <div className={cn('relative', className)}>
      <Input
        value={value}
        onChange={(e) => onChange(e.target.value, null)}
        onFocus={() => suggestions.length > 0 && setOpen(true)}
        placeholder={placeholder}
      />
      {open && suggestions.length > 0 && (
        <ul className="absolute z-20 mt-1 max-h-48 w-full overflow-auto rounded-xl border border-border bg-white shadow-lg">
          {suggestions.map((s) => (
            <li key={s.place_id}>
              <button
                type="button"
                className="w-full px-3 py-2 text-left text-sm hover:bg-brand-50"
                onClick={() => {
                  const lat = parseFloat(s.lat)
                  const lng = parseFloat(s.lon)
                  onChange(
                    s.display_name,
                    Number.isFinite(lat) && Number.isFinite(lng) ? { lat, lng } : null
                  )
                  setOpen(false)
                }}
              >
                {s.display_name}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

