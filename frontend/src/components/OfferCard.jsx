import React from 'react';
import { acceptOffer, rejectOffer, counterOffer, payAcceptedOffer } from '../services/paymentService';
import { getStoredUser } from '../services/api';

export default function OfferCard({ offer, onUpdate }) {
    const user = getStoredUser();
    const data = offer?.offer || offer;
    if (!data) return null;

    const isSeller = user?.id === data.sellerId;
    const isBuyer = user?.id === data.buyerId;
    const status = data.status;

    const handleAccept = async () => {
        await acceptOffer(data.id);
        onUpdate?.();
    };

    const handleReject = async () => {
        await rejectOffer(data.id);
        onUpdate?.();
    };

    const handlePay = async () => {
        const res = await payAcceptedOffer(data.id);
        if (res.url) window.location.href = res.url;
    };

    const handleCounter = async () => {
        const amount = prompt('Introdu contra-oferta (RON):');
        if (!amount) return;
        await counterOffer(data.id, parseFloat(amount));
        onUpdate?.();
    };

    return (
        <div className={`offer-card offer-${status?.toLowerCase()}`}>
            <div className="offer-card-header">
                <span className="offer-amount">{data.amount} RON</span>
                <span className={`offer-status-badge status-${status}`}>{status}</span>
            </div>
            <div className="offer-card-actions">
                {isSeller && status === 'PENDING' && (
                    <>
                        <button type="button" className="btn-accept" onClick={handleAccept}>Acceptă</button>
                        <button type="button" className="btn-reject" onClick={handleReject}>Refuză</button>
                        <button type="button" className="btn-counter" onClick={handleCounter}>Contra-ofertă</button>
                    </>
                )}
                {isBuyer && status === 'ACCEPTED' && (
                    <button type="button" className="btn-pay" onClick={handlePay}>Plătește acum</button>
                )}
            </div>
        </div>
    );
}
