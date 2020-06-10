const app = new Vue({
    el: '#app',
    data: {
        ts: "never",
        count: {
            bus: 0,
            tram: 0
        }
    }
});

const socket = io();
socket.on('arrival-count', (msg) => {
    app.count[msg.vt] = msg.count;
    app.ts = new Date(msg.ts);
})
