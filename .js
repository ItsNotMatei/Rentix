const API_URL = "http://localhost:8080/auth";

// REGISTER
function register() {
    const user = {
        username: document.getElementById("username").value,
        email: document.getElementById("email").value,
        password: document.getElementById("password").value
    };

    fetch(API_URL + "/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(user)
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        window.location.href = "login.html";
    })
    .catch(error => console.error(error));
}

// LOGIN
function login() {
    const user = {
        email: document.getElementById("email").value,
        password: document.getElementById("password").value
    };

    fetch(API_URL + "/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(user)
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        // aici poți redirecționa spre home
    })
    .catch(error => console.error(error));
}