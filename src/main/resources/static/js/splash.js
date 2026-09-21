async function resolveNextPage() {
    const token = localStorage.getItem('inventoryToken');
    if (!token) { window.location.replace('/login'); return; }
    try {
        const response = await fetch('/api/products', { headers: { Authorization: `Bearer ${token}` } });
        if (!response.ok) throw new Error('Session expired');
        window.location.replace(localStorage.getItem('inventoryRole') === 'STAFF' ? '/sales' : '/dashboard');
    } catch (error) {
        localStorage.removeItem('inventoryToken');
        localStorage.removeItem('inventoryRole');
        window.location.replace('/login');
    }
}
setTimeout(resolveNextPage, 2600);