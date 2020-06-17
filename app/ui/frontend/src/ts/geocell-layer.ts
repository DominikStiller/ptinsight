import * as leaflet from "leaflet";
import { h3ToGeoBoundary, H3Index } from "h3-js";
import { ExpiringMap } from "./expiring-map";
import LegendUi, { ColorBar } from "./legend-ui";
import * as d3 from "d3";
import { magmaColorScheme } from "./colors";

/**
 * Map layer for drawing H3 geocells
 *
 * The timeout determines when old cell data is removed if no new data arrived
 * The hexagon overlays are cleared every second based on the available data
 */
export default class GeocellLayer {
  private data = new ExpiringMap<string, number>();
  private cells = new Map<string, leaflet.Polygon>();

  private minData = Number.POSITIVE_INFINITY;
  private maxData = Number.NEGATIVE_INFINITY;

  private colorbar: ColorBar;

  constructor(
    private map: leaflet.Map,
    private legend: LegendUi,
    private timeout: number
  ) {
    setInterval(() => this.updateLimits(), timeout);
    // @ts-ignore
    this.colorbar = new ColorBar("vehicle-count", d3.schemeOrRd[8]);
    this.colorbar.attachTo(legend);
  }

  public updateData(geocell: H3Index, data: number): void {
    this.data.set(geocell, data, this.timeout, (key, value) => {
      // Clean up map cell when data expires
      this.cells.get(key).removeFrom(this.map);
      this.cells.delete(key);
    });

    if (this.data.size <= 5) {
      // Update limits every time at the beginning to prevent all being at max
      this.updateLimits();
    }

    let style = {
      stroke: false,
      fillColor: this.colorbar.getColor(data),
      fillOpacity: 0.7,
    };

    if (this.cells.has(geocell)) {
      this.cells.get(geocell).setStyle(style);
    } else {
      // @ts-ignore
      let hexagon = leaflet
        .polygon(h3ToGeoBoundary(geocell), style)
        .bindPopup((layer) => {
          return `Vehicle count: ${data}`;
        });
      hexagon.addTo(this.map);
      this.cells.set(geocell, hexagon);
    }
  }

  private updateLimits(): void {
    // Limits are calculated regularly from all data instead of at every data update
    // because the limits will otherwise only increase but never go back, since they have no timeout
    let values = Array.from(this.data.values());
    this.minData = Math.min(...values);
    this.maxData = Math.max(...values);
    this.colorbar.updateDomain([this.minData, this.maxData]);
  }
}
