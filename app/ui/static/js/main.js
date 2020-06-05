const app = new Vue({
    el: '#app',
    data: {
        count: 0
    }
});

const socket = io();
socket.on('arrival-count', (msg) => {
    app.count = msg.count
})
