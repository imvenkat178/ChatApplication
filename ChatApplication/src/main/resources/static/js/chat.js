// Check if user is logged in
const currentUser = sessionStorage.getItem('username');
const credentials = sessionStorage.getItem('credentials');

if (!credentials || !currentUser) {
    alert('Please login first');
    window.location.href = '/login';
}

document.getElementById('logged-user').textContent = currentUser;

let stompClient = null;
let selectedUser = null;

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    const headers = {
        'username': currentUser
    };

    console.log('Connecting with username: ' + currentUser);

    stompClient.connect(headers, function(frame) {
        console.log('Connected as: ' + currentUser);
        document.getElementById('connection-status').textContent = 'Connected';
        document.getElementById('connection-status').classList.add('connected');

        // START LISTENER via HTTP call
        fetch('/api/start-listener', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username: currentUser})
        }).then(() => {
            console.log('Listener started for: ' + currentUser);
        });

        // Subscribe to topic-based destination instead of user queue
        stompClient.subscribe('/topic/messages.' + currentUser, function(message) {
            console.log('RAW MESSAGE:', message);
            const msg = JSON.parse(message.body);
            console.log('PARSED MESSAGE:', msg);

            if (msg.from === selectedUser || !selectedUser) {
                displayMessage(msg, 'received');
            } else {
                console.log('Message from ' + msg.from + ' (not currently selected)');
            }
        });

        loadUsers();
    }, function(error) {
        console.error('Connection error:', error);
        document.getElementById('connection-status').textContent = 'Disconnected';
    });
}

async function loadUsers() {
    try {
        const response = await fetch('/api/users?currentUser=' + currentUser);
        const users = await response.json();
        const usersList = document.getElementById('users-list');
        usersList.innerHTML = '';

        if (users.length === 0) {
            usersList.innerHTML = '<div class="empty-state">No other users</div>';
            return;
        }

        users.forEach(user => {
            const userDiv = document.createElement('div');
            userDiv.className = 'user-item';
            userDiv.innerHTML = `
                <span class="user-status online"></span>
                <span>${user}</span>
            `;
            userDiv.onclick = () => selectUser(user, userDiv);
            usersList.appendChild(userDiv);
        });
    } catch (error) {
        console.error('Error:', error);
    }
}

function selectUser(username, element) {
    selectedUser = username;
    document.querySelectorAll('.user-item').forEach(item => {
        item.classList.remove('active');
    });
    element.classList.add('active');
    document.getElementById('chat-with').textContent = `Chat with ${username}`;
    document.getElementById('message-input').disabled = false;
    document.getElementById('send-btn').disabled = false;
    document.getElementById('messages-container').innerHTML = '';
}

function sendMessage() {
    const input = document.getElementById('message-input');
    const messageContent = input.value.trim();

    if (!messageContent || !selectedUser || !stompClient) return;

    const message = {
        from: currentUser,
        to: selectedUser,
        message: messageContent,
        timestamp: new Date().toISOString()
    };

    stompClient.send('/app/chat.send', {}, JSON.stringify(message));

    displayMessage({
        from: currentUser,
        to: selectedUser,
        message: messageContent,
        timestamp: new Date().toISOString()
    }, 'sent');

    input.value = '';
}

function displayMessage(msg, type) {
    const container = document.getElementById('messages-container');
    const noSelection = container.querySelector('.no-selection');
    if (noSelection) noSelection.remove();

    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;

    const time = new Date(msg.timestamp).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
    });

    messageDiv.innerHTML = `
        <div class="message-sender">${type === 'sent' ? 'You' : msg.from}</div>
        <div class="message-content">${msg.message}</div>
        <div class="message-time">${time}</div>
    `;

    container.appendChild(messageDiv);
    container.scrollTop = container.scrollHeight;
}

function logout() {
    if (stompClient) stompClient.disconnect();
    sessionStorage.clear();
    window.location.href = '/login';
}

document.getElementById('send-btn').addEventListener('click', sendMessage);
document.getElementById('message-input').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') sendMessage();
});

connect();