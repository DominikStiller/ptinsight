import { Map as LMap, Layer, Polygon, polygon } from "leaflet";
import { debounce } from "underscore";
import { h3ToGeoBoundary, H3Index } from "h3-js";
import { ExpiringMap } from "./expiring-map";
import { LegendUi, ColorBar } from "./legend-ui";
import { schemeOrRd } from "d3-scale-chromatic";

/**
 * Map layer for drawing H3 geocells with data-based coloring
 *
 * The timeout determines when old cell data is removed if no new data arrived
 * The hexagon overlays are cleared at most every two seconds based on the available data
 */
export default class GeocellLayer<T> extends Layer {
  private data = new ExpiringMap<string, T>();
  private cells = new Map<string, Polygon>();

  private active = false;
  private minData = Number.POSITIVE_INFINITY;
  private maxData = Number.NEGATIVE_INFINITY;

  private legend: LegendUi;
  private map: LMap;
  private colorbar: ColorBar;

  private updateLimitsDebounced: () => void;

  constructor(
    private name: string,
    private popupTextSelector: (data: T) => string = undefined,
    private displayDataSelector: (data: T) => number = (data) => Number(data),
    private timeout: number = 10000
  ) {
    super();
    this.colorbar = new ColorBar(
      name.split(" ").join("-").toLowerCase(),
      // @ts-ignore
      schemeOrRd[8]
    );
    this.updateLimitsDebounced = debounce(this.updateLimits, 2000);
  }

  public addToLegend(legend: LegendUi): this {
    this.legend = legend;

    return this;
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

  public updateData(geocell: H3Index, data: T): void {
    if (!this.active) {
      return;
    }

    this.data.set(geocell, data, this.timeout, (key, value) => {
      // Clean up map cell when data expires
      this.cells.get(key).removeFrom(this.map);
      this.cells.delete(key);
    });

    if (this.cells.size <= 5) {
      this.updateLimits();
    } else {
      this.updateLimitsDebounced();
    }

    let hexagon;
    if (!this.cells.has(geocell)) {
      // @ts-ignore
      hexagon = polygon(h3ToGeoBoundary(geocell)).addTo(this.map);
      this.cells.set(geocell, hexagon);
    } else {
      hexagon = this.cells.get(geocell);
    }
    hexagon.setStyle({
      stroke: false,
      fillColor: this.colorbar.getColor(this.displayDataSelector(data)),
      fillOpacity: 0.7,
    });
    if (this.popupTextSelector) {
      hexagon.bindPopup(() => {
        return this.popupTextSelector(data);
      });
    }
  }

  private updateLimits(): void {
    // Limits are calculated regularly from all data instead of for every data update
    // because the limits cannot shrink otherwise
    let values = Array.from(this.data.values()).map(this.displayDataSelector);
    this.minData = Math.min(...values);
    this.maxData = Math.max(...values);
    this.colorbar.updateDomain([this.minData, this.maxData]);
    // TODO recalculate limits if not too compute-heavy
  }
}
