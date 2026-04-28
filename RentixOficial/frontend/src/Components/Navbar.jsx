import { Link } from 'react-router'
import "./Navbar.css";

export default function Navbar() {
    return (
        <div  className="navbar">
            <Link to="/"  className="nav-logo">Rentix</Link>
            <Link to="/Mesaje" className="nav-button">Mesaje</Link>
            <Link to="/Adaugare" className="nav-button">Adaugare</Link>
            <Link to= "/Favorite" className="nav-button">Favorite</Link>

            <Link to="/Cont" className="nav-button nav-button-last">Cont</Link>
        </div>
    );
}