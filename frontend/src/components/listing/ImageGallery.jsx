import { useState } from 'react'
import { ChevronLeft, ChevronRight, Expand, X, ZoomIn } from 'lucide-react'
import { AnimatePresence, motion } from 'framer-motion'
import { cn } from '@/lib/utils'

export default function ImageGallery({ images = [], title = 'Galerie' }) {
  const urls = images?.length ? images : ['https://images.unsplash.com/photo-1581244277943-fe4a9c777189?q=80&w=1200']
  const [index, setIndex] = useState(0)
  const [fullscreen, setFullscreen] = useState(false)
  const [zoom, setZoom] = useState(false)

  const prev = () => setIndex((i) => (i === 0 ? urls.length - 1 : i - 1))
  const next = () => setIndex((i) => (i === urls.length - 1 ? 0 : i + 1))

  return (
    <>
      <div className="grid gap-3 lg:grid-cols-[1fr_120px]">
        <div className="relative aspect-[16/10] overflow-hidden rounded-2xl bg-slate-100">
          <img
            src={urls[index]}
            alt={`${title} ${index + 1}`}
            className={cn('h-full w-full object-cover transition', zoom && 'scale-125 cursor-zoom-out')}
            onClick={() => setZoom((z) => !z)}
          />
          {urls.length > 1 && (
            <>
              <button type="button" onClick={prev} className="absolute left-3 top-1/2 -translate-y-1/2 rounded-full bg-white/90 p-2 shadow">
                <ChevronLeft size={20} />
              </button>
              <button type="button" onClick={next} className="absolute right-3 top-1/2 -translate-y-1/2 rounded-full bg-white/90 p-2 shadow">
                <ChevronRight size={20} />
              </button>
            </>
          )}
          <div className="absolute bottom-3 right-3 flex gap-2">
            <button type="button" onClick={() => setZoom((z) => !z)} className="rounded-full bg-white/90 p-2 shadow">
              <ZoomIn size={18} />
            </button>
            <button type="button" onClick={() => setFullscreen(true)} className="rounded-full bg-white/90 p-2 shadow">
              <Expand size={18} />
            </button>
          </div>
        </div>
        <div className="flex gap-2 overflow-x-auto lg:flex-col lg:overflow-y-auto">
          {urls.map((url, i) => (
            <button
              key={url + i}
              type="button"
              onClick={() => setIndex(i)}
              className={cn(
                'h-20 w-28 shrink-0 overflow-hidden rounded-xl border-2 lg:h-24 lg:w-full',
                i === index ? 'border-brand-600' : 'border-transparent opacity-80'
              )}
            >
              <img src={url} alt="" className="h-full w-full object-cover" loading="lazy" />
            </button>
          ))}
        </div>
      </div>

      <AnimatePresence>
        {fullscreen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-[100] flex items-center justify-center bg-black/90 p-4"
          >
            <button type="button" className="absolute right-4 top-4 rounded-full bg-white/10 p-2 text-white" onClick={() => setFullscreen(false)}>
              <X size={24} />
            </button>
            <img src={urls[index]} alt={title} className="max-h-[90vh] max-w-full object-contain" />
            {urls.length > 1 && (
              <div className="absolute bottom-6 flex gap-4">
                <button type="button" onClick={prev} className="rounded-full bg-white/20 p-3 text-white"><ChevronLeft /></button>
                <button type="button" onClick={next} className="rounded-full bg-white/20 p-3 text-white"><ChevronRight /></button>
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </>
  )
}
