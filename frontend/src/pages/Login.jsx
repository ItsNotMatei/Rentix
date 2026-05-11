import React from 'react';
import { Link } from 'react-router-dom';

const Login = () => {
    return (
        <div className="login-container">
            <h2>Autentificare</h2>
            <form action="http://localhost:8080/login" method="POST">
                <div className="form-group">
                    <label>Email:</label>
                    <input type="email" name="username" required />
                </div>
                <div className="form-group">
                    <label>Parolă:</label>
                    <input type="password" name="password" required />
                </div>
                <button type="submit" className="btn-login">Intră în cont</button>
            </form>
            <p style={{ textAlign: 'center', marginTop: '15px' }}>
                Nu ai cont? <Link to="/signup">Înregistrează-te</Link>
            </p>
        </div>
    );
};

export default Login;