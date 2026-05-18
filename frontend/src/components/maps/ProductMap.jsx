import { useEffect, useState } from 'react'
import { MapContainer, Marker, Popup, TileLayer, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

/** Centrul României — folosit doar dacă nu există coordonate și geocodarea eșuează */
const DEFAULT = { lat: 45.9432, lng: 24.9668 }

const icon = new L.Icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
})

function Recenter({ center }) {
  const map = useMap()
  useEffect(() => {
    map.setView(center, map.getZoom())
  }, [center, map])
  return null
}

function hasValidCoords(lat, lng) {
  return lat != null && lng != null && !Number.isNaN(Number(lat)) && !Number.isNaN(Number(lng))
}

function normalizeQuery(address) {
  const a = (address || '').trim()
  const lower = a.toLowerCase()
  if (!lower.includes('romania') && !lower.includes('românia')) {
    return `${a}, Romania`
  }
  return a
}

export default function ProductMap({ address, latitude, longitude }) {
  const [center, setCenter] = useState(() => {
    if (hasValidCoords(latitude, longitude)) {
      return { lat: Number(latitude), lng: Number(longitude) }
    }
    return DEFAULT
  })

  useEffect(() => {
    if (hasValidCoords(latitude, longitude)) {
      const lat = Number(latitude)
      const lng = Number(longitude)
      setCenter((prev) => (prev.lat === lat && prev.lng === lng ? prev : { lat, lng }))
      return
    }

    if (!address?.trim()) return
    const q = encodeURIComponent(normalizeQuery(address))

    fetch(`https://nominatim.openstreetmap.org/search?q=${q}&format=json&limit=1&countrycodes=ro`, {
      headers: {
        'Accept-Language': 'ro',
        'User-Agent': 'RentixApp/1.0 (contact@rentix.test)',
      },
    })
      .then((r) => r.json())
      .then((data) => {
        if (data?.[0]) {
          const newLat = parseFloat(data[0].lat)
          const newLng = parseFloat(data[0].lon)
          setCenter((prev) =>
            prev.lat === newLat && prev.lng === newLng ? prev : { lat: newLat, lng: newLng }
          )
        }
      })
      .catch(() => {})
  }, [address, latitude, longitude])

  return (
    <MapContainer center={center} zoom={13} className="h-[280px] w-full rounded-2xl z-0" scrollWheelZoom={false}>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <Recenter center={center} />
      <Marker position={center} icon={icon}>
        <Popup>{address || 'Locație'}</Popup>
      </Marker>
    </MapContainer>
  )
}
