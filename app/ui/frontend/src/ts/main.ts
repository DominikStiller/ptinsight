import "leaflet-providers";
import { control, map as lmap, tileLayer } from "leaflet";
import * as socketio from "socket.io-client";
import GeocellLayer from "./geocell-layer";
import GeoedgeLayer from "./geoedge-layer";
import { LegendUi } from "./legend-ui";
import "./styles";

const socket = socketio();
const legend = new LegendUi();

// Vehicle counts layer
const vehicleCountsLayer = new GeocellLayer(
  "Vehicle Count",
  (data) => `Vehicles in the last 30 s: ${data}`
).addToLegend(legend);
socket.on("vehicle-count", (msg: any) => {
  vehicleCountsLayer.updateData(msg.geocell, msg.count);
});

// Delay statistics layer
const delayStatisticsLayer = new GeocellLayer<{
  p50: number;
  p90: number;
  p99: number;
}>(
  "Arrival Delay",
  (data) =>
    `Arrival delay in the last 5 min (50th percentile): ${data.p50} min<br>
      Arrival delay in the last 5 min (90th percentile): ${data.p90} min<br>
      Arrival delay in the last 5 min (99th percentile): ${data.p99} min`,
  (data) => data.p90
).addToLegend(legend);
socket.on("delay-statistics", (msg: any) => {
  delayStatisticsLayer.updateData(msg.geocell, msg);
});

// Flow direction layer
const flowDirectionLayer = new GeoedgeLayer(
  "Flow direction",
  (data) => `Vehicles in the last 5 min: ${data}`
);
socket.on("flow-direction", (msg: any) => {
  flowDirectionLayer.updateData(msg.edge, msg.count);
});

// General maps
var streetsLayerLite = tileLayer.provider("Stamen.TonerLite");
var streetsLayerDark = tileLayer.provider("CartoDB.DarkMatter");

const map = lmap("map-container", {
  center: [60.2199, 24.9284],
  zoom: 11.7,
  layers: [streetsLayerLite, vehicleCountsLayer],
});

control
  .scale({
    imperial: false,
  })
  .addTo(map);
control
  .layers(
    {
      "Streets Light": streetsLayerLite,
      "Streets Dark": streetsLayerDark,
    },
    {
      "Vehicle count": vehicleCountsLayer,
      "Delay statistics": delayStatisticsLayer,
      "Flow direction": flowDirectionLayer,
    },
    {
      position: "bottomright",
      collapsed: false,
    }
  )
  .addTo(map);
