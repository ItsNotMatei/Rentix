import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Mail } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import authService from '@/services/authService'
import { toast } from '@/lib/toast'
import { getErrorMessage } from '@/lib/errors'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await authService.forgotPassword(email)
      setSent(true)
      toast.success('Dacă există un cont cu acest email, vei primi un link de resetare.')
    } catch (err) {
      toast.error(getErrorMessage(err, 'Nu s-a putut trimite emailul.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AppLayout hideFooter>
      <div className="container-rentix flex min-h-[70vh] items-center justify-center py-12">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Resetare parolă</CardTitle>
            <CardDescription>
              {sent
                ? 'Verifică inbox-ul (și spam) pentru linkul de la rentix@oficial.com'
                : 'Introdu emailul contului tău Rentix'}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {!sent ? (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
                  <Input
                    className="pl-10"
                    type="email"
                    placeholder="Email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                  />
                </div>
                <Button type="submit" className="w-full" disabled={loading}>
                  {loading ? 'Se trimite...' : 'Trimite link de resetare'}
                </Button>
              </form>
            ) : (
              <p className="text-sm text-text-muted">
                Linkul este valabil 1 oră. Dacă nu primești emailul, încearcă din nou sau contactează suportul.
              </p>
            )}
            <p className="mt-4 text-center text-sm">
              <Link to="/login" className="font-semibold text-brand-700 hover:underline">Înapoi la login</Link>
            </p>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}
