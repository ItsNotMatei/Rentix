import React, { useState } from 'react';
import { Link } from 'react-router-dom';

const Home = () => {
    const [isCatOpen, setIsCatOpen] = useState(false);

    return (
        <div>
            <header className="navbar">
                <div className="nav-container">
                    <div className="logo">Rentix</div>
                    <nav className="nav-links">
                        <div className="dropdown">
                            <button className="dropdown-btn" onClick={() => setIsCatOpen(!isCatOpen)}>
                                Categorii ▾
                            </button>
                            {isCatOpen && (
                                <div className="dropdown-menu active">
                                    <input type="text" className="dropdown-search" placeholder="Caută categorie..." />
                                    <div className="dropdown-list">
                                        <div className="cat-group">Vehicule</div>
                                        <label><input type="radio" name="cat" /> Mașini</label>
                                        <div className="cat-group">Tehnologie</div>
                                        <label><input type="radio" name="cat" /> Laptopuri</label>
                                    </div>
                                </div>
                            )}
                        </div>
                        <Link to="/cum-functioneaza">Cum funcționează</Link>
                    </nav>
                    <div className="search-bar-nav">
                        <input type="text" placeholder="Caută produse..." />
                    </div>
                    <div className="nav-right">
                        <Link to="/login" className="btn dark">Autentificare</Link>
                        <Link to="/signup" className="btn outline">Înregistrare</Link>
                    </div>
                </div>
            </header>

            <section className="section-container" style={{ marginTop: '40px' }}>
                <h3>Anunțuri recomandate</h3>
                <div className="product-grid">
                    {/* Card Exemplu */}
                    <div className="card" style={{ cursor: 'pointer' }}>
                        <div className="card-img">
                            <span className="badge rent">Închiriere</span>
                            <img src="https://via.placeholder.com/250x180" alt="produs" />
                        </div>
                        <div className="card-info">
                            <h4>Bormașină Bosch</h4>
                            <p className="price">50 RON / zi</p>
                            <p className="location">📍 București</p>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default Home;