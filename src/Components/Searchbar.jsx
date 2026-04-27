
import { useState } from "react";
import "./Searchbar.css";

export default function SearchBar() {
  const [inputText, setInputText] = useState("");

  function handleChange(event) {
    setInputText(event.target.value);
  }

  function handleKeyDown(event) {
    if (event.key === "Enter") {
      setInputText("");
    }
  }

  return (
    <div className="search-container">
      <button className="filter-button">Filtre</button>

      <input
        type="text"
        value={inputText}
        onChange={handleChange}
        onKeyDown={handleKeyDown}
        placeholder="Caută..."
        className="search-input"
      />
    </div>
  );
}