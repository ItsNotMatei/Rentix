import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import OfferCard from '../components/OfferCard';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import '../css/chat1.css';

const API = 'http://localhost:8080';

export default function Chat() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const cuUtilizatorId = searchParams.get('cuUtilizator');
    const produsId = searchParams.get('produs');

    const [user, setUser] = useState(null);
    const [conversations, setConversations] = useState([]);
    const [activeConversation, setActiveConversation] = useState(null);
    const [messages, setMessages] = useState([]);
    const [textMesaj, setTextMesaj] = useState('');
    const [loading, setLoading] = useState(true);
    const [typingUser, setTypingUser] = useState(null);
    const [unreadTotal, setUnreadTotal] = useState(0);
    const stompClient = useRef(null);
    const typingTimeout = useRef(null);

    useEffect(() => {
        const stored = localStorage.getItem('user');
        if (!stored) {
            navigate('/login');
            return;
        }
        setUser(JSON.parse(stored));
    }, [navigate]);

    const loadConversations = useCallback(async (userId) => {
        const res = await api.get(`/api/conversations`);
        setConversations(res.data);
        const unread = await api.get(`/api/conversations/unread-count`);
        setUnreadTotal(unread.data.count || 0);
    }, []);

    useEffect(() => {
        if (!user?.id) return;
        setLoading(true);
        loadConversations(user.id).finally(() => setLoading(false));
    }, [user?.id, loadConversations]);

    const connectStomp = useCallback((conversationId, currentUser) => {
        if (stompClient.current?.connected) {
            stompClient.current.disconnect();
        }
        const socket = new SockJS(`${API}/chat`);
        const client = Stomp.over(socket);
        client.debug = () => {};
        client.connect({}, () => {
            client.subscribe(`/topic/conversations/${conversationId}`, (msg) => {
                const body = JSON.parse(msg.body);
                if (body.type === 'TYPING') return;
                setMessages(prev => (prev.some(m => m.id === body.id) ? prev : [...prev, body]));
                if (body.senderId !== currentUser.id) {
                    api.patch(`/api/conversations/${conversationId}/read`);
                }
            });
            client.subscribe(`/topic/conversations/${conversationId}/typing`, (msg) => {
                const body = JSON.parse(msg.body);
                if (body.senderId !== currentUser.id) {
                    setTypingUser(body.senderName || 'Utilizator');
                    clearTimeout(typingTimeout.current);
                    typingTimeout.current = setTimeout(() => setTypingUser(null), 2000);
                }
            });
        });
        stompClient.current = client;
    }, []);

    const openConversation = useCallback(async (conversation, currentUser) => {
        setActiveConversation(conversation);
        const res = await api.get(`/api/conversations/${conversation.id}/messages`);
        setMessages(res.data);
        await api.patch(`/api/conversations/${conversation.id}/read`);
        loadConversations(currentUser.id);
        connectStomp(conversation.id, currentUser);
    }, [connectStomp, loadConversations]);

    useEffect(() => {
        const bootstrap = async () => {
            if (!user?.id || !cuUtilizatorId || !produsId) return;
            const created = await api.post(`/api/conversations`, {
                listingId: Number(produsId),
                otherUserId: Number(cuUtilizatorId)
            });
            const list = await api.get(`/api/conversations`);
            const found = list.data.find(c => c.id === created.data.id) || {
                id: created.data.id,
                listingId: Number(produsId),
                otherUserId: Number(cuUtilizatorId),
                otherUserName: 'Utilizator',
                listingTitle: 'Anunț'
            };
            await openConversation(found, user);
        };
        bootstrap();
    }, [user, cuUtilizatorId, produsId, openConversation]);

    useEffect(() => {
        return () => {
            if (stompClient.current?.connected) stompClient.current.disconnect();
        };
    }, []);

    const handleTrimiteMesaj = async (e) => {
        e.preventDefault();
        if (!textMesaj.trim() || !activeConversation || !user) return;

        const payload = { senderId: user.id, content: textMesaj.trim() };

        if (stompClient.current?.connected) {
            stompClient.current.send(
                `/app/conversations/${activeConversation.id}/send`,
                {},
                JSON.stringify(payload)
            );
        } else {
            const res = await api.post(
                `/api/conversations/${activeConversation.id}/messages`,
                payload
            );
            setMessages(prev => [...prev, res.data]);
        }

        setTextMesaj('');
        setTypingUser(null);
        loadConversations(user.id);
    };

    const handleTyping = () => {
        if (!stompClient.current?.connected || !activeConversation || !user) return;
        stompClient.current.send(
            `/app/conversations/${activeConversation.id}/typing`,
            {},
            JSON.stringify({ senderId: user.id, senderName: user.nume || 'Tu' })
        );
    };

    if (!user) return null;

    return (
        <div className="rentix-chat-page">
            <header className="rentix-chat-header">
                <Link to="/" className="chat-back-link">Acasă</Link>
                <h1>
                    Mesaje
                    {unreadTotal > 0 && <span className="chat-unread-badge">{unreadTotal}</span>}
                </h1>
            </header>

            <div className="rentix-chat-layout">
                <aside className="chat-sidebar">
                    {loading ? (
                        <p className="chat-loading">Se încarcă conversațiile...</p>
                    ) : conversations.length === 0 ? (
                        <p className="chat-empty">Nu ai conversații. Contactează un proprietar din pagina unui anunț.</p>
                    ) : (
                        conversations.map(c => (
                            <button
                                key={c.id}
                                type="button"
                                className={`chat-conv-item ${activeConversation?.id === c.id ? 'active' : ''}`}
                                onClick={() => openConversation(c, user)}
                            >
                                <div className="chat-conv-main">
                                    <strong>{c.otherUserName}</strong>
                                    <span className="chat-conv-listing">{c.listingTitle}</span>
                                    <span className="chat-conv-preview">{c.lastMessage || 'Fără mesaje'}</span>
                                </div>
                                {c.unreadCount > 0 && <span className="chat-conv-unread">{c.unreadCount}</span>}
                            </button>
                        ))
                    )}
                </aside>

                <section className="chat-main-panel">
                    {!activeConversation ? (
                        <div className="chat-placeholder">
                            <p>Selectează o conversație sau deschide chat-ul de pe un anunț.</p>
                        </div>
                    ) : (
                        <>
                            <div className="chat-panel-header">
                                <div>
                                    <h3>{activeConversation.otherUserName}</h3>
                                    <span>{activeConversation.listingTitle}</span>
                                </div>
                                <span className="chat-online-dot" title="Online" />
                            </div>

                            <div className="chat-messages-body">
                                {messages.map(m => {
                                    const reload = () => openConversation(activeConversation, user);
                                    if (m.messageType === 'OFFER' && m.offer) {
                                        return (
                                            <div key={m.id} className={`message-row offer-row ${m.senderId === user.id ? 'me' : 'other'}`}>
                                                <OfferCard offer={m.offer} onUpdate={reload} />
                                            </div>
                                        );
                                    }
                                    if (m.messageType === 'SYSTEM') {
                                        return <p key={m.id} className="chat-system-msg">{m.content}</p>;
                                    }
                                    return (
                                        <div
                                            key={m.id || `${m.senderId}-${m.createdAt}`}
                                            className={`message-row ${m.senderId === user.id ? 'me' : 'other'}`}
                                        >
                                            <div className="message-bubble">
                                                <p>{m.content}</p>
                                                {m.seen && m.senderId === user.id && <small>Văzut</small>}
                                            </div>
                                        </div>
                                    );
                                })}
                                {typingUser && <p className="chat-typing">{typingUser} scrie...</p>}
                            </div>

                            <form onSubmit={handleTrimiteMesaj} className="chat-input-footer">
                                <input
                                    type="text"
                                    value={textMesaj}
                                    onChange={(e) => {
                                        setTextMesaj(e.target.value);
                                        handleTyping();
                                    }}
                                    placeholder="Scrie un mesaj..."
                                />
                                <button type="submit">Trimite</button>
                            </form>
                        </>
                    )}
                </section>
            </div>
        </div>
    );
}