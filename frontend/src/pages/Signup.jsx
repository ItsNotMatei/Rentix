import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';
import '../css/signup.css';
import { User, Mail, Lock, ArrowRight } from 'lucide-react'; // Iconițe pentru un look modern

const Signup = () => {
    const [userData, setUserData] = useState({ username: '', email: '', password: '' });
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSignup = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await authService.register(userData.username, userData.email, userData.password);
            alert("Cont creat cu succes! Te poți loga.");
            navigate('/login');
        } catch (error) {
            console.error("Eroare la signup:", error);
            alert(error.response?.data?.message || "Ceva nu a mers bine la înregistrare.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h2>Creează un cont</h2>
                    <p>Alătură-te comunității Rentix astăzi</p>
                </div>

                <form onSubmit={handleSignup} className="auth-form">
                    <div className="input-group">
                        <User size={20} className="input-icon" />
                        <input
                            type="text"
                            placeholder="Nume utilizator"
                            required
                            onChange={(e) => setUserData({...userData, username: e.target.value})}
                        />
                    </div>

                    <div className="input-group">
                        <Mail size={20} className="input-icon" />
                        <input
                            type="email"
                            placeholder="Adresa de email"
                            required
                            onChange={(e) => setUserData({...userData, email: e.target.value})}
                        />
                    </div>

                    <div className="input-group">
                        <Lock size={20} className="input-icon" />
                        <input
                            type="password"
                            placeholder="Parolă"
                            required
                            onChange={(e) => setUserData({...userData, password: e.target.value})}
                        />
                    </div>

                    <button type="submit" className="auth-submit-btn" disabled={loading}>
                        {loading ? "Se procesează..." : "Creează Cont"}
                        {!loading && <ArrowRight size={18} />}
                    </button>
                </form>

                <div className="auth-footer">
                    <p>Ai deja un cont? <Link to="/login">Conectează-te</Link></p>
                </div>
            </div>
        </div>
    );
};

export default Signup;