import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import Signup from './pages/Signup'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import AdaugaAnunt from './pages/AdaugaAnunt'
import CalendarPage from './pages/CalendarPage'
import CumFunctioneaza from './pages/CumFunctioneaza'
import Termeni from './pages/Termeni'
import PoliticaConfidentialitate from './pages/PoliticaConfidentialitate'
import Profile from './pages/Profile'
import ProductDetails from './pages/ProductDetails'
import AnunturiList from './pages/AnunturiList'
import Chat from './pages/Chat'
import Checkout from './pages/Checkout'
import CheckoutSuccess from './pages/CheckoutSuccess'
import AdminDashboard from './pages/AdminDashboard'
import ProtectedRoute from './components/ProtectedRoute'
import AdminRoute from './components/AdminRoute'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/adauga" element={<ProtectedRoute><AdaugaAnunt /></ProtectedRoute>} />
        <Route path="/calendar" element={<CalendarPage />} />
                <Route path="/cum-functioneaza" element={<CumFunctioneaza />} />
                <Route path="/termeni" element={<Termeni />} />
                <Route path="/confidentialitate" element={<PoliticaConfidentialitate />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/chat" element={<ProtectedRoute><Chat /></ProtectedRoute>} />
        <Route path="/admin" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
        <Route path="/product/:id" element={<ProductDetails />} />
        <Route path="/anunturi" element={<AnunturiList />} />
        <Route path="/checkout/:id" element={<ProtectedRoute><Checkout /></ProtectedRoute>} />
        <Route path="/checkout/success" element={<ProtectedRoute><CheckoutSuccess /></ProtectedRoute>} />
        <Route path="/checkout/cancel" element={<ProtectedRoute><Checkout /></ProtectedRoute>} />
      </Routes>
    </Router>
  )
}

export default App
