import * as leaflet from "leaflet";
import { h3ToGeoBoundary, H3Index } from "h3-js";
import { ExpiringMap } from "./expiring-map";

export default class GeocellLayer {
  private data = new ExpiringMap<string, any>();
  private cells = new Map<string, leaflet.Polygon>();

  constructor(private map: leaflet.Map, private timeout: number) {
    setInterval(this.clearOldCells, 1000);
  }

  public updateData(geocell: H3Index, data: any): void {
    this.data.set(geocell, data, this.timeout);

    let style = {
      stroke: false,
      color: "red",
    };

    if (this.cells.has(geocell)) {
      this.cells.get(geocell).setStyle(style);
    } else {
      // @ts-ignore
      let hexagon = leaflet.polygon(h3ToGeoBoundary(geocell), style);
      hexagon.addTo(this.map);
      this.cells.set(geocell, hexagon);
    }
  }

  private clearOldCells() {
    for (let key of this.cells.keys()) {
      if (!this.data.has(key)) {
        this.cells.get(key).removeFrom(this.map);
        this.cells.delete(key);
      }
    }
  }
}
