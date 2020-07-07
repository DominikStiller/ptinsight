import { Map as LMap, Layer, Polygon, polygon } from "leaflet";
import { debounce } from "underscore";
import { v4 as uuidv4 } from "uuid";
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
  private data = new ExpiringMap<string, { data: T; timestamp: number }>();
  private cells = new Map<string, Polygon>();

  private active = false;
  private minData = Number.POSITIVE_INFINITY;
  private maxData = Number.NEGATIVE_INFINITY;

  private legend: LegendUi;
  private map: LMap;
  private colorbar: ColorBar;

  private updateLimitsDebounced: () => void;

  /**
   * Creates a new geocell layer
   * @param popupTextSelector A function that returns the popup text
   * @param displayDataSelector A function that returns the value used for coloring
   * @param timeout The timeout after which data for a cell is removed if they were not updated.
   *                Should be set about 5 s higher than the data generation interval.
   */
  constructor(
    private popupTextSelector: (data: T) => string,
    private displayDataSelector: (data: T) => number,
    private timeout: number = 10000
  ) {
    super();
    // @ts-ignore
    this.colorbar = new ColorBar(uuidv4(), schemeOrRd[8]);
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

  public updateData(
    geocell: H3Index,
    data: { timestamp: number; data: T }
  ): void {
    // Do not accept data for inactive layer
    if (!this.active) {
      return;
    }

    // Do not accept outdated data
    // Data with the same timestamp are okay, since they are refinements using late data
    if (
      this.data.has(geocell) &&
      data.timestamp < this.data.get(geocell).timestamp
    ) {
      return;
    }

    this.data.set(geocell, data, this.timeout, (geocell) => {
      // Clean up map cell when data expires
      this.cells.get(geocell).removeFrom(this.map);
      this.cells.delete(geocell);
    });

    if (this.cells.size <= 5) {
      this.updateLimits();
    } else {
      this.updateLimitsDebounced();
    }

    // Reuse and only re-color old hexagons if possible to improve performance
    let hexagon = this.cells.get(geocell);
    if (hexagon == undefined) {
      // @ts-ignore
      hexagon = polygon(h3ToGeoBoundary(geocell)).addTo(this.map);
      this.cells.set(geocell, hexagon);
    }
    hexagon.setStyle({
      stroke: false,
      fillColor: this.colorbar.getColor(this.displayDataSelector(data.data)),
      fillOpacity: 0.7,
    });
    hexagon.bindPopup(() => {
      return this.popupTextSelector(data.data);
    });
  }

  private updateLimits(): void {
    // Limits are calculated regularly from all data instead of for every data update
    // because the limits cannot shrink otherwise
    let values = Array.from(this.data.values())
      .map((value) => value.data)
      .map(this.displayDataSelector);
    this.minData = Math.min(...values);
    this.maxData = Math.max(...values);
    this.colorbar.updateDomain([this.minData, this.maxData]);
    // TODO recalculate limits if not too compute-heavy
  }
}
