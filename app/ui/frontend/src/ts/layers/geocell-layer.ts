import { Map as LMap, Layer, Polygon, polygon } from "leaflet";
import { v4 as uuidv4 } from "uuid";
import { h3ToGeoBoundary, H3Index } from "h3-js";
import { LegendUi, ColorBar, ColorbarLimitUpdater } from "../util/legend-ui";
import { schemeOrRd } from "d3-scale-chromatic";
import LayerDataManager from "../util/layer-data-manager";

/**
 * Map layer for drawing H3 geocells with data-based coloring
 *
 * The timeout determines when old cell data is removed if no new data arrived
 * The hexagon overlays are cleared at most every two seconds based on the available data
 */
export default class GeocellLayer<T> extends Layer {
  private dataManager: LayerDataManager<H3Index, T>;
  private limitUpdater: ColorbarLimitUpdater<H3Index, T>;

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
    this.dataManager = new LayerDataManager(
      this.createHexagon,
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
    geocell: H3Index,
    data: { timestamp: number; data: T }
  ): void {
    this.dataManager.updateData(geocell, data.data, data.timestamp);
  }

  private createHexagon(geocell: any): Polygon {
    // @ts-ignore
    return polygon(h3ToGeoBoundary(geocell)).setStyle({
      stroke: false,
      fillOpacity: 0,
    });
  }
}
