import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Lock, LogIn, Mail, ShieldCheck, User } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import authService from '@/services/authService'
import { toast } from '@/lib/toast'
import { getErrorMessage } from '@/lib/errors'

export default function Login() {
  const [step, setStep] = useState('credentials')
  const [loginData, setLoginData] = useState({ email: '', password: '' })
  const [challengeId, setChallengeId] = useState('')
  const [code, setCode] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const finishLogin = (user) => {
    const roles = ['MODERATOR', 'ADMIN', 'SUPER_ADMIN']
    toast.success('Autentificare reușită!')
    navigate(roles.includes(user?.role) ? '/admin' : '/')
  }

  const handleLogin = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const result = await authService.login(loginData.email, loginData.password)
      if (result.requiresTwoFactor) {
        setChallengeId(result.challengeId)
        setStep('2fa')
        toast.info(result.message || 'Verifică emailul pentru codul de autentificare.')
      } else if (result.user) {
        finishLogin(result.user)
      }
    } catch (err) {
      toast.error(getErrorMessage(err, 'Email sau parolă greșită!'))
    } finally {
      setLoading(false)
    }
  }

  const handleVerify2fa = async (e) => {
    e.preventDefault()
    if (code.length !== 6) {
      toast.error('Introdu codul din 6 cifre.')
      return
    }
    setLoading(true)
    try {
      const user = await authService.verify2fa(challengeId, code)
      finishLogin(user)
    } catch (err) {
      toast.error(getErrorMessage(err, 'Cod invalid sau expirat.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AppLayout hideFooter>
      <div className="container-rentix flex min-h-[70vh] items-center justify-center py-12">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>{step === '2fa' ? 'Verificare în doi pași' : 'Bine ai revenit!'}</CardTitle>
            <CardDescription>
              {step === '2fa'
                ? 'Am trimis un cod la emailul tău de la rentix@oficial.com'
                : 'Introdu datele pentru a intra în cont'}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {step === 'credentials' ? (
              <form onSubmit={handleLogin} className="space-y-4">
                <div className="relative">
                  <User className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
                  <Input
                    className="pl-10"
                    type="email"
                    placeholder="Email"
                    required
                    value={loginData.email}
                    onChange={(e) => setLoginData({ ...loginData, email: e.target.value })}
                  />
                </div>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
                  <Input
                    className="pl-10"
                    type="password"
                    placeholder="Parolă"
                    required
                    value={loginData.password}
                    onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
                  />
                </div>
                <div className="text-right">
                  <Link to="/forgot-password" className="text-sm font-medium text-brand-700 hover:underline">
                    Ai uitat parola?
                  </Link>
                </div>
                <Button type="submit" className="w-full" disabled={loading}>
                  {loading ? 'Se verifică...' : <><LogIn size={18} /> Conectează-te</>}
                </Button>
              </form>
            ) : (
              <form onSubmit={handleVerify2fa} className="space-y-4">
                <div className="relative">
                  <ShieldCheck className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
                  <Input
                    className="pl-10 text-center text-lg tracking-[0.3em]"
                    type="text"
                    inputMode="numeric"
                    maxLength={6}
                    placeholder="000000"
                    required
                    value={code}
                    onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  />
                </div>
                <p className="flex items-center gap-2 text-xs text-text-muted">
                  <Mail size={14} /> Cod trimis la {loginData.email}
                </p>
                <Button type="submit" className="w-full" disabled={loading}>
                  {loading ? 'Se verifică...' : 'Confirmă codul'}
                </Button>
                <Button type="button" variant="secondary" className="w-full" onClick={() => setStep('credentials')}>
                  Înapoi
                </Button>
              </form>
            )}
            <p className="mt-4 text-center text-sm text-text-muted">
              Nu ai cont? <Link to="/signup" className="font-semibold text-brand-700">Înregistrează-te</Link>
            </p>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}
