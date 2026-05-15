import React, { useState, useEffect, useRef } from 'react';
import '../css/style.css';
import { Link, useNavigate } from 'react-router-dom';
import {
    Search,
    MessageCircle,
    Plus,
    Heart,
    User,
    LogOut,
    X,
    Package,
    Trash2,
    CheckCircle // Am adăugat bifa pentru utilizator verificat
} from "lucide-react";
import axios from 'axios';

export default function Home() {
    const [user, setUser] = useState(null);
    const [anunturi, setAnunturi] = useState([]);
    const navigate = useNavigate();

    // --- STĂRI PENTRU SEARCH BAR ---
    const [query, setQuery] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const searchRef = useRef(null);

    useEffect(() => {
        const loadData = async () => {
            const storedUser = localStorage.getItem("user");
            if (storedUser) {
                setUser(JSON.parse(storedUser));
            }

            try {
                // Încercăm să luăm datele din API-ul tău existent
                const response = await fetch("http://localhost:8080/api/anunturi");
                if (response.ok) {
                    const data = await response.json();
                    setAnunturi(data);
                } else {
                    const storedAds = JSON.parse(localStorage.getItem("anunturi")) || [];
                    setAnunturi(storedAds);
                }
            } catch (error) {
                console.error("Eroare la conectarea cu API-ul:", error);
                const storedAds = JSON.parse(localStorage.getItem("anunturi")) || [];
                setAnunturi(storedAds);
            }
        };

        loadData();
    }, []);

    // --- FUNCȚIE ADMIN: Ștergere Anunț ---
    const handleDeleteAnunt = async (e, id) => {
        e.stopPropagation(); // Oprim navigarea către detalii când apăsăm pe șterge
        if (window.confirm("Ești sigur că vrei să ștergi acest anunț ca Admin?")) {
            try {
                await axios.delete(`http://localhost:8080/api/anunturi/${id}`);
                setAnunturi(anunturi.filter(a => a.id !== id));
                alert("Anunțul a fost eliminat.");
            } catch (err) {
                console.error("Eroare la ștergere:", err);
                alert("Nu s-a putut șterge anunțul.");
            }
        }
    };

    // Restul logică search (rămâne neschimbată)
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (searchRef.current && !searchRef.current.contains(e.target)) {
                setShowDropdown(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    useEffect(() => {
        if (query.trim().length < 2) {
            setSuggestions([]);
            setShowDropdown(false);
            return;
        }

        const delayFetch = setTimeout(async () => {
            try {
                const res = await axios.get(`http://localhost:8080/api/anunturi/search?query=${encodeURIComponent(query)}`);
                setSuggestions(res.data.slice(0, 5));
                setShowDropdown(true);
            } catch (err) {
                console.error("Eroare la preluarea sugestiilor:", err);
            }
        }, 300);

        return () => clearTimeout(delayFetch);
    }, [query]);

    const executeSearch = (searchTerm) => {
        if (!searchTerm.trim()) return;
        setShowDropdown(false);
        navigate(`/anunturi?search=${encodeURIComponent(searchTerm)}`);
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            executeSearch(query);
        }
    };

    const handleProductClick = (productId) => {
        navigate(`/product/${productId}`);
    };

    return (
        <div className="home-page-container">
            {/* NAVBAR (Rămâne neschimbat) */}
            <header className="main-navbar">
                <div className="navbar-wrapper">
                    <div className="navbar-logo" onClick={() => navigate('/')} style={{cursor: 'pointer'}}>
                        Rentix
                    </div>

                    <div className="navbar-search" ref={searchRef} style={{ position: 'relative' }}>
                        <input
                            type="text"
                            placeholder="Caută echipamente, haine sau unelte de închiriat..."
                            value={query}
                            onChange={(e) => setQuery(e.target.value)}
                            onKeyDown={handleKeyDown}
                            onFocus={() => query.trim().length >= 2 && setShowDropdown(true)}
                        />

                        {query && (
                            <X
                                size={16}
                                color="#94a3b8"
                                onClick={() => { setQuery(''); setSuggestions([]); }}
                                style={{ cursor: 'pointer', marginRight: '10px', position: 'absolute', right: '55px' }}
                            />
                        )}

                        <button className="search-submit-btn" onClick={() => executeSearch(query)}>
                            <Search size={18} />
                        </button>

                        {showDropdown && (
                            <div className="search-suggestions-dropdown" style={{
                                position: 'absolute', top: '115%', left: 0, width: '100%',
                                background: '#fff', borderRadius: '14px', boxShadow: '0 10px 25px rgba(0,0,0,0.08)',
                                border: '1px solid #e2e8f0', zIndex: 1000, overflow: 'hidden'
                            }}>
                                {suggestions.length > 0 ? (
                                    <>
                                        <div style={{ padding: '10px 18px', fontSize: '11px', fontWeight: '700', color: '#94a3b8', background: '#f8fafc', textTransform: 'uppercase' }}>
                                            Sugestii potrivite
                                        </div>
                                        {suggestions.map((anunt) => (
                                            <div
                                                key={anunt.id}
                                                onClick={() => { setQuery(anunt.titlu); executeSearch(anunt.titlu); }}
                                                className="suggestion-item"
                                                style={{ display: 'flex', alignItems: 'center', gap: '14px', padding: '12px 18px', cursor: 'pointer', borderBottom: '1px solid #f1f5f9' }}
                                            >
                                                <Package size={16} color="#0284c7" />
                                                <span>{anunt.titlu}</span>
                                            </div>
                                        ))}
                                    </>
                                ) : (
                                    <div style={{ padding: '16px', color: '#64748b' }}>Apasă Enter pentru a căuta "<strong>{query}</strong>"</div>
                                )}
                            </div>
                        )}
                    </div>

                    <div className="navbar-actions">
                        <Link to="/profile?tab=chat" className="nav-action-icon"><MessageCircle size={22} /></Link>
                        {user ? (
                            <div className="logged-user-wrapper" style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                                <Link to="/profile" className="welcome-user-text" style={{ textDecoration: 'none', color: 'inherit' }}>{user.nume}</Link>
                                {/* BIFĂ BULETIN NAVBAR */}
                                {user?.isVerified && (
                                    <CheckCircle size={15} color="#0284c7" fill="#e0f2fe" style={{ marginLeft: '-2px' }} title="Utilizator Verificat cu Buletinul" />
                                )}
                                <Link to="/profile" className="nav-action-icon active-profile-btn"><User size={22} /></Link>
                            </div>
                        ) : (
                            <div className="navbar-auth-group">
                                <Link to="/login" className="auth-nav-link login-nav-link">Autentificare</Link>
                                <Link to="/signup" className="auth-nav-btn signup-nav-btn">Cont Nou</Link>
                            </div>
                        )}
                    </div>
                </div>
            </header>

            {/* HERO SECTION (Rămâne neschimbat) */}
            <section className="rentix-hero-section">
                <div className="hero-content-left">
                    <h1>Închiriază inteligent. <span>Folosește ce ai nevoie, când ai nevoie.</span></h1>
                    <p>Rentix îți oferă acces la o comunitate modernă unde poți închiria orice, simplu și securizat.</p>
                    <button className="hero-explore-btn" onClick={() => window.scrollTo({top: 750, behavior: 'smooth'})}>Explorează catalogul</button>
                </div>
                <div className="hero-banner-right">
                    <img src="https://images.unsplash.com/photo-1529139574466-a303027c1d8b?q=80&w=1200&auto=format&fit=crop" alt="Hero" />
                </div>
            </section>

            {/* CATEGORIES SECTION (Rămâne neschimbat) */}
            <section className="rentix-categories-section">
                <h2 className="section-main-title">Categorii Echipamente</h2>
                <div className="categories-flex-grid">
                    {["🧥 Haine", "📱 Gadgeturi", "🔧 Scule", "🚲 Sport", "🎮 Console", "🚘 Auto"].map(cat => (
                        <div key={cat} className="rentix-category-item" onClick={() => executeSearch(cat)} style={{ cursor: 'pointer' }}>{cat}</div>
                    ))}
                </div>
            </section>

            {/* PRODUCTS SECTION (Cu funcție Admin adăugată) */}
            <section className="rentix-listings-section">
                <h2 className="section-main-title">Anunțuri Recomandate</h2>
                <div className="listings-responsive-grid">
                    {anunturi.length === 0 ? (
                        <p className="no-listings-message">Nu există articole disponibile.</p>
                    ) : (
                        anunturi.map((anunt) => (
                            <div
                                className="rentix-ecommerce-card"
                                key={anunt.id}
                                onClick={() => handleProductClick(anunt.id)}
                                style={{ position: 'relative' }}
                            >
                                {/* BUTON ADMIN ȘTERGERE - Apare doar dacă userul are rolul ADMIN */}
                                {user?.role === 'ADMIN' && (
                                    <button
                                        onClick={(e) => handleDeleteAnunt(e, anunt.id)}
                                        style={{
                                            position: 'absolute', top: '10px', left: '10px', zIndex: 100,
                                            backgroundColor: '#ef4444', color: 'white', border: 'none',
                                            borderRadius: '50%', width: '36px', height: '36px',
                                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                                            cursor: 'pointer', boxShadow: '0 2px 8px rgba(0,0,0,0.2)'
                                        }}
                                        title="Șterge (Admin)"
                                    >
                                        <Trash2 size={18} />
                                    </button>
                                )}

                                <div className="card-favorite-action" onClick={(e) => e.stopPropagation()}><Heart size={18} /></div>

                                <div className="card-media-header">
                                    <img src={anunt.imageUrl || "https://via.placeholder.com/600"} alt={anunt.titlu} className="card-main-img" />
                                    <div className="card-type-overlay-badge">{anunt.tip || "Închiriere"}</div>
                                </div>

                                <div className="card-info-content">
                                    <h3 className="card-product-title">{anunt.titlu}</h3>
                                    <div className="card-owner-row" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                        <div className="owner-avatar-circle">{anunt.user?.nume?.charAt(0) || 'U'}</div>
                                        <span className="owner-display-name" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                            {anunt.user?.nume || "Proprietar"}
                                            {/* BIFĂ BULETIN PE CARDUL PRODUSULUI */}
                                            {anunt.user?.verified && (
                                                <CheckCircle size={14} color="#0284c7" fill="#e0f2fe" title="Proprietar Verificat cu Buletinul" />
                                            )}
                                        </span>
                                    </div>
                                    <div className="card-meta-pricing-footer">
                                        <span className="card-location-text">📍 {anunt.adresa || "Nespecificată"}</span>
                                        <div className="card-price-block">
                                            <span className="price-number">{anunt.pret}</span>
                                            <span className="price-currency"> RON/zi</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </section>

            <Link to="/adauga" className="rentix-floating-add-btn"><Plus size={28} /></Link>
        </div>
    );
}