import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import AppLayout from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Input, Textarea } from '@/components/ui/input'
import AddressAutocomplete from '@/components/AddressAutocomplete'
import api, { getStoredUser } from '@/services/api'
import { CLOUDINARY_CLOUD, CLOUDINARY_PRESET } from '@/lib/utils'
import { LISTING_CATEGORIES, PRODUCT_CONDITIONS } from '@/lib/listingMeta'
import { notifyError } from '@/lib/errors'
import { toast } from '@/lib/toast'

const STEPS = ['Poze', 'Detalii', 'Locație', 'Previzualizare']

export default function AdaugaAnunt() {
  const navigate = useNavigate()
  const [step, setStep] = useState(0)
  const [titlu, setTitlu] = useState('')
  const [pret, setPret] = useState('')
  const [descriere, setDescriere] = useState('')
  const [adresa, setAdresa] = useState('')
  const [tip, setTip] = useState('Închiriere')
  const [categorie, setCategorie] = useState(LISTING_CATEGORIES[0])
  const [stareProdus, setStareProdus] = useState('PUTIN_FOLOSIT')
  const [images, setImages] = useState([])
  const [coverIndex, setCoverIndex] = useState(0)
  const [uploading, setUploading] = useState(false)
  const [dragOver, setDragOver] = useState(false)

  useEffect(() => {
    if (!getStoredUser()) navigate('/login')
  }, [navigate])

  const loadDraft = useCallback(() => {
    try {
      const d = JSON.parse(localStorage.getItem('rentix-listing-draft') || 'null')
      if (!d) return
      setTitlu(d.titlu || '')
      setPret(d.pret || '')
      setDescriere(d.descriere || '')
      setAdresa(d.adresa || '')
      setTip(d.tip || 'Închiriere')
      setCategorie(d.categorie || LISTING_CATEGORIES[0])
      setStareProdus(d.stareProdus || 'PUTIN_FOLOSIT')
      if (d.imageUrls?.length) {
        setImages(d.imageUrls.map((url, i) => ({ id: `draft-${i}`, url, file: null })))
      }
    } catch {
      /* ignore */
    }
  }, [])

  useEffect(() => {
    loadDraft()
  }, [loadDraft])

  useEffect(() => {
    const t = setTimeout(() => {
      const urls = images.filter((i) => i.url).map((i) => i.url)
      localStorage.setItem(
        'rentix-listing-draft',
        JSON.stringify({ titlu, pret, descriere, adresa, tip, categorie, stareProdus, imageUrls: urls })
      )
    }, 500)
    return () => clearTimeout(t)
  }, [titlu, pret, descriere, adresa, tip, categorie, stareProdus, images])

  const addFiles = (files) => {
    const next = [...images]
    Array.from(files).forEach((file) => {
      if (!file.type.startsWith('image/')) return
      next.push({ id: `${Date.now()}-${file.name}`, file, url: URL.createObjectURL(file) })
    })
    setImages(next)
  }

  const uploadOne = async (file) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('upload_preset', CLOUDINARY_PRESET)
    const res = await axios.post(`https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD}/image/upload`, formData)
    return res.data.secure_url
  }

  const handlePublish = async () => {
    if (!titlu || !pret || !adresa || !categorie) {
      toast.error('Completează titlul, prețul, categoria și locația.')
      return
    }
    setUploading(true)
    try {
      const urls = []
      for (const img of images) {
        if (img.url?.startsWith('http')) urls.push(img.url)
        else if (img.file) urls.push(await uploadOne(img.file))
      }
      const ordered = urls.length ? [urls[coverIndex], ...urls.filter((_, i) => i !== coverIndex)] : []
      await api.post('/api/products', {
        titlu,
        pret: parseFloat(pret),
        descriere,
        adresa,
        tip,
        categorie,
        stareProdus,
        status: 'AVAILABLE',
        imagineUrls: ordered,
        imagineUrl: ordered[0] || '',
      })
      localStorage.removeItem('rentix-listing-draft')
      toast.success('Anunț publicat!')
      navigate('/')
    } catch (err) {
      notifyError(err, 'Nu s-a putut publica anunțul.')
    } finally {
      setUploading(false)
    }
  }

  const progress = ((step + 1) / STEPS.length) * 100
  const stareLabel = PRODUCT_CONDITIONS.find((c) => c.value === stareProdus)?.label

  return (
    <AppLayout>
      <div className="container-rentix max-w-2xl py-10">
        <h1 className="text-2xl font-bold">Adaugă anunț</h1>
        <div className="mt-4 h-2 rounded-full bg-slate-200">
          <div className="h-2 rounded-full bg-brand-600 transition-all" style={{ width: `${progress}%` }} />
        </div>
        <p className="mt-2 text-sm text-text-muted">Pas {step + 1} din {STEPS.length}: {STEPS[step]}</p>

        {step === 0 && (
          <div className="mt-6 space-y-4">
            <div
              onDragOver={(e) => { e.preventDefault(); setDragOver(true) }}
              onDragLeave={() => setDragOver(false)}
              onDrop={(e) => { e.preventDefault(); setDragOver(false); addFiles(e.dataTransfer.files) }}
              className={`rounded-2xl border-2 border-dashed p-8 text-center ${dragOver ? 'border-brand-500 bg-brand-50' : 'border-border'}`}
            >
              <p className="text-sm text-text-muted">Trage poze aici sau</p>
              <Input type="file" accept="image/*" multiple className="mt-2" onChange={(e) => addFiles(e.target.files)} />
            </div>
            <div className="grid grid-cols-3 gap-3">
              {images.map((img, idx) => (
                <div key={img.id} className={`relative overflow-hidden rounded-xl border-2 ${idx === coverIndex ? 'border-brand-600' : 'border-transparent'}`}>
                  <img src={img.url} alt="" className="aspect-square w-full object-cover" />
                  <div className="absolute bottom-1 left-1 flex gap-1">
                    <button type="button" className="rounded bg-white/90 px-2 text-xs" onClick={() => setCoverIndex(idx)}>Copertă</button>
                    <button type="button" className="rounded bg-red-500/90 px-2 text-xs text-white" onClick={() => setImages(images.filter((x) => x.id !== img.id))}>Șterge</button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {step === 1 && (
          <div className="mt-6 space-y-4">
            <Input placeholder="Titlu" value={titlu} onChange={(e) => setTitlu(e.target.value)} />
            <Textarea placeholder="Descriere" value={descriere} onChange={(e) => setDescriere(e.target.value)} />
            <label className="block text-sm font-medium text-text-muted">
              Tip anunț
              <select className="mt-1 w-full rounded-xl border border-border px-4 py-2" value={tip} onChange={(e) => setTip(e.target.value)}>
                <option>Închiriere</option>
                <option>Vânzare</option>
              </select>
            </label>
            <label className="block text-sm font-medium text-text-muted">
              Categorie *
              <select className="mt-1 w-full rounded-xl border border-border px-4 py-2" value={categorie} onChange={(e) => setCategorie(e.target.value)}>
                {LISTING_CATEGORIES.map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </label>
            <label className="block text-sm font-medium text-text-muted">
              Starea produsului *
              <select className="mt-1 w-full rounded-xl border border-border px-4 py-2" value={stareProdus} onChange={(e) => setStareProdus(e.target.value)}>
                {PRODUCT_CONDITIONS.map((c) => (
                  <option key={c.value} value={c.value}>{c.label}</option>
                ))}
              </select>
            </label>
            <Input type="number" placeholder="Preț RON" value={pret} onChange={(e) => setPret(e.target.value)} />
          </div>
        )}

        {step === 2 && (
          <div className="mt-6">
            <AddressAutocomplete value={adresa} onChange={setAdresa} placeholder="Caută adresă în România..." />
          </div>
        )}

        {step === 3 && (
          <div className="mt-6 rounded-2xl border border-border bg-white p-4">
            {images[0] && <img src={images[coverIndex]?.url || images[0].url} alt="" className="mb-4 aspect-video w-full rounded-xl object-cover" />}
            <h2 className="text-xl font-bold">{titlu || 'Titlu anunț'}</h2>
            <p className="text-brand-700 font-semibold">{pret} RON · {tip}</p>
            <p className="mt-1 text-sm text-brand-600">{categorie} · {stareLabel}</p>
            <p className="text-sm text-text-muted">{adresa}</p>
            <p className="mt-2 text-sm">{descriere}</p>
          </div>
        )}

        <div className="mt-8 flex justify-between">
          <Button variant="secondary" disabled={step === 0} onClick={() => setStep((s) => s - 1)}>Înapoi</Button>
          {step < STEPS.length - 1 ? (
            <Button onClick={() => setStep((s) => s + 1)}>Continuă</Button>
          ) : (
            <Button onClick={handlePublish} disabled={uploading}>{uploading ? 'Se publică...' : 'Publică'}</Button>
          )}
        </div>
      </div>
    </AppLayout>
  )
}
