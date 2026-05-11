import React, { useState } from 'react';

const AdaugaAnunt = () => {
    const [isRental, setIsRental] = useState(true);

    return (
        <div className="form-container">
            <h1>Adaugă un anunț nou</h1>
            <form id="formAnunt">
                <div className="form-group">
                    <label>Titlu Anunț</label>
                    <input type="text" required />
                </div>

                <div className="form-group">
                    <label>Tip Anunț</label>
                    <div className="type-options">
                        <label className="radio-card">
                            <input type="radio" name="tip" checked={isRental} onChange={() => setIsRental(true)} />
                            <div className="card-content">Închiriază</div>
                        </label>
                        <label className="radio-card">
                            <input type="radio" name="tip" checked={!isRental} onChange={() => setIsRental(false)} />
                            <div className="card-content">Vinde</div>
                        </label>
                    </div>
                </div>

                {isRental ? (
                    <div className="form-row">
                        <div className="form-group"><label>Preț pe zi</label><input type="number" /></div>
                    </div>
                ) : (
                    <div className="form-group"><label>Preț Total</label><input type="number" /></div>
                )}

                <button type="submit" className="btn-submit">Publică Anunțul</button>
            </form>
        </div>
    );
};

export default AdaugaAnunt;