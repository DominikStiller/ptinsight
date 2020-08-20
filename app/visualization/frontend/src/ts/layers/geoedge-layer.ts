import { Map as LMap, Layer, Polyline, polyline } from "leaflet";
import { H3Index, h3ToGeo, getH3IndexesFromUnidirectionalEdge } from "h3-js";
import { ExpiringMap } from "../util/expiring-map";
import "leaflet-arrowheads";
import LayerDataManager from "../util/layer-data-manager";

/**
 * Map layer for drawing edges between H3 geocells
 */
export default class GeoedgeLayer<T> extends Layer {
  private dataManager: LayerDataManager<H3Index, T>;

  constructor(
    private popupTextSelector: (data: T) => string = undefined,
    private timeout: number = 10000
  ) {
    super();
    this.dataManager = new LayerDataManager(
      this.createEdge,
      popupTextSelector,
      timeout
    );
  }

  public onAdd(map: LMap): this {
    this.dataManager.activate(map);

    return this;
  }

  public onRemove(map: LMap): this {
    this.dataManager.deactivate(map);

    return this;
  }

  public updateData(edge: H3Index, data: { timestamp: number; data: T }): void {
    let [fromCell, toCell] = getH3IndexesFromUnidirectionalEdge(edge);
    data.data = Object.assign(data.data, { fromCell, toCell });

    this.dataManager.updateData(fromCell, data.data, data.timestamp);
  }

  private createEdge(edge: H3Index, data: any): Polyline {
    // @ts-ignore
    return polyline([data.fromCell, data.toCell].map(h3ToGeo)).arrowheads({
      frequency: "endonly",
    });
  }
}
