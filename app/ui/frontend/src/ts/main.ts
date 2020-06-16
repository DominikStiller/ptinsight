import * as L from "leaflet";
import * as _ from "underscore";
import * as io from "socket.io-client";
import * as h3 from "h3-js";
import { ExpiringMap } from "./expiring-map";
import "./styles";

let counts = new ExpiringMap();
let polygons = new Map();

const map = L.map("map-container").setView([60.1699, 24.9384], 11);
L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
  attribution:
    '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
  maxZoom: 19,
}).addTo(map);

const socket = io();
socket.on("vehicle-count", (msg: any) => {
  counts.set(msg.geocell, msg.count, 6000);
  updateMap();
});

let updateMap = _.debounce(() => {
  for (let key of polygons.keys()) {
    if (!counts.has(key)) {
      polygons.get(key).removeFrom(map);
      polygons.delete(key);
    }
  }

  counts.forEach((count, index) => {
    let style = {
      stroke: false,
      color: "red",
    };

    if (polygons.has(index)) {
      polygons.get(index).setStyle(style);
    } else {
      polygons.set(
        index,
        L.polygon(h3.h3ToGeoBoundary(index), style).addTo(map)
      );
    }
  });
}, 1000);
