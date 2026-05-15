import React from 'react';
import { Check, X, Crown } from 'lucide-react';
import '../css/Benefits.css';

const Benefits = ({ isPro, onSubscribe }) => {
    return (
        <div className="benefits-page-container">
            <div className="benefits-header">
                <h2>Alege planul potrivit pentru tine</h2>
                <p>Maximizează-ți câștigurile și închiriază în siguranță pe Rentix</p>
            </div>

            <div className="pricing-grid">
                {/* Plan Normal */}
                <div className={`pricing-card ${!isPro ? 'active-plan' : ''}`}>
                    <h3>Plan Normal</h3>
                    <div className="price">0 lei <span>/永久</span></div>
                    <ul className="benefits-list">
                        <li><Check size={18} className="icon-check" /> Comision standard la închiriere</li>
                        <li><Check size={18} className="icon-check" /> Suport tehnic standard</li>
                        <li className="disabled"><X size={18} className="icon-x" /> Insignă PRO pe profil</li>
                        <li className="disabled"><X size={18} className="icon-x" /> Asigurare bunuri inclusă</li>
                    </ul>
                    <button className="plan-btn missing" disabled={!isPro}>
                        {!isPro ? 'Planul tău actual' : 'Rămâi la Normal'}
                    </button>
                </div>

                {/* Plan PRO */}
                <div className={`pricing-card pro-card ${isPro ? 'active-plan' : ''}`}>
                    <div className="crown-badge"><Crown size={16} /> RECOMANDAT</div>
                    <h3>Rentix PRO</h3>
                    <div className="price">49 lei <span>/ lună</span></div>
                    <ul className="benefits-list">
                        <li><Check size={18} className="icon-check" /> <strong>Comision 0%</strong> la listări</li>
                        <li><Check size={18} className="icon-check" /> Insignă **PRO** pe profil și anunțuri</li>
                        <li><Check size={18} className="icon-check" /> Asigurare gratuită la produse</li>
                        <li><Check size={18} className="icon-check" /> Suport prioritar 24/7</li>
                    </ul>
                    <button
                        className="plan-btn pro-btn"
                        onClick={() => onSubscribe('price_stripe_id_aici')}
                    >
                        {isPro ? 'Gestionare Abonament' : 'Abonează-te cu Stripe'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Benefits;