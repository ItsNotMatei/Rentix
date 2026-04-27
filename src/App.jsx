import { Routes, Route } from 'react-router'
import HomePage from "./pages/HomePage.jsx"
import Account from "./pages/Account"
import './App.css'

export default function App() {
  return (
    <Routes>
      <Route index element={<HomePage />} />
      <Route path="Cont" element={<Account />} />
    </Routes>
  )
}