import Navbar from './Navbar'
import Footer from './Footer'

export default function AppLayout({ children, hideFooter = false }) {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <main className="flex-1">{children}</main>
      {!hideFooter && <Footer />}
    </div>
  )
}
