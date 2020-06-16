class ExpiringMap extends Map {

    static Entity = class {
        constructor(data, duration){
            this.data = data
            this.expire = duration ? Date.now() + duration : false
        }

        get expired(){
            return this.expire ? this.expire <= Date.now() : false
        }
    }

	set(key, value, duration){
		var entity = new ExpiringMap.Entity(value, duration)
		super.set(key, entity)
		if(duration) setTimeout(key => {
			const o = super.get(key)
			if(o && o.expired) {
			    // Check is necessary since value might have been updated with newer expiratino date
			    this.delete(key);
            }
		}, duration, key)
	}

	get(key){
		var entity = super.get(key);
		return entity === undefined || entity.expired ? undefined : entity.data;
	}

	delete(key){
		super.delete(key)
	}
}




const app = new Vue({
    el: '#app',
    data: {

    }
});
let counts = new ExpiringMap();
let polygons = new Map();

const map = L.map('map-container').setView([60.1699, 24.9384], 11);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19,
}).addTo(map);

const socket = io();
socket.on('vehicle-count', (msg) => {
    counts.set(msg.geocell, msg.count, 10000);
    updateMap();
})

let updateMap = _.debounce(() => {
    for (let key of polygons.keys()) {
        if (!counts.has(key)) {
            polygons.get(key).removeFrom(map)
            polygons.delete(key);
        }
    }

    counts.forEach((count, index) => {
        let style = {
            stroke: false,
            color: "red"
        };

        if (polygons.has(index)) {
            polygons.get(index).setStyle(style);
        } else {
            polygons.set(index, L.polygon(h3.h3ToGeoBoundary(index), style).addTo(map))
        }
    })
}, 1000);
