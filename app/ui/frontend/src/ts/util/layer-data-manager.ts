import { Map as LMap, Path } from "leaflet";
import { ExpiringMap } from "./expiring-map";

/**
 * A class to manage data and their corresponding shapes for map layers. Data and shapes are identified by a geokey like coordinates or an H3 geocell.
 */
export default class LayerDataManager<K, T> {
  private data = new ExpiringMap<K, { data: T; timestamp: number }>();
  private shapes = new Map<K, Path>();

  private active = false;
  private map: LMap;

  /**
   * Creates a new layer data manager
   * @param shapeCreator A function that returns a new shape
   * @param popupTextSelector A function that returns the popup text
   * @param timeout The timeout after which data for a cell is removed if they were not updated.
   *                Should be set about 5 s higher than the data generation interval.
   */
  constructor(
    private shapeCreator: (geokey: K, data: T) => Path,
    private popupTextSelector: (data: T) => string,
    private timeout: number = 10000
  ) {}

  public activate(map: LMap): void {
    this.active = true;
    this.map = map;
  }

  public deactivate(map: LMap): void {
    if (map != this.map) {
      throw "Can only deactivate on map that this data manager belongs to";
    }

    this.active = false;

    this.data.clear();
    this.shapes.forEach((shape) => shape.removeFrom(map));
    this.shapes.clear();
  }

  public updateData(geokey: K, data: T, timestamp: number): void {
    // Do not accept data when layer is inactive
    if (!this.active) {
      return;
    }

    // Do not accept outdated data
    // Data with the same timestamp are okay, since they are refinements using late data
    if (this.data.has(geokey) && timestamp < this.data.get(geokey).timestamp) {
      return;
    }

    // Store data and register callback to keep shapes synchronized
    this.data.set(geokey, { data, timestamp }, this.timeout, (geokey) => {
      // Clean up shapes when data expires
      this.shapes.get(geokey).removeFrom(this.map);
      this.shapes.delete(geokey);
    });

    // Reuse shapes if possible to improve performance
    let shape = this.shapes.get(geokey);
    if (shape == undefined) {
      // Create new shape for new geokeys
      shape = this.shapeCreator(geokey, data);
      shape.addTo(this.map);
      this.shapes.set(geokey, shape);
    }
    if (this.popupTextSelector) {
      shape.bindPopup(() => this.popupTextSelector(data));
    }
  }

  /**
   * Returns an iterator over all geokeys and their data and shapes
   */
  public *iterator(): IterableIterator<[K, T, Path]> {
    for (const [geokey, data] of this.data) {
      yield [geokey, data.data, this.shapes.get(geokey)];
    }
  }

  /**
   * Returns the current number of geokeys
   */
  public size(): number {
    return this.data.size;
  }
}
