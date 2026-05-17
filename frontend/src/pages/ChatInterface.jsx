import React, { useState, useEffect, useRef } from 'react';
import { Send, Info } from 'lucide-react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import "../css/ChatInterface.css";
import { getStoredUser } from '../services/api';

export default function ChatInterface() {
    const user = getStoredUser() || { nume: "Utilizator" };

    // Stările interne pentru mesaje, input și starea conexiunii
    const [messages, setMessages] = useState([]);
    const [inputValue, setInputValue] = useState('');
    const [connected, setConnected] = useState(false);

    const scrollRef = useRef(null);
    const stompClientRef = useRef(null);

    // Inițializare și gestionare conexiune WebSocket
    useEffect(() => {
        const socket = new SockJS('/chat');

        const client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            onConnect: () => {
                setConnected(true);
                // Subscriere la canalul de mesaje
                client.subscribe('/topic/messages', (msg) => {
                    const body = JSON.parse(msg.body);
                    setMessages((prev) => [...prev, body]);
                });
            },
            onDisconnect: () => {
                setConnected(false);
            }
        });

        client.activate();
        stompClientRef.current = client;

        // Cleanup la demontarea componentei
        return () => {
            if (stompClientRef.current) {
                stompClientRef.current.deactivate();
            }
        };
    }, []);

    // Auto-scroll optimizat la fiecare mesaj nou
    useEffect(() => {
        if (scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [messages]);

    // Funcția de trimitere mesaj prin STOMP
    const sendMessage = () => {
        if (stompClientRef.current && stompClientRef.current.connected && inputValue.trim()) {
            stompClientRef.current.publish({
                destination: '/app/send',
                body: JSON.stringify({
                    senderId: user.nume, // Folosim user.nume pentru a se potrivi cu logica isMine din interfață
                    content: inputValue.trim()
                })
            });
            setInputValue('');
        }
    };

    // Gestionare trimitere la apăsarea tastei Enter
    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && inputValue.trim() && connected) {
            sendMessage();
        }
    };

    return (
        <div className="ig-chat-container">
            {/* SIDEBAR: Informații utilizator și listă conversații */}
            <div className="ig-chat-sidebar">
                <div className="sidebar-header">
                    <h4>{user.nume}</h4>
                    <span className={`status-text ${connected ? 'online' : 'offline'}`}>
                        {connected ? '● Online' : '○ Offline'}
                    </span>
                </div>
                <div className="conversations-list">
                    <div className="conv-item active">
                        <div className="avatar">R</div>
                        <div className="conv-text">
                            <span className="name">Rentix Support</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* ZONA PRINCIPALĂ: Fereastra de chat */}
            <div className="ig-chat-main">
                <div className="chat-header">
                    <div className="header-info">
                        <div className="avatar-sm">R</div>
                        <span>Rentix Support</span>
                    </div>
                    <Info size={20} className="info-icon" />
                </div>

                {/* Zona de afișare a mesajelor */}
                <div className="messages-area" ref={scrollRef}>
                    {messages && messages.map((msg, index) => {
                        // Verificăm dacă mesajul este trimis de utilizatorul curent
                        const isMine = msg.senderId === user.nume;

                        return (
                            <div key={index} className={`msg-wrapper ${isMine ? 'mine' : 'theirs'}`}>
                                {!isMine && (
                                    <div className="avatar-sm-chat">
                                        {msg.senderId ? msg.senderId.charAt(0).toUpperCase() : '?'}
                                    </div>
                                )}
                                <div className="msg-bubble">
                                    {msg.content}
                                </div>
                            </div>
                        );
                    })}
                </div>

                {/* Zona de introducere text */}
                <div className="chat-input-container">
                    <div className="input-wrapper">
                        <Send size={18} className="input-icon" />
                        <input
                            type="text"
                            placeholder={connected ? "Trimite un mesaj..." : "Se conectează la chat..."}
                            value={inputValue}
                            disabled={!connected}
                            onChange={(e) => setInputValue(e.target.value)}
                            onKeyDown={handleKeyDown}
                        />
                        <button
                            onClick={sendMessage}
                            className={inputValue && connected ? 'active' : ''}
                            disabled={!connected || !inputValue.trim()}
                        >
                            Trimite
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}