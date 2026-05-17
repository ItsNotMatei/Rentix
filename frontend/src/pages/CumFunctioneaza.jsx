import { Link } from 'react-router-dom'
import { Camera, MessageCircle, Shield, Wallet } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'

const STEPS = [
  { icon: Camera, title: 'Publică anunțul', text: 'Încarcă poze, setează preț și locație cu autocomplete.' },
  { icon: MessageCircle, title: 'Comunică în siguranță', text: 'Chat în timp real cu proprietari și chiriași verificați.' },
  { icon: Wallet, title: 'Plătește cu Stripe', text: 'Plăți și abonament PRO procesate securizat, confirmate automat.' },
  { icon: Shield, title: 'Escrow & verificare', text: 'Bani blocați până la confirmare. Identitate verificată cu buletinul.' },
]

export default function CumFunctioneaza() {
  return (
    <AppLayout>
      <section className="container-rentix py-16">
        <div className="mx-auto max-w-3xl text-center">
          <h1 className="text-4xl font-bold text-brand-700">Cum funcționează Rentix?</h1>
          <p className="mt-4 text-lg text-text-muted">
            Închiriază sau listezi echipamente în câțiva pași simpli, cu protecție pentru ambele părți.
          </p>
        </div>
        <div className="mt-14 grid gap-6 sm:grid-cols-2">
          {STEPS.map(({ icon: Icon, title, text }) => (
            <div key={title} className="card-premium p-6">
              <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-brand-100 text-brand-700">
                <Icon size={24} />
              </div>
              <h2 className="text-lg font-semibold">{title}</h2>
              <p className="mt-2 text-sm text-text-muted">{text}</p>
            </div>
          ))}
        </div>
        <div className="mt-12 flex flex-wrap justify-center gap-4">
          <Button asChild size="lg">
            <Link to="/signup">Creează cont</Link>
          </Button>
          <Button asChild variant="secondary" size="lg">
            <Link to="/anunturi">Explorează anunțuri</Link>
          </Button>
        </div>
      </section>
    </AppLayout>
  )
}
