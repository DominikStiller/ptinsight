import { Map as LMap, Layer, circle, Circle } from "leaflet";
import { v4 as uuidv4 } from "uuid";
import { LegendUi, ColorBar, ColorbarLimitUpdater } from "../util/legend-ui";
import { schemeOrRd } from "d3-scale-chromatic";
import LayerDataManager from "../util/layer-data-manager";

/**
 * Map layer for drawing points at coordinates with data-based coloring
 */
export default class GeopointLayer<T> extends Layer {
  private dataManager: LayerDataManager<[number, number], T>;
  private limitUpdater: ColorbarLimitUpdater<[number, number], T>;

  constructor(
    private popupTextSelector: (data: T) => string = undefined,
    private displayDataSelector: (data: T) => number = (data) => Number(data),
    private timeout: number = 30000
  ) {
    super();
    this.dataManager = new LayerDataManager(
      this.createCircle,
      popupTextSelector,
      timeout
    );
    this.limitUpdater = new ColorbarLimitUpdater(
      () => this.dataManager.iterator(),
      () => this.dataManager.size(),
      displayDataSelector,
      // @ts-ignore
      new ColorBar(uuidv4(), schemeOrRd[8])
    );
  }

  public addToLegend(legend: LegendUi): this {
    this.limitUpdater.addToLegend(legend);

    return this;
  }

  public onAdd(map: LMap): this {
    this.limitUpdater.activate();
    this.dataManager.activate(map);

    return this;
  }

  public onRemove(map: LMap): this {
    this.dataManager.deactivate(map);
    this.limitUpdater.deactivate();

    return this;
  }

  public updateData(
    coords: [number, number],
    data: { timestamp: number; data: T }
  ): void {
    this.dataManager.updateData(coords, data.data, data.timestamp);
  }

  private createCircle(coords: [number, number]): Circle {
    return circle(coords, { radius: 100 }).setStyle({
      stroke: false,
      fillOpacity: 0,
    });
  }
}
