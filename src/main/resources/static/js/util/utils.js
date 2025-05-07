function saveToLocal(key, data) {
    localStorage.setItem(key, JSON.stringify(data));
}

function loadFromLocal(key) {
    const data = localStorage.getItem(key);
    return data ? JSON.parse(data) : null;
}