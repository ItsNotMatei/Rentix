import { Link } from 'react-router'
import "./Navbar.css";

export default function Navbar() {
  return (
    <div  className="navbar">
      <Link to="/"  className="nav-logo">Rentix</Link>
      <button className="nav-button">Mesaje</button>
      <button className="nav-button">Adaugare</button>
      <button className="nav-button">Favorite</button>

      <Link to="/Cont" className="nav-button nav-button-last">Cont</Link>
    </div>
  );
}