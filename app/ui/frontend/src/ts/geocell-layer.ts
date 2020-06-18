import { Map as LMap, Layer, Polygon, polygon } from "leaflet";
import { debounce } from "underscore";
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
export default class GeocellLayer extends Layer {
  private data = new ExpiringMap<string, number>();
  private cells = new Map<string, Polygon>();

  private active = false;
  private minData = Number.POSITIVE_INFINITY;
  private maxData = Number.NEGATIVE_INFINITY;

  private map: LMap;
  private colorbar: ColorBar;

  constructor(
    private name: string,
    private popupText: (data: number) => string,
    private legend: LegendUi,
    private timeout: number
  ) {
    super();
    this.colorbar = new ColorBar(
      name.replace(" ", "-").toLowerCase(),
      // @ts-ignore
      schemeOrRd[8]
    );
    this.updateLimits = debounce(this.updateLimits, 2000);
  }

  public onAdd(map: LMap): this {
    this.active = true;
    this.map = map;
    this.colorbar.attachTo(this.legend);

    return this;
  }

  public onRemove(map: LMap): this {
    this.active = false;
    this.colorbar.removeFrom(this.legend);
    this.data.clear();
    this.cells.forEach((cell) => cell.removeFrom(this.map));
    this.cells.clear();

    return this;
  }

  public updateData(geocell: H3Index, data: number): void {
    if (!this.active) {
      return;
    }

    this.data.set(geocell, data, this.timeout, (key, value) => {
      // Clean up map cell when data expires
      this.cells.get(key).removeFrom(this.map);
      this.cells.delete(key);
    });

    this.updateLimits();

    if (!this.cells.has(geocell)) {
      // @ts-ignore
      let hexagon = polygon(h3ToGeoBoundary(geocell)).addTo(this.map);
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
        return this.popupText(data);
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
