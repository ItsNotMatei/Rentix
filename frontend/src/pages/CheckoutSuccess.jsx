import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import api from '../services/api';

export default function CheckoutSuccess() {
    const [searchParams] = useSearchParams();
    const orderId = searchParams.get('orderId');
    const [order, setOrder] = useState(null);

    useEffect(() => {
        if (!orderId) return;
        api.get('/api/payments/orders').then(res => {
            const found = res.data.find(o => String(o.id) === String(orderId));
            setOrder(found);
        });
    }, [orderId]);

    return (
        <div className="checkout-success-page">
            <h1>Plată inițiată cu succes</h1>
            <p>Banii sunt ținuți în escrow până confirmi primirea produsului.</p>
            {order && (
                <p>Status comandă: <strong>{order.escrowStatus}</strong></p>
            )}
            <Link to="/profile?tab=comenzi" className="btn-primary-link">Vezi comenzile mele</Link>
            <Link to="/" className="btn-secondary-link">Înapoi acasă</Link>
        </div>
    );
}
