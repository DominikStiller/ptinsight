import { Map as LMap, Layer, Circle, circle } from "leaflet";
import { v4 as uuidv4 } from "uuid";
import { debounce } from "underscore";
import { ExpiringMap } from "./expiring-map";
import { LegendUi, ColorBar } from "./legend-ui";
import { schemeOrRd } from "d3-scale-chromatic";
import { H3Index } from "h3-js";

/**
 * Map layer for drawing points at coordinates with data-based coloring
 */
export default class GeopointLayer<T> extends Layer {
  private data = new ExpiringMap<
    [number, number],
    { data: T; timestamp: number }
  >();
  private points = new Map<[number, number], Circle>();

  private active = false;
  private minData = Number.POSITIVE_INFINITY;
  private maxData = Number.NEGATIVE_INFINITY;

  private legend: LegendUi;
  private map: LMap;
  private colorbar: ColorBar;

  private updateLimitsDebounced: () => void;

  constructor(
    private popupTextSelector: (data: T) => string = undefined,
    private displayDataSelector: (data: T) => number = (data) => Number(data),
    private timeout: number = 30000
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
    this.points.forEach((cell) => cell.removeFrom(this.map));
    this.points.clear();

    return this;
  }

  public updateData(
    coords: [number, number],
    data: { timestamp: number; data: T }
  ): void {
    if (!this.active) {
      return;
    }

    if (
      this.data.has(coords) &&
      data.timestamp < this.data.get(coords).timestamp
    ) {
      return;
    }

    this.data.set(coords, data, this.timeout, (key, value) => {
      // Clean up map point when data expires
      this.points.get(key).removeFrom(this.map);
      this.points.delete(key);
    });

    if (this.points.size <= 5) {
      this.updateLimits();
    } else {
      this.updateLimitsDebounced();
    }

    let point = circle(coords, { radius: 100 }).addTo(this.map);
    this.points.set(coords, point);
    point.setStyle({
      stroke: false,
      fillColor: this.colorbar.getColor(this.displayDataSelector(data.data)),
      fillOpacity: 0.7,
    });
    if (this.popupTextSelector) {
      point.bindPopup(() => {
        return this.popupTextSelector(data.data);
      });
    }
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
