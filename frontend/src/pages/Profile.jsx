import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate, useSearchParams, Link } from 'react-router-dom';
import { User, MessageCircle, Package, Heart, Star, Shield, LogOut, CheckCircle, Crown, Check, X, Zap } from 'lucide-react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import axios from 'axios';
import authService from '../services/authService';
import api from '../services/api';
import '../css/profile.css';

const Profile = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();

    // Citim user-ul din localStorage la inițializare
    const savedUser = JSON.parse(localStorage.getItem("user")) || { nume: "Utilizator", email: "nespecificat@email.com", pro: false, isVerified: false, verified: false };
    const [user, setUser] = useState(savedUser);

    // Citim tab-ul curent direct din URL sau aplicăm valoarea implicită 'cont'
    const activeTab = searchParams.get('tab') || 'cont';

    // Stări pentru formularul de editare date
    const [editMode, setEditMode] = useState(false);
    const [nume, setNume] = useState(user?.nume || '');
    const [telefon, setTelefon] = useState(user?.telefon || '');
    const [adresa, setAdresa] = useState(user?.adresa || '');

    const [messages, setMessages] = useState([]);
    const [inputValue, setInputValue] = useState("");
    const [connected, setConnected] = useState(false);

    // Stare pentru încărcarea sesiunii Stripe Identity
    const [verifying, setVerifying] = useState(false);
    const stompClient = useRef(null);

    // --- EVALUARE FLEXIBILĂ PROPRIETATE VERIFICARE (Sincronizare completă cu MySQL tinyint/boolean) ---
    const esteVerificat = user?.isVerified === true || user?.isVerified === 1 || user?.verified === true || user?.verified === 1;

    // --- 1. VERIFICARE SUCCES STRIPE SUBSCRIPTION ÎN URL ---
    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('subscription') === 'success' && user?.id) {
            axios.post(`http://localhost:8080/api/payments/activate-pro?userId=${user.id}`)
                .then(res => {
                    localStorage.setItem('user', JSON.stringify(res.data));
                    setUser(res.data);
                    alert("Felicitări! Abonamentul tău Rentix PRO este acum activ! 👑");
                    setSearchParams({ tab: 'cont' });
                })
                .catch(err => console.error("Eroare activare PRO:", err));
        }
    }, [location.search, user?.id]);

    // --- 2. LOGICĂ UNIFICATĂ REVENIRE STRIPE IDENTITY (Sincronizare sigură cu local storage) ---
    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('verification') === 'success' && user?.id) {
            api.post(`/api/identity/webhook-verified`, { sessionId: urlParams.get('session_id') })
                .then(res => {
                    // Măsură de siguranță: Ne asigurăm că ambele posibile denumiri (isVerified și verified) devin true în local storage
                    const updatedUser = {
                        ...res.data,
                        isVerified: true,
                        verified: true
                    };

                    localStorage.setItem('user', JSON.stringify(updatedUser));
                    setUser(updatedUser);

                    alert("Identitatea ta a fost verificată cu succes prin Stripe! Bifa albastră a fost activată. ✓");

                    // Curățăm parametrii din URL pentru a nu re-executa ruta la refresh
                    setSearchParams({ tab: 'cont' });
                })
                .catch(err => console.error("Eroare salvare verificare identitate:", err));
        }
    }, [location.search, user?.id, setSearchParams]);

    const handleSendMessage = () => {
        if (inputValue.trim() && connected && stompClient.current) {
            const chatMessage = {
                senderId: user.nume || "Anonim",
                content: inputValue,
                timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
                type: 'CHAT'
            };

            stompClient.current.send("/app/chat.send", {}, JSON.stringify(chatMessage));
            setInputValue("");
        }
    };

    const handleTabChange = (tabName) => {
        setEditMode(false);
        setSearchParams({ tab: tabName });
    };

    const handleLogout = async () => {
        await authService.logout();
        navigate('/');
        window.location.reload();
    };

    // --- LOGICĂ ACTUALIZARE PROFIL ---
    const handleUpdateProfile = async (e) => {
        e.preventDefault();
        try {
            const res = await axios.put(`http://localhost:8080/api/payments/update-profile/${user.id}`, {
                nume, telefon, adresa
            });
            localStorage.setItem('user', JSON.stringify(res.data));
            setUser(res.data);
            setEditMode(false);
            alert("Datele contului au fost actualizate!");
        } catch (err) {
            console.error("Eroare salvare profil:", err);
            alert("A apărut o eroare la salvarea datelor.");
        }
    };

    // --- LOGICĂ SUB-SESSION STRIPE ---
    const handleSubscribeStripe = async () => {
        if (!user?.id) {
            alert("Trebuie să fii autentificat pentru a te abona.");
            return;
        }
        try {
            const res = await axios.post('http://localhost:8080/api/payments/create-subscription-session', {
                userId: user.id,
                userEmail: user.email,
                planType: "PRO"
            });

            if (res.data && res.data.url) {
                window.location.href = res.data.url;
            } else {
                console.error("URL-ul Stripe lipsește din răspuns:", res.data);
            }
        } catch (err) {
            console.error("Stripe error details:", err.response?.data || err.message);
            alert("Eroare la conectarea cu Stripe. Verifică consola pentru detalii.");
        }
    };
