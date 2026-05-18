import AppLayout from '@/components/layout/AppLayout'

export default function LegalPage({ title, children }) {
  return (
    <AppLayout>
      <article className="container-rentix max-w-3xl py-12">
        <h1 className="text-3xl font-bold text-brand-700">{title}</h1>
        <div className="mt-6 space-y-4 text-text-muted leading-relaxed">{children}</div>
      </article>
    </AppLayout>
  )
}
