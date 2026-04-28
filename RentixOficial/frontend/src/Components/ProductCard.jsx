import "./ProductCard.css";

const ProductCard = ({ image, title, price, location }) => {
    return (
        <div className="product-card">
            <img src={image} alt={title} className="product-image" />

            <div className="product-content">
                <h3 className="product-title">{title}</h3>
                <p className="product-price">{price}</p>
                <p className="product-location">{location}</p>

                <button className="product-button">Vezi detalii</button>
            </div>
        </div>
    );
};

export default ProductCard;