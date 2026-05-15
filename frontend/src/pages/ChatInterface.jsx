import React, { useEffect, useRef } from 'react';
import { Send, Info } from 'lucide-react';
import "../css/ChatInterface.css";

const ChatInterface = ({ connected, messages, sendMessage, inputValue, setInputValue }) => {
    // Preluare sigură din localStorage
    const user = JSON.parse(localStorage.getItem("user")) || { nume: "Utilizator" };
    const scrollRef = useRef(null);

    // Auto-scroll optimizat la fiecare mesaj nou
    useEffect(() => {
        if (scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [messages]);

    // Înlocuim onKeyPress cu onKeyDown (standardul modern care nu blochează interfața)
    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && inputValue.trim() && connected) {
            sendMessage();
        }
    };

    return (
        <div className="ig-chat-container">
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

            <div className="ig-chat-main">
                <div className="chat-header">
                    <div className="header-info">
                        <div className="avatar-sm">R</div>
                        <span>Rentix Support</span>
                    </div>
                    <Info size={20} className="info-icon" />
                </div>

                <div className="messages-area" ref={scrollRef}>
                    {messages && messages.map((msg, index) => {
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
};

export default ChatInterface;