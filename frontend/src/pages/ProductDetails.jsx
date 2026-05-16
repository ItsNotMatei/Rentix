import React, { useState, useEffect } from 'react';
import { Star } from 'lucide-react';
import { useParams, useNavigate } from 'react-router-dom';
import { Heart, Share2, MapPin, Calendar, Clock, ShieldCheck, MessageSquare, Tag } from 'lucide-react';
import { GoogleMap, Marker, useLoadScript } from '@react-google-maps/api';
import '../css/Details.css';

export default function ProductDetails() {
    const { id } = useParams();
    const navigate = useNavigate();

    // Stări existente
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [reviews, setReviews] = useState([]);
    const [rating, setRating] = useState(5);
    const [comment, setComment] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');

    // Stări noi adăugate (Favorite, Booking & Google Maps)
    const [isFavorite, setIsFavorite] = useState(false);
    const [bookingError, setBookingError] = useState('');

    const { isLoaded } = useLoadScript({
        googleMapsApiKey: 'GOOGLE_API_KEY' // Înlocuiește cu cheia ta reală de la Google
    });

    // 1. Încarcă detaliile produsului
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

    // 2. Funcția pentru a încărca recenziile
    const fetchReviews = async () => {
        try {
            const response = await fetch(`http://localhost:8080/api/reviews/${id}`);
            const data = await response.json();
            setReviews(data);
        } catch (error) {
            console.error(error);
        }
    };

    // 3. Încarcă recenziile la montarea componentei sau schimbarea ID-ului
    useEffect(() => {
        fetchReviews();
    }, [id]);

    // 4. Verificare stare Favorit în localStorage la încărcare
    useEffect(() => {
        const saved = localStorage.getItem(`favorite-${id}`);
        if (saved) {
            setIsFavorite(JSON.parse(saved));
        }
    }, [id]);

    // 5. Funcționalitate buton Favorite
    const toggleFavorite = () => {
        const nextFavoriteState = !isFavorite;
        setIsFavorite(nextFavoriteState);
        localStorage.setItem(`favorite-${id}`, JSON.stringify(nextFavoriteState));
    };

    // 6. Funcționalitate buton Share nativ
    const handleShare = async () => {
        try {
            await navigator.share({
                title: product?.titlu || 'Produs Rentix',
                text: product?.descriere || 'Aruncă o privire peste acest anunț pe Rentix!',
                url: window.location.href
            });
        } catch (error) {
            console.error("Eroare la partajare:", error);
        }
    };

    // 7. Funcția pentru trimiterea unei recenzii noi
    const handleReview = async () => {
        await fetch(`http://localhost:8080/api/reviews/${id}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                rating,
                comment
            })
        });

        setComment('');
        fetchReviews();
    };

    // 8. Funcția pentru realizarea unei rezervări (Închiriere)
    const handleBooking = async () => {
        try {
            setBookingError(''); // Resetăm eroarea din trecut la o nouă încercare
            const response = await fetch('http://localhost:8080/api/bookings', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    productId: id,
                    startDate,
                    endDate
                })
            });

            if (!response.ok) {
                const error = await response.text();
                setBookingError(error);
                return;
            }

            alert('Rezervare realizată cu succes');

        } catch (error) {
            console.error(error);
        }
    };

    // Redirecționare simplă pentru cumpărare directă
    const handleCumparaAcum = () => {
        navigate(`/checkout/${product.id}`);
    };

    const handleDeschideChat = () => {
        navigate(`/chat?cuUtilizator=${product.userId || 2}&produs=${product.id}`);
    };

    if (loading) return <div className="loading-spinner">Se încarcă detaliile produsului...</div>;
    if (!product) return <div className="error-message">Produsul nu a fost găsit în baza de date.</div>;

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
                                {/* Buton Favorite Dinamic */}
                                <button
                                    className={`circle-action-btn ${isFavorite ? 'active-favorite' : ''}`}
                                    onClick={toggleFavorite}
                                    title="Salvează la favorite"
                                >
                                    <Heart
                                        size={20}
                                        fill={isFavorite ? 'red' : 'transparent'}
                                        color={isFavorite ? 'red' : 'currentColor'}
                                    />
                                </button>
                                {/* Buton Share Dinamic */}
                                <button
                                    className="circle-action-btn"
                                    onClick={handleShare}
                                    title="Distribuie"
                                >
                                    <Share2 size={20} />
                                </button>
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

                    {/* Secțiune Hartă Google Maps */}
                    <div className="product-map-box" style={{ marginBottom: '24px' }}>
                        <h3>Locație produs</h3>
                        {isLoaded ? (
                            <GoogleMap
                                center={{ lat: 45.657974, lng: 25.601198 }}
                                zoom={12}
                                mapContainerStyle={{
                                    width: '100%',
                                    height: '300px',
                                    borderRadius: '16px'
                                }}
                            >
                                <Marker position={{ lat: 45.657974, lng: 25.601198 }} />
                            </GoogleMap>
                        ) : (
                            <div className="map-loading-placeholder" style={{ height: '300px', background: '#f3f4f6', borderRadius: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                Se încarcă harta...
                            </div>
                        )}
                    </div>

                    {/* CASETA DINAMICĂ DE PREȚ ȘI ACȚIUNE */}
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

                                <button className="primary-action-btn buy-now-btn" onClick={handleCumparaAcum}>
                                    Cumpără acum
                                </button>
                                <button className="secondary-action-btn offer-btn" onClick={handleDeschideChat}>
                                    Fă o ofertă
                                </button>
                            </div>
                        ) : (
                            /* --- STRUCTURA PENTRU ANUNȚURILE DE ÎNCHIRIERE (Cu Calendar & Rezervare) --- */
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

                                {/* Afișare dinamică pentru erorile de rezervare trimise de backend */}
                                {bookingError && (
                                    <p className="booking-error" style={{ color: '#dc2626', fontSize: '14px', marginTop: '8px', fontWeight: '500' }}>
                                        {bookingError}
                                    </p>
                                )}

                                <div className="hours-disclaimer">
                                    <Clock size={14} /> <span>Interval predare standard: 09:00 - 18:00</span>
                                </div>

                                <button className="primary-action-btn rent-now-btn" onClick={handleBooking}>
                                    Închiriază acum
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
                        <button className="chat-contact-btn" onClick={handleDeschideChat}>
                            <MessageSquare size={16} /> Întreabă
                        </button>
                    </div>

                </div>
            </div>

            {/* SECȚIUNEA DE RECENZII */}
            <div className="reviews-section">
                <h2>Recenzii</h2>

                <div className="add-review-box">
                    <select value={rating} onChange={(e) => setRating(Number(e.target.value))}>
                        <option value="5">5 ★</option>
                        <option value="4">4 ★</option>
                        <option value="3">3 ★</option>
                        <option value="2">2 ★</option>
                        <option value="1">1 ★</option>
                    </select>

                    <textarea
                        placeholder="Scrie o recenzie..."
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                    />

                    <button onClick={handleReview}>
                        Adaugă recenzie
                    </button>
                </div>

                {reviews.map((review) => (
                    <div key={review.id} className="review-card">
                        <div className="review-header">
                            <strong>{review.user?.nume || 'Utilizator'}</strong>
                            <span>{review.rating} ★</span>
                        </div>
                        <p>{review.comment}</p>
                    </div>
                ))}
            </div>
        </div>
    );
}