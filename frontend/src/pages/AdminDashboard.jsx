import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api, { getStoredUser, hasRole } from '../services/api';
import authService from '../services/authService';
import '../css/admin.css';

const TABS = [
    { id: 'overview', label: 'Statistici' },
    { id: 'users', label: 'Utilizatori', min: 'ADMIN' },
    { id: 'listings', label: 'Anunțuri' },
    { id: 'reviews', label: 'Recenzii' },
    { id: 'reports', label: 'Rapoarte' },
    { id: 'bookings', label: 'Rezervări' },
    { id: 'chat', label: 'Chat' }
];

export default function AdminDashboard() {
    const navigate = useNavigate();
    const user = getStoredUser();
    const [tab, setTab] = useState('overview');
    const [stats, setStats] = useState(null);
    const [users, setUsers] = useState([]);
    const [listings, setListings] = useState([]);
    const [reviews, setReviews] = useState([]);
    const [reports, setReports] = useState([]);
    const [bookings, setBookings] = useState([]);
    const [conversations, setConversations] = useState([]);
    const [search, setSearch] = useState('');
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');

    const visibleTabs = TABS.filter(t => !t.min || hasRole(t.min));

    useEffect(() => {
        loadTab(tab);
    }, [tab]);

    const loadTab = async (active) => {
        setLoading(true);
        setMessage('');
        try {
            if (active === 'overview') {
                const res = await api.get('/api/admin/stats');
                setStats(res.data);
            } else if (active === 'users') {
                const res = await api.get(`/api/admin/users?q=${encodeURIComponent(search)}&page=0&size=50`);
                setUsers(res.data.content || []);
            } else if (active === 'listings') {
                const res = await api.get('/api/admin/listings');
                setListings(res.data);
            } else if (active === 'reviews') {
                const res = await api.get('/api/admin/reviews');
                setReviews(res.data);
            } else if (active === 'reports') {
                const res = await api.get('/api/admin/reports');
                setReports(res.data);
            } else if (active === 'bookings') {
                const res = await api.get('/api/admin/bookings');
                setBookings(res.data);
            } else if (active === 'chat') {
                const res = await api.get('/api/admin/conversations');
                setConversations(res.data);
            }
        } catch (err) {
            setMessage(err.response?.data?.message || 'Eroare la încărcare.');
        } finally {
            setLoading(false);
        }
    };

    const handleBan = async (id) => {
        await api.patch(`/api/admin/users/${id}/ban`, { reason: 'Încălcare reguli platformă' });
        loadTab('users');
    };

    const handleSuspend = async (id) => {
        await api.patch(`/api/admin/users/${id}/suspend`, { days: 7, reason: 'Suspendare 7 zile' });
        loadTab('users');
    };

    const handleUnban = async (id) => {
        await api.patch(`/api/admin/users/${id}/unban`);
        loadTab('users');
    };

    const handleRole = async (id, role) => {
        if (!hasRole('SUPER_ADMIN')) return;
        await api.patch(`/api/admin/users/${id}/role`, { role });
        loadTab('users');
    };

    const handleDeleteListing = async (id) => {
        await api.delete(`/api/admin/listings/${id}`);
        loadTab('listings');
    };

    const handleDeleteReview = async (id) => {
        await api.delete(`/api/admin/reviews/${id}`);
        loadTab('reviews');
    };

    const handleResolveReport = async (id) => {
        await api.patch(`/api/admin/reports/${id}/resolve`);
        loadTab('reports');
    };

    const handleLogout = async () => {
        await authService.logout();
        navigate('/login');
    };

    return (
        <div className="admin-page">
            <header className="admin-header">
                <div>
                    <Link to="/">← Rentix</Link>
                    <h1>Admin Panel</h1>
                    <span className="admin-role-badge">{user?.role}</span>
                </div>
                <button type="button" onClick={handleLogout} className="admin-logout-btn">Logout</button>
            </header>

            <nav className="admin-tabs">
                {visibleTabs.map(t => (
                    <button
                        key={t.id}
                        type="button"
                        className={tab === t.id ? 'active' : ''}
                        onClick={() => setTab(t.id)}
                    >
                        {t.label}
                    </button>
                ))}
            </nav>

            {message && <p className="admin-alert">{message}</p>}
            {loading && <p className="admin-loading">Se încarcă...</p>}

            <main className="admin-content">
                {tab === 'overview' && stats && (
                    <div className="admin-stats-grid">
                        {Object.entries(stats).map(([key, val]) => (
                            <div key={key} className="admin-stat-card">
                                <span>{key}</span>
                                <strong>{val}</strong>
                            </div>
                        ))}
                    </div>
                )}

                {tab === 'users' && hasRole('ADMIN') && (
                    <>
                        <div className="admin-toolbar">
                            <input
                                placeholder="Caută email sau nume..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                            />
                            <button type="button" onClick={() => loadTab('users')}>Caută</button>
                        </div>
                        <table className="admin-table">
                            <thead>
                                <tr>
                                    <th>ID</th><th>Nume</th><th>Email</th><th>Rol</th><th>Status</th><th>Acțiuni</th>
                                </tr>
                            </thead>
                            <tbody>
                                {users.map(u => (
                                    <tr key={u.id}>
                                        <td>{u.id}</td>
                                        <td>{u.nume}</td>
                                        <td>{u.email}</td>
                                        <td>{u.role}</td>
                                        <td>{u.banned ? 'BAN' : u.suspended ? 'SUSPEND' : 'OK'}</td>
                                        <td className="admin-actions">
                                            <button type="button" onClick={() => handleBan(u.id)}>Ban</button>
                                            <button type="button" onClick={() => handleSuspend(u.id)}>Suspend</button>
                                            <button type="button" onClick={() => handleUnban(u.id)}>Unban</button>
                                            {hasRole('SUPER_ADMIN') && (
                                                <>
                                                    <button type="button" onClick={() => handleRole(u.id, 'MODERATOR')}>Mod</button>
                                                    <button type="button" onClick={() => handleRole(u.id, 'ADMIN')}>Admin</button>
                                                    <button type="button" onClick={() => handleRole(u.id, 'USER')}>User</button>
                                                </>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </>
                )}

                {tab === 'listings' && (
                    <table className="admin-table">
                        <thead><tr><th>ID</th><th>Titlu</th><th>Preț</th><th>Owner</th><th></th></tr></thead>
                        <tbody>
                            {listings.map(l => (
                                <tr key={l.id}>
                                    <td>{l.id}</td>
                                    <td>{l.titlu}</td>
                                    <td>{l.pret}</td>
                                    <td>{l.userId}</td>
                                    <td><button type="button" onClick={() => handleDeleteListing(l.id)}>Șterge</button></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}

                {tab === 'reviews' && (
                    <table className="admin-table">
                        <thead><tr><th>ID</th><th>Rating</th><th>Comentariu</th><th></th></tr></thead>
                        <tbody>
                            {reviews.map(r => (
                                <tr key={r.id}>
                                    <td>{r.id}</td>
                                    <td>{r.rating}★</td>
                                    <td>{r.comment}</td>
                                    <td><button type="button" onClick={() => handleDeleteReview(r.id)}>Șterge</button></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}

                {tab === 'reports' && (
                    <table className="admin-table">
                        <thead><tr><th>ID</th><th>Tip</th><th>Target</th><th>Status</th><th>Motiv</th><th></th></tr></thead>
                        <tbody>
                            {reports.map(r => (
                                <tr key={r.id}>
                                    <td>{r.id}</td>
                                    <td>{r.type}</td>
                                    <td>{r.targetId}</td>
                                    <td>{r.status}</td>
                                    <td>{r.reason}</td>
                                    <td>
                                        {r.status === 'OPEN' && (
                                            <button type="button" onClick={() => handleResolveReport(r.id)}>Rezolvă</button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}

                {tab === 'bookings' && (
                    <table className="admin-table">
                        <thead><tr><th>ID</th><th>Status</th><th>Start</th><th>End</th></tr></thead>
                        <tbody>
                            {bookings.map(b => (
                                <tr key={b.id}>
                                    <td>{b.id}</td>
                                    <td>{b.status}</td>
                                    <td>{b.startDate}</td>
                                    <td>{b.endDate}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}

                {tab === 'chat' && (
                    <table className="admin-table">
                        <thead><tr><th>ID</th><th>Listing</th><th>Participanți</th></tr></thead>
                        <tbody>
                            {conversations.map(c => (
                                <tr key={c.id}>
                                    <td>{c.id}</td>
                                    <td>{c.listingId}</td>
                                    <td>{c.participantOneId} ↔ {c.participantTwoId}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </main>
        </div>
    );
}
