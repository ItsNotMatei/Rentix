import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios'; // Folosim axios pentru un cod mai curat la upload
import '../css/adauga.css';

export default function AdaugaAnunt() {
    const [titlu, setTitlu] = useState('');
    const [pret, setPret] = useState('');
    const [descriere, setDescriere] = useState('');
    const [adresa, setAdresa] = useState('');
    const [tip, setTip] = useState('Închiriere');
    const [imagine, setImagine] = useState(null);
    const [previewUrl, setPreviewUrl] = useState('');
    const [uploading, setUploading] = useState(false); // Stare pentru animația de încărcare
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    // --- PROTECȚIE ȘI VERIFICARE BULETIN ---
    useEffect(() => {
        const storedUser = localStorage.getItem("user");
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);
            setUser(parsedUser);

            // MOD DEZVOLTARE: Am comentat blocarea pentru a putea adăuga anunțuri fără buletin
            /*
            if (!parsedUser.isVerified) {
                alert("Trebuie să îți verifici identitatea cu buletinul în pagina de Profil înainte de a putea posta un anunț!");
                navigate('/profile?tab=cont');
            }
            */
        } else {
            navigate('/login');
        }
    }, [navigate]);

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setImagine(file);
            setPreviewUrl(URL.createObjectURL(file));
        }
    };

    // --- FUNCȚIA DE UPLOAD ÎN CLOUDINARY ---
    const uploadImageToCloudinary = async () => {
        if (!imagine) return null;

        const formData = new FormData();
        formData.append("file", imagine);
        formData.append("upload_preset", "rentix_presets"); // Înlocuiește cu numele presetului tău dacă diferă

        try {
            setUploading(true);
            // ⚠️ ÎNLOCUIEȘTE 'numele_tau_de_cloud' cu Cloud Name-ul tău real din Cloudinary Dashboard
            const res = await axios.post(
                "https://api.cloudinary.com/v1_1/dn2hvsk0o/image/upload",
                formData
            );
            setUploading(false);
            return res.data.secure_url; // Returnează URL-ul securizat (https://res.cloudinary.com/...)
        } catch (err) {
            console.error("Eroare upload Cloudinary:", err);
            setUploading(false);
            alert("Nu s-a putut încărca imaginea în Cloudinary.");
            return null;
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!titlu || !pret || !adresa) {
            alert("Titlul, prețul și locația sunt obligatorii!");
            return;
        }

        // 1. Trimitem mai întâi poza în Cloudinary și așteptăm URL-ul
        let uploadedImageUrl = "";
        if (imagine) {
            uploadedImageUrl = await uploadImageToCloudinary();
            if (!uploadedImageUrl) return; // Oprim procesul dacă upload-ul imaginii a eșuat
        }

        // 2. Construim obiectul JSON curat pentru backend-ul Java
        const listingData = {
            titlu: titlu,
            pret: parseFloat(pret),
            descriere: descriere,
            adresa: adresa,
            tip: tip,
            userId: user ? user.id : 2,
            status: 'AVAILABLE',
            imagineUrl: uploadedImageUrl // <--- Trimitem URL-ul primit de la Cloudinary
        };

        try {
            // Trimitem datele ca aplicație/json simplă, fără FormData complicat
            const response = await fetch("http://localhost:8080/api/products", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(listingData)
            });

            if (response.ok) {
                localStorage.removeItem("anunturi");
                alert("Anunțul a fost publicat cu succes!");
                navigate('/');
            } else {
                const errorText = await response.text();
                alert("Eroare de la server: " + errorText);
            }
        } catch (error) {
            console.error("Eroare la conectare:", error);
            alert("Eroare la salvare. Verifică dacă serverul Java este pornit.");
        }
    };

    return (
        <div className="adauga-anunt-container">
            <h2>Adaugă un anunț nou</h2>
            <form onSubmit={handleSubmit} className="adauga-form">

                <div className="form-group">
                    <label>Încarcă fotografie din dispozitiv:</label>
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleFileChange}
                        className="file-input"
                    />

                    {previewUrl && (
                        <div className="image-preview">
                            <p>Previzualizare fotografie:</p>
                            <img src={previewUrl} alt="Preview" style={{ maxWidth: '200px', borderRadius: '8px', marginTop: '10px' }} />
                        </div>
                    )}
                </div>

                <div className="form-group">
                    <label>Titlu anunț:</label>
                    <input
                        type="text"
                        value={titlu}
                        onChange={(e) => setTitlu(e.target.value)}
                        placeholder="Ex: Stander moto profesional / Hanorac Balenciaga"
                    />
                </div>

                <div className="form-group">
                    <label>Descriere:</label>
                    <textarea
                        value={descriere}
                        onChange={(e) => setDescriere(e.target.value)}
                        placeholder="Oferă detalii despre produs, starea lui și condiții..."
                    />
                </div>

                <div className="form-group">
                    <label>Adresă / Locație:</label>
                    <input
                        type="text"
                        value={adresa}
                        onChange={(e) => setAdresa(e.target.value)}
                        placeholder="Ex: Valea Adâncă, RO"
                    />
                </div>

                <div className="form-group">
                    <label>Tip anunț:</label>
                    <select value={tip} onChange={(e) => setTip(e.target.value)}>
                        <option value="Închiriere">Închiriere</option>
                        <option value="Vânzare">Vânzare</option>
                    </select>
                </div>

                <div className="form-group">
                    <label>Preț total (RON / zi):</label>
                    <input
                        type="number"
                        value={pret}
                        onChange={(e) => setPret(e.target.value)}
                        placeholder="0.00"
                    />
                </div>

                <button type="submit" className="submit-btn" disabled={uploading}>
                    {uploading ? "Se încarcă imaginea..." : "Publică anunțul"}
                </button>
            </form>
        </div>
    );
}