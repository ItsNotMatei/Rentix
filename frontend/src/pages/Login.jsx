import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';
import { LogIn, Lock, User, ArrowRight } from 'lucide-react'; // Iconițe pentru Login
import '../css/signup.css';
const Login = () => {
    const [loginData, setLoginData] = useState({ email: '', password: '' });
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const user = await authService.login(loginData.email, loginData.password);
            const roles = ['MODERATOR', 'ADMIN', 'SUPER_ADMIN'];
            if (roles.includes(user?.role)) {
                navigate('/admin');
            } else {
                navigate('/');
            }
        } catch (error) {
            console.error(error);
            alert("Email sau parolă greșită!");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h2>Bine ai revenit!</h2>
                    <p>Introdu datele pentru a intra în cont</p>
                </div>

                <form onSubmit={handleLogin} className="auth-form">
                    <div className="input-group">
                        <User size={20} className="input-icon" />
                        <input
                            type="email"
                            placeholder="Adresa de email"
                            required
                            onChange={(e) => setLoginData({...loginData, email: e.target.value})}
                        />
                    </div>

                    <div className="input-group">
                        <Lock size={20} className="input-icon" />
                        <input
                            type="password"
                            placeholder="Parolă"
                            required
                            onChange={(e) => setLoginData({...loginData, password: e.target.value})}
                        />
                    </div>

                    <button type="submit" className="auth-submit-btn" disabled={loading}>
                        {loading ? "Se verifică..." : "Conectează-te"}
                        {!loading && <LogIn size={18} />}
                    </button>
                </form>

                <div className="auth-footer">
                    <p>Nu ai un cont? <Link to="/signup">Înregistrează-te</Link></p>
                </div>
            </div>
        </div>
    );
};

export default Login;