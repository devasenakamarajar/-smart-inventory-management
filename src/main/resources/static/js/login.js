document.getElementById('passwordToggle').addEventListener('click', function () {
    const password = document.getElementById('password');
    const visible = password.type === 'text';
    password.type = visible ? 'password' : 'text';
    this.innerHTML = visible ? '&#9673;' : '&#9675;';
    this.setAttribute('aria-label', visible ? 'Show password' : 'Hide password');
});

document.getElementById('loginForm').addEventListener('submit', async function (event) {
    event.preventDefault();
    const error = document.getElementById('loginError');
    error.classList.add('d-none');
    try {
        const response = await fetch('/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username: document.getElementById('username').value.trim(), password: document.getElementById('password').value }) });
        if (!response.ok) throw new Error('Invalid username or password');
        const data = await response.json();
        localStorage.setItem('inventoryToken', data.token);
        localStorage.setItem('inventoryRole', data.role || 'STAFF');
        localStorage.setItem('inventoryUsername', data.username || document.getElementById('username').value.trim());
        window.location.href = '/splash';
    } catch (loginError) {
        error.textContent = loginError.message;
        error.classList.remove('d-none');
    }
});