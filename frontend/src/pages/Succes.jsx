import React from 'react';
import { Link } from 'react-router-dom';

const Success = () => {
    return (
        <div style={{ textAlign: 'center', marginTop: '100px' }}>
            <h1>✅ Plată finalizată cu succes!</h1>
            <p>Rezervarea ta a fost confirmată.</p>
            <Link to="/" className="btn dark">Înapoi la Acasă</Link>
        </div>
    );
};

export default Success;