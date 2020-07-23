import "leaflet-providers";
import { control, map as lmap, tileLayer } from "leaflet";
import * as socketio from "socket.io-client";
import GeocellLayer from "./layers/geocell-layer";
import GeoedgeLayer from "./layers/geoedge-layer";
import { LegendUi, TimeDisplay } from "./util/legend-ui";
import "./styles";
import GeopointLayer from "./layers/geopoint-layer";

const socket = socketio();
const legend = new LegendUi();
const timeDisplay = new TimeDisplay();
timeDisplay.attachTo(legend);

function subscribe(topic: string, handler: (msg: any) => void) {
  socket.on(topic, (msg: any) => {
    timeDisplay.update(msg.timestamp);
    handler(msg);
  });
}

// Vehicle counts layer
const vehicleCountsLayer = new GeocellLayer<{
  count: number;
}>(
  (data) => `Vehicles in the last 30 s: ${data.count}`,
  (data) => data.count
).addToLegend(legend);
subscribe("egress.vehicle-count", (msg: any) => {
  vehicleCountsLayer.updateData(msg.data.geocell, msg);
});

// Delay statistics layer
const delayStatisticsLayer = new GeocellLayer<{
  p50: number;
  p90: number;
  p99: number;
}>(
  (data) =>
    `Arrival delay in the last 5 min (50th percentile): ${data.p50} min<br>
      Arrival delay in the last 5 min (90th percentile): ${data.p90} min<br>
      Arrival delay in the last 5 min (99th percentile): ${data.p99} min`,
  (data) => data.p90
).addToLegend(legend);
subscribe("egress.delay-statistics", (msg: any) => {
  if (msg.data.p90 >= 0) {
    delayStatisticsLayer.updateData(msg.data.geocell, msg);
  }
});

// Flow direction layer
const flowDirectionLayer = new GeoedgeLayer<{
  count: number;
}>((data) => `Vehicles in the last 5 min: ${data.count}`);
subscribe("egress.flow-direction", (msg: any) => {
  // Keep map clean by only showing edges with more than 3 vehicles
  if (msg.data.count > 3) {
    flowDirectionLayer.updateData(msg.data.edge, msg);
  }
});

// Final stop counts layer
const finalStopCountsLayer = new GeocellLayer<{
  count: number;
}>(
  (data) => `Vehicles bound for here in the last 5 min: ${data.count}`,
  (data) => data.count
).addToLegend(legend);
subscribe("egress.final-stop-count", (msg: any) => {
  finalStopCountsLayer.updateData(msg.data.geocell, msg);
});

// Emergency stop layer
const emergencyStopLayer = new GeopointLayer<{
  veh_type: string;
  lat: number;
  lon: number;
  max_dec: number;
  speed_diff: number;
}>(
  (data) =>
    `Vehicle type: ${data.veh_type}<br>
     Speed difference between cruising and stop: ${data.speed_diff.toFixed(
       1
     )} m/s<br>
      Maximum deceleration: ${data.max_dec.toFixed(1)} m/s<sup>2</sup>`,
  (data) => -data.max_dec
).addToLegend(legend);
subscribe("egress.emergency-stop-streaming", (msg: any) => {
  emergencyStopLayer.updateData([msg.data.lat, msg.data.lon], msg);
});

// General maps
var streetsLayerLite = tileLayer.provider("Stamen.TonerLite");
var streetsLayerDark = tileLayer.provider("CartoDB.DarkMatter");

const map = lmap("map-container", {
  // Zoom on Helsinki initially
  center: [60.2199, 24.9284],
  // Point Nemo (origin of latency markers)
  // center: [-48.875, -123.393],
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
      "Final stop count": finalStopCountsLayer,
      "Emergency stop": emergencyStopLayer,
    },
    {
      position: "bottomright",
      collapsed: false,
    }
  )
  .addTo(map);
