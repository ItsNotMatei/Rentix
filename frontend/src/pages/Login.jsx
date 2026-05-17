import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Lock, LogIn, User } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import authService from '@/services/authService'

export default function Login() {
  const [loginData, setLoginData] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleLogin = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const user = await authService.login(loginData.email, loginData.password)
      const roles = ['MODERATOR', 'ADMIN', 'SUPER_ADMIN']
      navigate(roles.includes(user?.role) ? '/admin' : '/')
    } catch {
      alert('Email sau parolă greșită!')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AppLayout hideFooter>
      <div className="container-rentix flex min-h-[70vh] items-center justify-center py-12">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Bine ai revenit!</CardTitle>
            <CardDescription>Introdu datele pentru a intra în cont</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleLogin} className="space-y-4">
              <div className="relative">
                <User className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
                <Input className="pl-10" type="email" placeholder="Email" required onChange={(e) => setLoginData({ ...loginData, email: e.target.value })} />
              </div>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
                <Input className="pl-10" type="password" placeholder="Parolă" required onChange={(e) => setLoginData({ ...loginData, password: e.target.value })} />
              </div>
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? 'Se verifică...' : <><LogIn size={18} /> Conectează-te</>}
              </Button>
            </form>
            <p className="mt-4 text-center text-sm text-text-muted">
              Nu ai cont? <Link to="/signup" className="font-semibold text-brand-700">Înregistrează-te</Link>
            </p>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}
