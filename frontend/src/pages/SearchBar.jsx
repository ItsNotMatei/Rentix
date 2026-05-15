import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, X, Package } from 'lucide-react';
import axios from 'axios';

const SearchBar = () => {
    const [query, setQuery] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const searchRef = useRef(null);
    const navigate = useNavigate();

    // Închide dropdown-ul dacă se dă click în afara lui
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (searchRef.current && !searchRef.current.contains(e.target)) {
                setShowDropdown(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // Efect de Debounce pentru sugestiile live (Typeahead)
    useEffect(() => {
        if (query.trim().length < 2) {
            setSuggestions([]);
            setShowDropdown(false);
            return;
        }

        const delayFetch = setTimeout(async () => {
            try {
                const res = await axios.get(`http://localhost:8080/api/anunturi/search?query=${encodeURIComponent(query)}`);
                setSuggestions(res.data.slice(0, 5)); // Limităm sugestiile rapide la top 5 rezultate
                setShowDropdown(true);
            } catch (err) {
                console.error("Eroare la preluarea sugestiilor:", err);
            }
        }, 300); // Așteaptă 300ms de când utilizatorul s-a oprit din scris

        return () => clearTimeout(delayFetch);
    }, [query]);

    const executeSearch = (searchTerm) => {
        if (!searchTerm.trim()) return;
        setShowDropdown(false);
        // Redirecționează către pagina de rezultate
        navigate(`/anunturi?search=${encodeURIComponent(searchTerm)}`);
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            executeSearch(query);
        }
    };

    return (
        <div ref={searchRef} style={{ position: 'relative', width: '100%', maxWidth: '550px', margin: '0 auto' }}>
            {/* Caseta Input cu Lupa Turcoaz (Meciul vizual din screenshot) */}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                background: '#f8fafc',
                borderRadius: '30px',
                padding: '4px 6px 4px 20px',
                border: '1px solid #cbd5e1',
                boxShadow: '0 2px 4px rgba(0,0,0,0.02)'
            }}>
                <input
                    type="text"
                    placeholder="Caută echipamente, haine sau unelte de închiriat..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    onKeyDown={handleKeyDown}
                    onFocus={() => query.trim().length >= 2 && setShowDropdown(true)}
                    style={{
                        flex: 1,
                        background: 'transparent',
                        border: 'none',
                        outline: 'none',
                        fontSize: '15px',
                        color: '#1e293b',
                        padding: '8px 0'
                    }}
                />

                {query && (
                    <X
                        size={16}
                        color="#94a3b8"
                        onClick={() => { setQuery(''); setSuggestions([]); }}
                        style={{ cursor: 'pointer', marginRight: '12px' }}
                    />
                )}

                {/* Butonul rotund turcoaz cu lupă */}
                <button
                    onClick={() => executeSearch(query)}
                    style={{
                        background: '#0d9488',
                        color: '#fff',
                        border: 'none',
                        borderRadius: '50%',
                        width: '38px',
                        height: '38px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        cursor: 'pointer',
                        transition: 'background 0.2s'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#0f766e'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#0d9488'}
                >
                    <Search size={18} />
                </button>
            </div>

            {/* DROPDOWN SUGESTII LIVE */}
            {showDropdown && (
                <div style={{
                    position: 'absolute',
                    top: '115%',
                    left: 0,
                    width: '100%',
                    background: '#fff',
                    borderRadius: '16px',
                    boxShadow: '0 10px 25px rgba(0,0,0,0.08)',
                    border: '1px solid #e2e8f0',
                    zIndex: 1000,
                    overflow: 'hidden'
                }}>
                    {suggestions.length > 0 ? (
                        <>
                            <div style={{ padding: '10px 18px', fontSize: '11px', fontWeight: '700', color: '#94a3b8', background: '#f8fafc', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                                Sugestii potrivite
                            </div>
                            {suggestions.map((anunt) => (
                                <div
                                    key={anunt.id}
                                    onClick={() => {
                                        setQuery(anunt.titlu);
                                        executeSearch(anunt.titlu);
                                    }}
                                    style={{ display: 'flex', alignItems: 'center', gap: '14px', padding: '12px 18px', cursor: 'pointer', borderBottom: '1px solid #f1f5f9', transition: 'background 0.2s' }}
                                    onMouseEnter={(e) => e.currentTarget.style.background = '#f8fafc'}
                                    onMouseLeave={(e) => e.currentTarget.style.background = '#fff'}
                                >
                                    <div style={{ width: '32px', height: '32px', background: '#e0f2fe', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#0284c7' }}>
                                        <Package size={16} />
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                                        <span style={{ fontSize: '14px', fontWeight: '600', color: '#1e293b' }}>{anunt.titlu}</span>
                                        {anunt.pret && <span style={{ fontSize: '12px', color: '#0d9488', fontWeight: '700' }}>{anunt.pret} RON / zi</span>}
                                    </div>
                                </div>
                            ))}
                        </>
                    ) : (
                        <div style={{ padding: '16px', textAlign: 'center', color: '#64748b', fontSize: '14px' }}>
                            Apasă pe lupă sau Enter pentru a căuta "<strong>{query}</strong>"
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default SearchBar;