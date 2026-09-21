document.getElementById('registerForm').addEventListener('submit', async function (event) {
    event.preventDefault();
    const message = document.getElementById('registerMessage');
    message.className = 'small mt-3 d-none';
    try {
        const response = await fetch('/api/auth/register', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ fullName: document.getElementById('fullName').value.trim(), email: document.getElementById('email').value.trim(), username: document.getElementById('username').value.trim(), password: document.getElementById('password').value, role: document.getElementById('role').value }) });
        if (!response.ok) throw new Error(await response.text() || 'Registration failed');
        message.textContent = 'Account created. Redirecting to sign in...';
        message.className = 'text-success small mt-3';
        setTimeout(() => window.location.href = '/login', 800);
    } catch (error) {
        message.textContent = error.message;
        message.className = 'text-danger small mt-3';
    }
});