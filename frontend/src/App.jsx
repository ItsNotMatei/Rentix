import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login';
import Signup from './pages/Signup';
import AdaugaAnunt from './pages/AdaugaAnunt';
import CalendarPage from './pages/CalendarPage';
import CumFunctioneaza from './pages/CumFunctioneaza';
import Profile from './pages/Profile';
import ChatInterface from './pages/ChatInterface.jsx';
import ProductDetails from "./pages/ProductDetails.jsx";
import AnunturiList from "./pages/AnunturiList.jsx";
import Chat from "./pages/Chat.jsx";
import Checkout from "./pages/Checkout.jsx";
function App() {
    return (
        <Router>
            <div className="app-main-wrapper">
            {/* Aici era problema! Tagul <Navbar /> a fost eliminat complet
                pentru că meniul tău cu căutare trăiește acum doar în Home.jsx */}
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/adauga" element={<AdaugaAnunt />} />
                <Route path="/calendar" element={<CalendarPage />} />
                <Route path="/cum-functioneaza" element={<CumFunctioneaza />} />
                <Route path="/profile" element={<Profile />} />
                <Route path="/chat" element={<ChatInterface />} />
                <Route path="/product/:id" element={<ProductDetails />} />
                <Route path="/anunturi" element={<AnunturiList />} />
                <Route path="/chat" element={<Chat />} />
                <Route path="/checkout/:id" element={<Checkout />} /> {/* Pagina ta de plată Stripe */}
            </Routes>
            </div>
        </Router>
    );
}

export default App;
