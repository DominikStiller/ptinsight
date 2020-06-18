import * as leaflet from "leaflet";
import * as underscore from "underscore";
import { h3ToGeoBoundary, H3Index } from "h3-js";
import { ExpiringMap } from "./expiring-map";
import { LegendUi, ColorBar } from "./legend-ui";
import { schemeOrRd } from "d3-scale-chromatic";

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
    // @ts-ignore
    this.colorbar = new ColorBar("vehicle-count", schemeOrRd[8]);
    this.colorbar.attachTo(legend);
    this.updateLimits = underscore.debounce(this.updateLimits, 2000);
  }

  public updateData(geocell: H3Index, data: number): void {
    this.data.set(geocell, data, this.timeout, (key, value) => {
      // Clean up map cell when data expires
      this.cells.get(key).removeFrom(this.map);
      this.cells.delete(key);
    });

    this.updateLimits();

    if (!this.cells.has(geocell)) {
      let hexagon = leaflet
        // @ts-ignore
        .polygon(h3ToGeoBoundary(geocell));
      hexagon.addTo(this.map);
      this.cells.set(geocell, hexagon);
    }
    this.cells
      .get(geocell)
      .setStyle({
        stroke: false,
        fillColor: this.colorbar.getColor(data),
        fillOpacity: 0.7,
      })
      .bindPopup((layer) => {
        return `Vehicle count: ${data}`;
      });
  }

  private updateLimits(): void {
    // Limits are calculated regularly from all data instead of for every data update
    // because the limits cannot shrink otherwise
    let values = Array.from(this.data.values());
    this.minData = Math.min(...values);
    this.maxData = Math.max(...values);
    this.colorbar.updateDomain([this.minData, this.maxData]);
  }
}
