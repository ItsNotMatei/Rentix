// În Checkout.jsx (sus de tot)
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom'; // Pentru a lua ID-ul produsului din URL și pentru redirecționare
import axios from 'axios'; // Pentru a apela backend-ul tău în Spring Boot (ex: crearea Payment Intent-ului)

import '../css/checkout.css'; // Fișierul tău de stilizare

export default function Checkout() {
    const { id } = useParams(); // 'id' va fi ID-ul produsului trimis din pagina de detalii
    const [product, setProduct] = useState(null);

    useEffect(() => {
        // Luăm detaliile produsului de pe backend ca să afișăm prețul corect la plată
        axios.get(`http://localhost:8080/api/products/${id}`)
            .then(res => setProduct(res.data))
            .catch(err => console.error(err));
    }, [id]);

    return (
        <div className="checkout-container">
            <h2>Finalizare Plată</h2>
            {product && (
                <div>
                    <p>Plătești pentru: <strong>{product.titlu}</strong></p>
                    <p>Sumă de plată: <strong>{product.pret} RON</strong></p>
                </div>
            )}
            {/* Aici vine formularul tău de card Stripe */}
        </div>
    );
}