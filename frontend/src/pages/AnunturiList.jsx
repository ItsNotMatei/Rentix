import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Package, Tag, ArrowRight, ShoppingBag } from 'lucide-react';
import axios from 'axios';

const AnunturiList = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    // Extragere cuvânt cheie din URL (ex: ?search=bormasina)
    const searchTerm = searchParams.get('search') || '';

    const [anunturi, setAnunturi] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchAnunturi = async () => {
            setLoading(true);
            try {
                let url = 'http://localhost:8080/api/anunturi';

                // Dacă utilizatorul a căutat ceva, schimbăm endpoint-ul către cel de search
                if (searchTerm.trim() !== '') {
                    url = `http://localhost:8080/api/anunturi/search?query=${encodeURIComponent(searchTerm)}`;
                }

                const res = await axios.get(url);
                setAnunturi(res.data);
            } catch (err) {
                console.error("Eroare la încărcarea anunțurilor:", err);
            } finally {
                setLoading(false);
            }
        };

        fetchAnunturi();
    }, [searchTerm]); // Re-execută efectul de fiecare dată când se schimbă căutarea în URL

    return (
        <div style={{ maxWidth: '1200px', margin: '40px auto', padding: '0 20px', fontFamily: 'sans-serif' }}>

            {/* Header pagină / Titlu dinamic */}
            <div style={{ marginBottom: '30px', borderBottom: '1px solid #e2e8f0', paddingBottom: '16px' }}>
                {searchTerm ? (
                    <h2 style={{ fontSize: '24px', fontWeight: '700', color: '#1e293b', margin: 0 }}>
                        Rezultatele căutării pentru: <span style={{ color: '#0d9488' }}>"{searchTerm}"</span>
                        <small style={{ display: 'block', fontSize: '14px', color: '#64748b', fontWeight: '400', marginTop: '6px' }}>
                            Am găsit {anunturi.length} {anunturi.length === 1 ? 'rezultat' : 'rezultate'}
                        </small>
                    </h2>
                ) : (
                    <h2 style={{ fontSize: '24px', fontWeight: '700', color: '#1e293b', margin: 0 }}>
                        Toate echipamentele disponibile
                    </h2>
                )}
            </div>

            {/* Stare de încărcare (Skeleton/Loader) */}
            {loading && (
                <div style={{ textAlign: 'center', padding: '60px 0', color: '#64748b', fontSize: '16px' }}>
                    Se încarcă produsele...
                </div>
            )}

            {/* Cazul în care nu s-a găsit niciun produs */}
            {!loading && anunturi.length === 0 && (
                <div style={{ textAlign: 'center', padding: '60px 20px', background: '#f8fafc', borderRadius: '16px', border: '1px dashed #cbd5e1' }}>
                    <ShoppingBag size={48} color="#94a3b8" style={{ marginBottom: '16px' }} />
                    <h3 style={{ fontSize: '18px', fontWeight: '700', color: '#334155', margin: '0 0 8px 0' }}>Nu am găsit ce căutai</h3>
                    <p style={{ color: '#64748b', margin: '0 0 20px 0', fontSize: '14px' }}>Verifică corectitudinea cuvintelor sau încearcă termeni mai generali.</p>
                    <button
                        onClick={() => navigate('/anunturi')}
                        style={{ background: '#0d9488', color: '#fff', border: 'none', padding: '10px 20px', borderRadius: '8px', fontWeight: '600', cursor: 'pointer' }}
                    >
                        Vezi toate produsele
                    </button>
                </div>
            )}

            {/* Grid-ul de produse tip E-commerce */}
            {!loading && anunturi.length > 0 && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '24px' }}>
                    {anunturi.map((anunt) => (
                        <div
                            key={anunt.id}
                            onClick={() => navigate(`/product/${anunt.id}`)}
                            style={{ background: '#fff', border: '1px solid #e2e8f0', borderRadius: '14px', overflow: 'hidden', cursor: 'pointer', transition: 'transform 0.2s, box-shadow 0.2s', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.transform = 'translateY(-4px)';
                                e.currentTarget.style.boxShadow = '0 10px 20px rgba(0,0,0,0.05)';
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.transform = 'none';
                                e.currentTarget.style.boxShadow = 'none';
                            }}
                        >
                            {/* Imagine sau Placeholder */}
                            <div style={{ width: '100%', height: '180px', background: '#f1f5f9', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94a3b8' }}>
                                {anunt.imagineUrl ? (
                                    <img src={anunt.imagineUrl} alt={anunt.titlu} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                                ) : (
                                    <Package size={40} />
                                )}
                            </div>

                            {/* Informații Produs */}
                            <div style={{ padding: '20px', flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                                <div>
                                    <h3 style={{ fontSize: '16px', fontWeight: '700', color: '#1e293b', margin: '0 0 8px 0', lineHeight: '1.4' }}>
                                        {anunt.titlu}
                                    </h3>
                                    <p style={{ fontSize: '13px', color: '#64748b', margin: '0 0 16px 0', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden', textOverflow: 'ellipsis', lineHeight: '1.5' }}>
                                        {anunt.descriere || 'Nicio descriere adăugată acestui produs.'}
                                    </p>
                                </div>

                                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderTop: '1px solid #f1f5f9', paddingTop: '14px', marginTop: '10px' }}>
                                    <div>
                                        <span style={{ block: 'block', fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', fontWeight: '600' }}>Preț închiriere</span>
                                        <span style={{ fontSize: '18px', fontWeight: '800', color: '#0d9488' }}>{anunt.pret} RON <small style={{ fontSize: '12px', fontWeight: '400', color: '#64748b' }}>/ zi</small></span>
                                    </div>
                                    <div style={{ background: '#f0fdfa', color: '#0d9488', width: '36px', height: '36px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                        <ArrowRight size={18} />
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default AnunturiList;