import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Heart, Share2, MapPin, Calendar, Clock, ShieldCheck, MessageSquare, Tag } from 'lucide-react';
import '../css/Details.css';

export default function ProductDetails() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);

    // Stări pentru modul Închiriere (Calendar)
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');

    useEffect(() => {
        const fetchProduct = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/products/${id}`);
                if (response.ok) {
                    const data = await response.json();
                    setProduct(data);
                } else {
                    console.error("Produsul nu a fost găsit");
                }
            } catch (error) {
                console.error("Eroare la încărcarea produsului:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchProduct();
    }, [id]);

    if (loading) return <div className="loading-spinner">Se încarcă detaliile produsului...</div>;
    if (!product) return <div className="error-message">Produsul nu a fost găsit în baza de date.</div>;

    // Verificăm tipul anunțului (forțăm lowercase pentru siguranță la comparare)
    const esteVanzare = product.tip && product.tip.toLowerCase().includes('vânzare' || 'vanzare');
    const areRecenzii = product.reviews && product.reviews.length > 0;

    return (
        <div className="product-details-container">
            {/* Secțiunea Superioară: Galerie Imagini & Informații principale */}
            <div className="product-main-layout">

                {/* ZONA STÂNGA: Imaginea Produsului */}
                <div className="product-gallery-section">
                    <div className="main-image-wrapper">
                        <img
                            src={product.imageUrl || "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?q=80&w=600"}
                            alt={product.titlu}
                        />
                        <span className={`product-type-badge ${esteVanzare ? 'badge-vanzare' : 'badge-inchiriere'}`}>
                            {product.tip || "Închiriere"}
                        </span>
                    </div>
                </div>

                {/* ZONA DREAPTA: Detalii text & Caseta de Acțiune (Sidebar) */}
                <div className="product-info-sidebar-section">

                    {/* Detalii de bază */}
                    <div className="product-header-box">
                        <div className="header-meta-row">
                            <span className="product-category-tag">📋 Echipament</span>
                            <div className="action-buttons-group">
                                <button className="circle-action-btn" title="Salvează la favorite"><Heart size={20} /></button>
                                <button className="circle-action-btn" title="Distribuie"><Share2 size={20} /></button>
                            </div>
                        </div>

                        <h1 className="product-main-title">{product.titlu}</h1>

                        <div className="product-rating-row">
                            <span className="star-rating">★ 5.0</span>
                            <span className="reviews-count">({areRecenzii ? product.reviews.length : 0} recenzii)</span>
                            <span className="location-tag"><MapPin size={14} /> {product.adresa || "România"}</span>
                        </div>
                    </div>

                    {/* Descriere */}
                    <div className="product-description-box">
                        <h3>Descriere</h3>
                        <p>{product.descriere || "Nu au fost oferite detalii suplimentare despre acest produs."}</p>
                    </div>

                    {/* CASETA DINAMICĂ DE PREȚ ȘI ACȚIUNE (Aici se produce magia) */}
                    <div className="transaction-action-card">
                        <div className="price-display-row">
                            <span className="main-price-number">{product.pret} RON</span>
                            <span className="price-type-text">{esteVanzare ? '' : ' / zi'}</span>
                        </div>

                        {esteVanzare ? (
                            /* --- STRUCTURA PENTRU ANUNȚURILE DE VÂNZARE --- */
                            <div className="vanzare-action-block">
                                <div className="product-status-info">
                                    <Tag size={16} /> <span>Stare: <strong>Foarte bună</strong></span>
                                </div>

                                <button className="primary-action-btn buy-now-btn" onClick={() => alert('Procesare cumpărare...')}>
                                    Cumpără acum
                                </button>
                                <button className="secondary-action-btn offer-btn" onClick={() => alert('Trimite o ofertă...')}>
                                    Fă o ofertă
                                </button>
                            </div>
                        ) : (
                            /* --- STRUCTURA PENTRU ANUNȚURILE DE ÎNCHIRIERE (Cu Calendar) --- */
                            <div className="inchiriere-action-block">
                                <div className="picker-grid">
                                    <div className="picker-field">
                                        <label><Calendar size={14} /> Când?</label>
                                        <input
                                            type="date"
                                            value={startDate}
                                            onChange={(e) => setStartDate(e.target.value)}
                                        />
                                    </div>
                                    <div className="picker-field">
                                        <label><Calendar size={14} /> Până când?</label>
                                        <input
                                            type="date"
                                            value={endDate}
                                            onChange={(e) => setEndDate(e.target.value)}
                                        />
                                    </div>
                                </div>

                                <div className="hours-disclaimer">
                                    <Clock size={14} /> <span>Interval predare standard: 09:00 - 18:00</span>
                                </div>

                                <button className="primary-action-btn rent-now-btn" onClick={() => alert('Procesare închiriere...')}>
                                    Închiriază acuma
                                </button>
                            </div>
                        )}

                        {/* Elemente comune de siguranță inferioare */}
                        <div className="buyer-protection-box">
                            <ShieldCheck size={18} color="#0d9488" />
                            <p>Garanție de protecție inclusă prin Rentix.</p>
                        </div>
                    </div>

                    {/* Caseta Vânzător/Proprietar */}
                    <div className="owner-profile-card">
                        <div className="owner-avatar">
                            {product.user && product.user.nume ? product.user.nume.charAt(0).toUpperCase() : 'U'}
                        </div>
                        <div className="owner-meta">
                            <h4>{product.user && product.user.nume ? product.user.nume : `Utilizator #${product.userId || 'Extern'}`}</h4>
                            <p>Membru verificat Rentix</p>
                        </div>
                        <button className="chat-contact-btn">
                            <MessageSquare size={16} /> Întreabă
                        </button>
                    </div>

                </div>
            </div>
        </div>
    );
}

