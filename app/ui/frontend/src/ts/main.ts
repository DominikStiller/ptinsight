import "leaflet-providers";
import { control, map as lmap, tileLayer } from "leaflet";
import * as socketio from "socket.io-client";
import GeocellLayer from "./geocell-layer";
import { LegendUi } from "./legend-ui";
import "./styles";

const legend = new LegendUi();

const vehicleCountsLayer = new GeocellLayer(
  "Vehicle Count",
  (data) => `Vehicle count in the last 30 s: ${data}`,
  legend,
  7000
);
const delayStatisticsLayer = new GeocellLayer(
  "Arrival Delay",
  (data) => `90th percentile arrival delay in the last 5 min: ${data} min`,
  legend,
  7000
);

var streetsLayer = tileLayer.provider("Stadia.AlidadeSmooth");

const map = lmap("map-container", {
  center: [60.2199, 24.9184],
  zoom: 11.7,
  layers: [streetsLayer, vehicleCountsLayer],
});

control
  .layers(
    {
      Streets: streetsLayer,
    },
    {
      "Vehicle count": vehicleCountsLayer,
      "Delay statistics": delayStatisticsLayer,
    },
    {
      position: "bottomright",
      collapsed: false,
    }
  )
  .addTo(map);

const socket = socketio();
socket.on("vehicle-count", (msg: any) => {
  if (msg.count >= 2) {
    vehicleCountsLayer.updateData(msg.geocell, msg.count);
  }
});

socket.on("delay-statistics", (msg: any) => {
  delayStatisticsLayer.updateData(msg.geocell, msg.p90);
});
