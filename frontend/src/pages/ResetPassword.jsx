import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { Lock } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import authService from '@/services/authService'
import { toast } from '@/lib/toast'
import { getErrorMessage } from '@/lib/errors'

export default function ResetPassword() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') || ''
  const navigate = useNavigate()
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (password !== confirm) {
      toast.error('Parolele nu coincid.')
      return
    }
    if (password.length < 6) {
      toast.error('Parola trebuie să aibă minim 6 caractere.')
      return
    }
    if (!token) {
      toast.error('Link invalid. Solicită un link nou.')
      return
    }
    setLoading(true)
    try {
      await authService.resetPassword(token, password)
      toast.success('Parola a fost actualizată.')
      navigate('/login')
    } catch (err) {
      toast.error(getErrorMessage(err, 'Link invalid sau expirat.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AppLayout hideFooter>
      <div className="container-rentix flex min-h-[70vh] items-center justify-center py-12">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Parolă nouă</CardTitle>
            <CardDescription>Alege o parolă sigură pentru contul tău Rentix</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
                <Input
                  className="pl-10"
                  type="password"
                  placeholder="Parolă nouă"
                  required
                  minLength={6}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
                <Input
                  className="pl-10"
                  type="password"
                  placeholder="Confirmă parola"
                  required
                  value={confirm}
                  onChange={(e) => setConfirm(e.target.value)}
                />
              </div>
              <Button type="submit" className="w-full" disabled={loading || !token}>
                {loading ? 'Se salvează...' : 'Salvează parola'}
              </Button>
            </form>
            <p className="mt-4 text-center text-sm">
              <Link to="/login" className="font-semibold text-brand-700 hover:underline">Înapoi la login</Link>
            </p>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}
