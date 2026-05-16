import React, { useState } from 'react';
import { createOffer } from '../services/paymentService';
import { useNavigate } from 'react-router-dom';

export default function MakeOfferModal({ product, onClose }) {
    const [amount, setAmount] = useState(product?.pret ? String(Math.floor(product.pret * 0.9)) : '');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const submit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const offer = await createOffer(product.id, parseFloat(amount), null);
            onClose();
            navigate(`/chat?cuUtilizator=${product.userId}&produs=${product.id}`);
        } catch (err) {
            alert(err.response?.data?.message || 'Nu s-a putut trimite oferta.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="offer-modal-overlay" onClick={onClose}>
            <div className="offer-modal" onClick={(e) => e.stopPropagation()}>
                <h3>Fă o ofertă</h3>
                <p className="offer-modal-sub">Preț listat: {product.pret} RON</p>
                <form onSubmit={submit}>
                    <label>Suma oferită (RON)</label>
                    <input
                        type="number"
                        min="1"
                        step="0.01"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                        required
                    />
                    <div className="offer-modal-actions">
                        <button type="button" onClick={onClose}>Anulează</button>
                        <button type="submit" disabled={loading}>
                            {loading ? 'Se trimite...' : 'Trimite oferta'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