// 1. Funcția care trimite datele dinamice către Spring Boot
    const handleRentProduct = async () => {
        try {
            const response = await axios.post('http://localhost:8080/api/payments/create-escrow-session', {
                userId: user.id,                // ID-ul chiriașului (cel logat)
                productName: "Bormașină Makita", // Numele luat din starea produsului tau
                amount: 150                     // Prețul total în RON calculat din zbor
            });

            if (response.data && response.data.url) {
                // Trimitem chiriașul pe pagina Stripe să blocheze banii
                window.location.href = response.data.url;
            }
        } catch (err) {
            console.error("Eroare la pornirea Escrow:", err);
            alert("Nu s-a putut inițializa plata securizată.");
        }
    };

// 2. Butonul pe care îl randezi în interfață (în return-ul componentei):
    <button
        onClick={handleRentProduct}
        style={{ background: '#0d9488', color: '#fff', padding: '12px 24px', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}
    >
        Închiriază în siguranță (Escrow)
    </button>
    // --- LOGICĂ STRIPE IDENTITY (Reparată cu trimitere ID curat) ---
    const handleStartVerification = async (userId) => {
        if (!userId) {
            alert("Eroare: ID-ul utilizatorului lipsește.");
            return;
        }
        setVerifying(true);
        try {
            const response = await api.post(`/api/identity/create-session`);

            if (response.data && response.data.url) {
                console.log("Redirecționare către Stripe Identity...", response.data.url);
                window.location.href = response.data.url;
            } else {
                console.error("Serverul nu a returnat un URL valid", response.data);
                alert("Eroare la configurarea sesiunii de verificare.");
            }
        } catch (error) {
            console.error("Eroare Stripe Identity:", error);
            alert("A apărut o eroare la pornirea verificării. Verifică consola backend.");
        } finally {
            setVerifying(false);
        }
    };

    const renderContent = () => {
        switch (activeTab) {
            case 'chat':
                return (
                    <div className="tab-content-fade">
                        <h2 className="tab-section-title">Mesaje</h2>
                        <p style={{ color: '#64748b', marginBottom: '16px' }}>
                            Deschide conversațiile cu alți utilizatori din pagina unui anunț sau din centrul de mesaje.
                        </p>
                        <Link to="/chat" className="auth-submit-btn" style={{ display: 'inline-flex', width: 'auto', textDecoration: 'none' }}>
                            Deschide mesajele
                        </Link>
                    </div>
                );
            case 'comenzi':
                return (
                    <div className="tab-content-fade">
                        <h2 className="tab-section-title">Comenzile mele (Sistem Escrow Active)</h2>

                        {/* Exemplu de card de comandă activă blocat în sistemul Middle-Man */}
                        <div style={{ border: '1px solid #cbd5e1', padding: '20px', borderRadius: '12px', background: '#fff', marginTop: '16px' }}>
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                <div>
                                    <h4 style={{ margin: '0 0 4px 0', fontSize: '16px', fontWeight: '700' }}>Bormașină Makita XPT</h4>
                                    <p style={{ margin: 0, fontSize: '13px', color: '#6b7280' }}>ID Tranzacție Stripe: <code style={{ background: '#f1f5f9', padding: '2px 6px', borderRadius: '4px' }}>pi_3MtwXBL...</code></p>
                                </div>
                                <span style={{ background: '#fef3c7', color: '#d97706', padding: '4px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '600' }}>
                        Bani Blocați (Escrow)
                    </span>
                            </div>

                            <hr style={{ border: 'none', borderTop: '1px solid #f1f5f9', margin: '14px 0' }} />

                            <p style={{ fontSize: '13.5px', color: '#4b5563', lineHeight: '1.5', margin: '0 0 16px 0' }}>
                                🔒 Banii tăi (150 RON) sunt în siguranță la Rentix. Proprietarul va primi suma doar după ce apeși pe butonul de mai jos, confirmând că ai intrat în posesia obiectului și acesta corespunde descrierii.
                            </p>

                            <button
                                onClick={() => {
                                    // Aici pasezi ID-ul PaymentIntent primit de la Stripe salvat la comandă
                                    const mockPaymentIntentId = "pi_AICI_PUI_ID_UL_TRANZACTIEI";

                                    axios.post(`http://localhost:8080/api/payments/capture-payment/${mockPaymentIntentId}`)
                                        .then(() => {
                                            alert("Tranzacție finalizată! Banii au plecat spre proprietar. Mulțumim că folosești Rentix Escrow! 🤝");
                                            window.location.reload();
                                        })
                                        .catch(err => console.error("Eroare la captură:", err));
                                }}
                                style={{ background: '#22c55e', color: '#fff', border: 'none', padding: '10px 18px', borderRadius: '8px', fontWeight: '600', cursor: 'pointer', fontSize: '13px' }}
                            >
                                Confirm Primirea Produsului (Eliberează Banii)
                            </button>
                        </div>
                    </div>
                );
            case 'favorite':
                return (
                    <div className="tab-content-fade">
                        <h2 className="tab-section-title">Anunțuri favorite</h2>
                        <div className="empty-state-box">
                            <Heart size={48} className="empty-icon" />
                            <h3>Lista ta este goală</h3>
                            <p>Salvează produsele care îți plac apasând pe iconița în formă de inimă din pagina principală.</p>
                        </div>
                    </div>
                );
            case 'beneficii':
                return (
                    <div className="tab-content-fade">
                        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
                            <h2 style={{ fontSize: '26px', fontWeight: '800', color: '#111827', margin: '0 0 8px 0' }}>Planurile Rentix</h2>
                            <p style={{ color: '#6b7280', fontSize: '15px' }}>Comisioane reduse, listări nelimitate și protecție totală. Anulezi oricând.</p>
                        </div>

                        <div style={{ display: 'flex', gap: '28px', justifyContent: 'center', flexWrap: 'wrap' }}>
                            {/* PLAN NORMAL */}
                            <div style={{ border: '2px solid #e2e8f0', borderRadius: '16px', padding: '32px 24px', width: '280px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', background: '#fff' }}>
                                <div>
                                    <h3 style={{ fontSize: '18px', fontWeight: '700', color: '#111827', margin: 0 }}>Plan Normal</h3>
                                    <p style={{ fontSize: '32px', fontWeight: '800', margin: '16px 0 4px 0' }}>0 lei <span style={{ fontSize: '14px', fontWeight: '400', color: '#6b7280' }}>/ permanent</span></p>
                                    <hr style={{ border: 'none', borderTop: '1px solid #e2e8f0', margin: '20px 0' }} />
                                    <ul style={{ listStyle: 'none', padding: 0, display: 'flex', flexDirection: 'column', gap: '14px', fontSize: '14px', color: '#4b5563' }}>
                                        <li><Check size={16} color="#0d9488" style={{ marginRight: '10px', verticalAlign: 'middle' }} /> Comision standard 10% pe închiriere</li>
                                        <li><Check size={16} color="#0d9488" style={{ marginRight: '10px', verticalAlign: 'middle' }} /> Suport tehnic prin email (48h)</li>
                                        <li style={{ color: '#9ca3af', textDecoration: 'line-through' }}><X size={16} color="#ef4444" style={{ marginRight: '10px', verticalAlign: 'middle' }} /> Insignă PRO pe profil</li>
                                        <li style={{ color: '#9ca3af', textDecoration: 'line-through' }}><X size={16} color="#ef4444" style={{ marginRight: '10px', verticalAlign: 'middle' }} /> Asigurare bunuri inclusă</li>
                                    </ul>
                                </div>
                                <button disabled={!user?.pro} onClick={() => alert("Ești deja pe planul de bază.")} style={{ marginTop: '36px', width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #cbd5e1', background: !user?.pro ? '#f1f5f9' : '#fff', color: !user?.pro ? '#94a3b8' : '#1e293b', fontWeight: '600', cursor: user?.pro ? 'pointer' : 'default', fontSize: '14px' }}>
                                    {!user?.pro ? 'Planul tău actual' : 'Revino la Normal'}
                                </button>
                            </div>

                            {/* PLAN PRO */}
                            <div style={{ border: '2px solid #0d9488', background: 'linear-gradient(180deg, #ffffff 0%, #f0fdfa 100%)', borderRadius: '16px', padding: '32px 24px', width: '280px', position: 'relative', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', boxShadow: '0 10px 15px -3px rgba(13, 148, 136, 0.1)' }}>
                                <div style={{ position: 'absolute', top: '-12px', left: '50%', transform: 'translateX(-50%)', background: '#0d9488', color: '#fff', padding: '4px 14px', borderRadius: '20px', fontSize: '11px', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                    <Zap size={10} fill="#fff" /> RECOMANDAT
                                </div>
                                <div>
                                    <h3 style={{ fontSize: '18px', fontWeight: '700', color: '#0f766e', margin: 0 }}>Rentix PRO</h3>
                                    <p style={{ fontSize: '32px', fontWeight: '800', margin: '16px 0 4px 0' }}>49 lei <span style={{ fontSize: '14px', fontWeight: '400', color: '#6b7280' }}>/ lună</span></p>
                                    <hr style={{ border: 'none', borderTop: '1px solid #ccfbf1', margin: '20px 0' }} />
                                    <ul style={{ listStyle: 'none', padding: 0, display: 'flex', flexDirection: 'column', gap: '14px', fontSize: '14px', color: '#1e293b' }}>
                                        <li><Check size={16} color="#0d9488" style={{ marginRight: '10px', verticalAlign: 'middle' }} /> <strong>Comision 0%</strong> la toate închirierile</li>
                                        <li><Check size={16} color="#0d9488" style={{ marginRight: '10px', verticalAlign: 'middle' }} /> Insignă coroană PRO pe profil</li>
                                        <li><Check size={16} color="#0d9488" style={{ marginRight: '10px', verticalAlign: 'middle' }} /> <strong>Asigurare inclusă</strong> pentru defecte/avarii</li>
                                        <li><Check size={16} color="#0d9488" style={{ marginRight: '10px', verticalAlign: 'middle' }} /> Suport Prioritar Chat 24/7</li>
                                    </ul>
                                </div>
                                <button
                                    onClick={handleSubscribeStripe}
                                    disabled={user?.pro}
                                    style={{ marginTop: '36px', width: '100%', padding: '12px', borderRadius: '8px', border: 'none', background: user?.pro ? '#94a3b8' : '#0d9488', color: '#fff', fontWeight: '600', cursor: user?.pro ? 'default' : 'pointer', fontSize: '14px' }}
                                >
                                    {user?.pro ? 'Abonament PRO Activ ✓' : 'Abonează-te prin Stripe'}
                                </button>
                            </div>
                        </div>
                    </div>
                );
            case 'cont':
            default:
                return (
                    <div className="tab-content-fade">
                        <section className="content-header">
                            <h2 className="tab-section-title">Datele contului</h2>
                        </section>

                        {!editMode ? (
                            <div className="content-grid">
                                <div className="info-card">
                                    <div className="card-body">
                                        <div className="avatar-large" style={{ position: 'relative' }}>
                                            {user.nume ? user.nume.charAt(0).toUpperCase() : 'U'}
                                            {user?.pro && (
                                                <div style={{ position: 'absolute', bottom: '0', right: '0', background: '#0d9488', color: '#fff', borderRadius: '50%', width: '22px', height: '22px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px solid #fff' }}>
                                                    <Crown size={12} fill="#fff" />
                                                </div>
                                            )}
                                        </div>
                                        <div className="info-details">
                                            <p style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                                <strong>Nume complet:</strong> {user.nume}
                                                {esteVerificat && <CheckCircle size={16} color="#0284c7" fill="#e0f2fe" title="Utilizator Verificat cu Buletinul" />}
                                            </p>
                                            <p><strong>Adresă Email:</strong> {user.email}</p>
                                            <p><strong>Număr Telefon:</strong> {user.telefon || "Nespecificat"}</p>
                                            <p><strong>Adresă Domiciliu:</strong> {user.adresa || "Nespecificată"}</p>
                                            <button className="edit-link" onClick={() => setEditMode(true)}>Modifică datele tale</button>
                                        </div>
                                    </div>

                                    {/* --- CASETA INTEGRATĂ PENTRU VERIFICARE BULETIN --- */}
                                    {/* Cadrul de mai jos a fost comentat pentru a trece peste verificare temporar

<div style={{ marginTop: '24px', padding: '18px', borderRadius: '12px', background: esteVerificat ? '#f0fdf4' : '#fff8f1', border: esteVerificat ? '1px solid #bbf7d0' : '1px solid #fed7aa' }}>
    <h4 style={{ margin: '0 0 8px 0', fontSize: '15px', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '8px', color: esteVerificat ? '#16a34a' : '#ea580c' }}>
        Verificare Buletin / ID {esteVerificat && <CheckCircle size={18} color="#22c55e" fill="#e0f2fe" />}
    </h4>
    {esteVerificat ? (
        <p style={{ color: '#16a34a', margin: 0, fontSize: '13.5px', lineHeight: '1.5' }}>
            Identitatea ta a fost securizată și confirmată prin **Stripe Identity**. Ai primit insigna de încredere și poți posta anunțuri pe platformă.
        </p>
    ) : (
        <div>
            <p style={{ color: '#c2410c', margin: '0 0 12px 0', fontSize: '13.5px', lineHeight: '1.5' }}>
                Pentru a menține comunitatea Rentix sigură și family-friendly, ai nevoie de verificarea oficială a buletinului înainte de a posta prima listare.
            </p>
            <button
                onClick={() => handleStartVerification(user?.id)}
                disabled={verifying}
                style={{ background: '#0d9488', color: 'white', border: 'none', padding: '9px 16px', borderRadius: '8px', cursor: verifying ? 'default' : 'pointer', fontWeight: '600', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
            >
                {verifying ? "Se inițializează..." : "Începe verificarea securizată"}
            </button>
        </div>
    )}
</div>*/}
<div style={{ marginTop: '24px', padding: '18px', borderRadius: '12px', background: '#f0fdf4', border: '1px solid #bbf7d0' }}>
    <p style={{ color: '#16a34a', margin: 0, fontSize: '13.5px', fontWeight: '600' }}>
        Mod Dezvoltare: Verificarea cu buletinul este dezactivată. Poți uploada anunțuri liber!
    </p>
</div>

                                </div>

                                <div className="status-card" style={{ background: user?.pro ? 'linear-gradient(135deg, #f0fdfa 0%, #ccfbf1 100%)' : '#f9fafb' }}>
                                    <div className="badge-genius" style={{ backgroundColor: user?.pro ? '#0d9488' : '#6b7280' }}>
                                        {user?.pro ? 'Rentix Pro' : 'Rentix Standard'}
                                    </div>
                                    <h3>{user?.pro ? 'Membru Premium' : 'Treci la PRO'}</h3>
                                    <p>{user?.pro ? 'Beneficiezi de asigurare gratuită la produse închiriate și asistență 24/7.' : 'Scapă de comisioane și deblochează unelte sau anunțuri nelimitate.'}</p>
                                    <button className="btn-action" onClick={() => handleTabChange('beneficii')}>Vezi beneficii</button>
                                </div>
                            </div>
                        ) : (
                            <form onSubmit={handleUpdateProfile} style={{ maxWidth: '450px', display: 'flex', flexDirection: 'column', gap: '16px', background: '#fff', padding: '24px', borderRadius: '12px', border: '1px solid #e5e7eb' }}>
                                <div>
                                    <label style={{ display: 'block', fontWeight: '600', marginBottom: '4px', fontSize: '14px' }}>Nume complet</label>
                                    <input type="text" value={nume} onChange={e => setNume(e.target.value)} style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #cbd5e1' }} required />
                                </div>
                                <div>
                                    <label style={{ display: 'block', fontWeight: '600', marginBottom: '4px', fontSize: '14px' }}>Număr Telefon</label>
                                    <input type="text" value={telefon} placeholder="07xx xxx xxx" onChange={e => setTelefon(e.target.value)} style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #cbd5e1' }} />
                                </div>
                                <div>
                                    <label style={{ display: 'block', fontWeight: '600', marginBottom: '4px', fontSize: '14px' }}>Adresă livrare</label>
                                    <input type="text" value={adresa} placeholder="Oraș, Str. ..." onChange={e => setAdresa(e.target.value)} style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #cbd5e1' }} />
                                </div>
                                <div style={{ display: 'flex', gap: '10px', marginTop: '8px' }}>
                                    <button type="submit" style={{ background: '#0d9488', color: '#fff', border: 'none', padding: '10px 20px', borderRadius: '6px', fontWeight: '600', cursor: 'pointer' }}>Salvează</button>
                                    <button type="button" onClick={() => setEditMode(false)} style={{ background: '#e5e7eb', color: '#1f2937', border: 'none', padding: '10px 20px', borderRadius: '6px', fontWeight: '600', cursor: 'pointer' }}>Anulează</button>
                                </div>
                            </form>
                        )}

                        <section className="content-header" style={{ marginTop: '40px' }}>
                            <h2 className="tab-section-title">Activitatea mea</h2>
                        </section>

                        <div className="stats-row">
                            <div className="stat-box" onClick={() => handleTabChange('comenzi')}>
                                <Package size={24} color="#0d9488" />
                                <div>
                                    <span>0 comenzi</span>
                                    <small>vezi istoric</small>
                                </div>
                            </div>
                            <div className="stat-box" onClick={() => handleTabChange('chat')}>
                                <MessageCircle size={24} color="#0d9488" />
                                <div>
                                    <span>Mesaje chat</span>
                                    <small>deschide chat</small>
                                </div>
                            </div>
                            <div className="stat-box">
                                <Star size={24} color="#f59e0b" />
                                <div>
                                    <span>0 recenzii</span>
                                    <small>calificative proprietar</small>
                                </div>
                            </div>
                        </div>
                    </div>
                );
        }
    };

    return (
        <div className="profile-page-wrapper">
            <div className="profile-layout-container">

                {/* SIDEBAR STÂNGA */}
                <aside className="profile-sidebar">
                    <div className="sidebar-header-user">
                        <div className="user-avatar-circle" style={{ position: 'relative' }}>
                            {user.nume ? user.nume.charAt(0).toUpperCase() : 'U'}
                            {user?.pro && (
                                <div style={{ position: 'absolute', bottom: '-2px', right: '-2px', background: '#0d9488', color: '#fff', borderRadius: '50%', width: '18px', height: '18px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px solid #fff' }}>
                                    <Crown size={10} fill="#fff" />
                                </div>
                            )}
                        </div>
                        <div className="user-sidebar-meta">
                            <h3 style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                {user.nume}
                                {user?.pro && <Crown size={14} color="#0d9488" fill="#0d9488" />}
                                {esteVerificat && <CheckCircle size={14} color="#0284c7" fill="#e0f2fe" title="Utilizator Verificat" />}
                            </h3>
                            <span className="status-online-indicator">
                                <span className="dot" style={{ backgroundColor: (connected || user?.id) ? '#22c55e' : '#ef4444' }}></span>
                                {(connected || user?.id) ? "Conectat" : "Deconectat"}
                            </span>
                        </div>
                    </div>

                    <nav className="sidebar-menu-items">
                        <button
                            className={`menu-btn ${activeTab === 'cont' ? 'active' : ''}`}
                            onClick={() => handleTabChange('cont')}
                        >
                            <User size={18} />
                            <span>Datele contului</span>
                        </button>

                        <button
                            className={`menu-btn ${activeTab === 'chat' ? 'active' : ''}`}
                            onClick={() => handleTabChange('chat')}
                        >
                            <MessageCircle size={18} />
                            <span>Mesajele mele</span>
                        </button>

                        <button
                            className={`menu-btn ${activeTab === 'comenzi' ? 'active' : ''}`}
                            onClick={() => handleTabChange('comenzi')}
                        >
                            <Package size={18} />
                            <span>Comenzile mele</span>
                        </button>

                        <button
                            className={`menu-btn ${activeTab === 'favorite' ? 'active' : ''}`}
                            onClick={() => handleTabChange('favorite')}
                        >
                            <Heart size={18} />
                            <span>Anunțuri favorite</span>
                        </button>

                        <button
                            className={`menu-btn ${activeTab === 'beneficii' ? 'active' : ''}`}
                            onClick={() => handleTabChange('beneficii')}
                            style={{ color: '#0d9488' }}
                        >
                            <Crown size={18} />
                            <span style={{ fontWeight: '700' }}>Rentix PRO</span>
                        </button>
                    </nav>

                    <div className="sidebar-footer-zone">
                        <button className="sidebar-logout-btn" onClick={handleLogout}>
                            <LogOut size={18} />
                            <span>Deconectare</span>
                        </button>
                    </div>
                </aside>

                {/* CONȚINUT DINAMIC DREAPTA */}
                <main className="profile-main-content">
                    {renderContent()}
                </main>

            </div>
        </div>
    );
};

export default Profile;
