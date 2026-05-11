import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login';
import Signup from './pages/Signup';
import AdaugaAnunt from './pages/AdaugaAnunt';
import CalendarPage from './pages/CalendarPage';
import CumFunctioneaza from './pages/CumFunctioneaza';

function App() {
  return (
      <Router>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/adauga" element={<AdaugaAnunt />} />
          <Route path="/calendar" element={<CalendarPage />} />
          <Route path="/cum-functioneaza" element={<CumFunctioneaza />} />
        </Routes>
      </Router>
  );
}

export default App;
