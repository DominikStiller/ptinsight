import * as leaflet from "leaflet";
import * as socketio from "socket.io-client";
import "./styles";
import GeocellLayer from "./geocell-layer";
import LegendUi from "./legend-ui";

const map = leaflet.map("map-container").setView([60.1699, 24.9384], 11);
leaflet
  .tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution:
      '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19,
  })
  .addTo(map);
const legend = new LegendUi();

const vehicleCountsLayer = new GeocellLayer(map, legend, 7000);

const socket = socketio();
socket.on("vehicle-count", (msg: any) => {
  if (msg.count >= 2) {
    vehicleCountsLayer.updateData(msg.geocell, msg.count);
  }
});
