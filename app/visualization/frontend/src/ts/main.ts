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

// Vehicle distribution layer
const vehicleDistributionLayer = new GeocellLayer<{
  count: number;
}>(
  (data) => `Vehicles in the last 30 s: ${data.count}`,
  (data) => data.count
).addToLegend(legend);
subscribe("analytics.vehicle-distribution", (msg: any) => {
  vehicleDistributionLayer.updateData(msg.data.geocell, msg);
});

// Delay distribution layer
const delayDistributionLayer = new GeocellLayer<{
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
subscribe("analytics.delay-distribution", (msg: any) => {
  if (msg.data.p90 >= 0) {
    delayDistributionLayer.updateData(msg.data.geocell, msg);
  }
});

// Flow direction layer
const flowDirectionLayer = new GeoedgeLayer<{
  count: number;
}>((data) => `Vehicles in the last 5 min: ${data.count}`);
subscribe("analytics.flow-direction", (msg: any) => {
  // Keep map clean by only showing edges with more than 3 vehicles
  if (msg.data.count > 2) {
    flowDirectionLayer.updateData(msg.data.edge, msg);
  }
});

// Final stop distribution layer
const finalStopDistributionLayer = new GeocellLayer<{
  count: number;
}>(
  (data) => `Vehicles bound for here in the last 5 min: ${data.count}`,
  (data) => data.count
).addToLegend(legend);
subscribe("analytics.final-stop-distribution", (msg: any) => {
  finalStopDistributionLayer.updateData(msg.data.geocell, msg);
});

// Emergency stop detection layer
const emergencyStopDetectionLayer = new GeopointLayer<{
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
subscribe("analytics.emergency-stop-detection-streaming", (msg: any) => {
  emergencyStopDetectionLayer.updateData([msg.data.lat, msg.data.lon], msg);
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
  layers: [streetsLayerLite, vehicleDistributionLayer],
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
      "Vehicle distribution": vehicleDistributionLayer,
      "Delay distribution": delayDistributionLayer,
      "Flow direction": flowDirectionLayer,
      "Final stop distribution": finalStopDistributionLayer,
      "Emergency stops": emergencyStopDetectionLayer,
    },
    {
      position: "bottomright",
      collapsed: false,
    }
  )
  .addTo(map);
