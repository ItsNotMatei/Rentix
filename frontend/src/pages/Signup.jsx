import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Signup = () => {
    const [formData, setFormData] = useState({ nume: '', email: '', password: '' });
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        const response = await fetch('/api/auth/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            alert("Cont creat cu succes!");
            navigate('/login');
        } else {
            alert("Eroare la înregistrare.");
        }
    };

    return (
        <div className="auth-container">
            <h2>Creează un cont</h2>
            <form onSubmit={handleSubmit}>
                <div className="input-group">
                    <label>Nume complet:</label>
                    <input type="text" onChange={(e) => setFormData({...formData, nume: e.target.value})} required />
                </div>
                <div className="input-group">
                    <label>Email:</label>
                    <input type="email" onChange={(e) => setFormData({...formData, email: e.target.value})} required />
                </div>
                <div className="input-group">
                    <label>Parolă:</label>
                    <input type="password" onChange={(e) => setFormData({...formData, password: e.target.value})} required />
                </div>
                <button type="submit" className="btn-submit">Înregistrare</button>
            </form>
        </div>
    );
};

export default Signup;