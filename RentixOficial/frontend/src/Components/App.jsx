import Navbar from "./Navbar"
import SearchBar from "./Searchbar"
import ProductCard from "./ProductCard"

export  default function App() {
    return (
        <>
            <Navbar />
            <SearchBar />
            <div className="product-list">
                <ProductCard
                    image="https://via.placeholder.com/300"
                    title="Mașină de găurit profesională"
                    price="50 lei / zi"
                    location="București"
                />

                <ProductCard
                    image="https://via.placeholder.com/300"
                    title="Cort camping 4 persoane"
                    price="80 lei / zi"
                    location="Brașov"
                />
            </div>
        </>
    )
}