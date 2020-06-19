import { Map as LMap, Layer, Polyline, polyline } from "leaflet";
import { H3Index, h3ToGeo, getH3IndexesFromUnidirectionalEdge } from "h3-js";
import { ExpiringMap } from "./expiring-map";
import "leaflet-arrowheads";

/**
 * Map layer for drawing edges between H3 geocells
 */
export default class GeoedgeLayer<T> extends Layer {
  private data = new ExpiringMap<string, T>();
  private edges = new Map<string, Polyline>();

  private active = false;

  private map: LMap;

  constructor(
    private name: string,
    private popupTextSelector: (data: T) => string = undefined,
    private displayDataSelector: (data: T) => number = (data) => Number(data),
    private timeout: number = 10000
  ) {
    super();
  }

  public onAdd(map: LMap): this {
    this.active = true;
    this.map = map;

    return this;
  }

  public onRemove(map: LMap): this {
    this.active = false;
    this.data.clear();
    this.edges.forEach((cell) => cell.removeFrom(this.map));
    this.edges.clear();

    return this;
  }

  public updateData(edge: H3Index, data: T): void {
    if (!this.active) {
      return;
    }

    let [fromCell, toCell] = getH3IndexesFromUnidirectionalEdge(edge);

    this.data.set(fromCell, data, this.timeout, (key, value) => {
      // Clean up map cell when data expires
      this.edges.get(key).removeFrom(this.map);
      this.edges.delete(key);
    });

    let line;
    if (!this.edges.has(fromCell)) {
      // @ts-ignore
      line = polyline([fromCell, toCell].map(h3ToGeo)).addTo(this.map);
      // @ts-ignore
      line.arrowheads({
        frequency: "endonly",
      });
      this.edges.set(fromCell, line);
    } else {
      line = this.edges.get(fromCell);
    }
    if (this.popupTextSelector) {
      line.bindPopup(() => {
        return this.popupTextSelector(data);
      });
    }
  }
}
