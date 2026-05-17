import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Lock, Mail, User } from 'lucide-react'
import AppLayout from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import authService from '@/services/authService'

export default function Signup() {
  const [userData, setUserData] = useState({ username: '', email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSignup = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await authService.register(userData.username, userData.email, userData.password)
      alert('Cont creat cu succes! Te poți loga.')
      navigate('/login')
    } catch (error) {
      alert(error.response?.data?.message || 'Eroare la înregistrare.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AppLayout hideFooter>
      <div className="container-rentix flex min-h-[70vh] items-center justify-center py-12">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Creează un cont</CardTitle>
            <CardDescription>Alătură-te comunității Rentix</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSignup} className="space-y-4">
              <div className="relative">
                <User className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
                <Input className="pl-10" placeholder="Nume" required onChange={(e) => setUserData({ ...userData, username: e.target.value })} />
              </div>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
                <Input className="pl-10" type="email" placeholder="Email" required onChange={(e) => setUserData({ ...userData, email: e.target.value })} />
              </div>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
                <Input className="pl-10" type="password" placeholder="Parolă" required onChange={(e) => setUserData({ ...userData, password: e.target.value })} />
              </div>
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? 'Se creează...' : 'Înregistrează-te'}
              </Button>
            </form>
            <p className="mt-4 text-center text-sm text-text-muted">
              Ai deja cont? <Link to="/login" className="font-semibold text-brand-700">Autentifică-te</Link>
            </p>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}
