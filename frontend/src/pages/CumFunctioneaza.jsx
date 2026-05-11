import React from 'react';
import { Link } from 'react-router-dom';

const CumFunctioneaza = () => {
    return (
        <div className="how-page">
            <header className="navbar">
                <div className="nav-container">
                    <div className="logo">Rentix</div>
                    <nav>
                        <Link to="/">Acasă</Link>
                        <Link to="/categorii">Categorii</Link>
                    </nav>
                    <div className="search-bar-nav">
                        <input type="text" placeholder="Caută produse..." />
                    </div>
                    <div className="nav-right">
                        <Link to="#" className="nav-icon">♡ Favorite</Link>
                        <Link to="#" className="nav-icon">✉ Mesaje</Link>
                        <Link to="/login" className="btn dark">Autentificare</Link>
                        <Link to="/signup" className="btn outline">Înregistrare</Link>
                    </div>
                </div>
            </header>

            <section className="hero">
                <div className="hero-container">
                    <div className="hero-content">
                        <h1>Cum funcționează Rentix?</h1>
                        <p>Închiriază sau vinde rapid, în doar câțiva pași simpli.</p>
                    </div>
                </div>
            </section>

            <section className="section-container">
                <h2>Începe în 3 pași simpli</h2>
                <div className="steps-grid">
                    <div className="step-item">
                        <div className="step-icon">📸</div>
                        <div className="step-info">
                            <h4>Adaugă produsul</h4>
                            <p>Fă poze, adaugă descriere și setează prețul.</p>
                        </div>
                    </div>
                    {/* Adaugă restul elementelor step-item aici după același model */}
                </div>
            </section>
        </div>
    );
};

export default CumFunctioneaza;