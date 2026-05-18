import { useEffect, useState } from 'react'
import { MapContainer, Marker, Popup, TileLayer, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const DEFAULT = { lat: 45.657974, lng: 25.601198 }

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

export default function ProductMap({ address, latitude, longitude }) {
  const [center, setCenter] = useState(() => {
    if (latitude && longitude) return { lat: latitude, lng: longitude }
    return DEFAULT
  })

  useEffect(() => {
    if (latitude && longitude) {
      // ✅ VERIFICARE: Actualizăm DOAR dacă numerele s-au schimbat cu adevărat
      setCenter((prev) => {
        if (prev.lat === latitude && prev.lng === longitude) return prev;
        return { lat: latitude, lng: longitude };
      });
      return;
    }

    if (!address?.trim()) return
    const q = encodeURIComponent(`${address}, Romania`)

    fetch(`https://nominatim.openstreetmap.org/search?q=${q}&format=json&limit=1`, {
      headers: { 'Accept-Language': 'ro' },
    })
        .then((r) => r.json())
        .then((data) => {
          if (data?.[0]) {
            const newLat = parseFloat(data[0].lat)
            const newLng = parseFloat(data[0].lon)

            // ✅ VERIFICARE: Aceeași logică de siguranță și pentru rezultatul din API
            setCenter((prev) => {
              if (prev.lat === newLat && prev.lng === newLng) return prev;
              return { lat: newLat, lng: newLng };
            });
          }
        })
        .catch(() => {})
  }, [address, latitude, longitude])

  return (
      <MapContainer center={center} zoom={13} className="h-[280px] w-full rounded-2xl z-0" scrollWheelZoom={false}>
        <TileLayer attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        <Recenter center={center} />
        <Marker position={center} icon={icon}>
          <Popup>{address || 'Locație'}</Popup>
        </Marker>
      </MapContainer>
  )
}