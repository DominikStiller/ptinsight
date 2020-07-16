import { select, create, Selection, BaseType } from "d3-selection";
import { ScaleSequential, scaleSequential } from "d3-scale";
import { interpolateRgbBasis } from "d3-interpolate";
import { Path } from "leaflet";

type D3Selection = Selection<BaseType, unknown, HTMLElement, any>;

/**
 * A container for D3-based UI components
 */
export class LegendUi {
  private defs: D3Selection;
  private content: D3Selection;
  private canvas: D3Selection;

  constructor() {
    this.canvas = select("#legend-container").append("svg");
    this.defs = this.canvas.append("defs");
    this.content = this.canvas.append("g").attr("id", "content");
  }

  public addDef(def: D3Selection): void {
    this.defs.append(() => def.node());
  }

  public removeDef(def: D3Selection): void {
    def.remove();
  }

  public addContent(content: D3Selection): void {
    this.content.append(() => content.node());
  }

  public removeContent(content: D3Selection): void {
    content.remove();
  }
}

/**
 * A monotonically increasing text display for timestamp
 */
export class TimeDisplay {
  private text: D3Selection;
  private latestTimestamp: number = 0;

  constructor() {
    this.text = create("svg:text")
      .attr("x", 80)
      .attr("y", 24)
      .attr("font-size", 24);
  }

  public update(timestamp: number): void {
    if (timestamp > this.latestTimestamp) {
      this.latestTimestamp = timestamp;

      let date = new Date(timestamp);
      this.text.text(date.toUTCString());
    }
  }

  public attachTo(legend: LegendUi): void {
    legend.addContent(this.text);
  }

  public removeFrom(legend: LegendUi): void {
    legend.removeContent(this.text);
  }
}

/**
 * A colorbar with a color gradient that is mapped to a number range
 */
export class ColorBar {
  private readonly scale: ScaleSequential<string>;

  private gradientDef: D3Selection;
  private bar: D3Selection;
  private minText: D3Selection;
  private maxText: D3Selection;

  constructor(private name: string, private colorScheme: string[]) {
    this.scale = scaleSequential<string>(interpolateRgbBasis(colorScheme));
    this.createElements();
  }

  private createElements(): void {
    this.gradientDef = create("svg:linearGradient")
      .attr("id", `gradient-${this.name}`)
      .attr("x1", "0")
      .attr("x2", "0")
      .attr("y1", "1")
      .attr("y2", "0");

    this.gradientDef
      .selectAll("stop")
      // @ts-ignore
      .data(this.colorScheme)
      .enter()
      .append("stop")
      .attr("offset", (d, i) => {
        return i / (this.colorScheme.length - 1);
      })
      .attr("stop-color", (d: string) => d);

    this.bar = create("svg:rect")
      .attr("x", "480")
      .attr("width", 20)
      .attr("height", 300)
      .attr("fill", `url(#gradient-${this.name})`);

    this.minText = create("svg:text")
      .attr("x", "475")
      .attr("y", "300")
      .style("text-anchor", "end");

    this.maxText = create("svg:text")
      .attr("x", "475")
      .attr("y", "20")
      .attr("text-anchor", "end");
  }

  public updateDomain(domain: [number, number]): void {
    this.scale.domain(domain);
    this.minText.text(this.formatLimit(domain[0]));
    this.maxText.text(this.formatLimit(domain[1]));
  }

  private formatLimit(limit: number): string {
    if (limit == Number.POSITIVE_INFINITY) {
      return "0";
    }
    if (limit == Number.NEGATIVE_INFINITY) {
      return "1";
    }
    if (Number.isInteger(limit)) {
      return limit.toString();
    } else {
      return limit.toFixed(1);
    }
  }

  public getColor(value: number): string {
    return this.scale(value);
  }

  public attachTo(legend: LegendUi): void {
    legend.addDef(this.gradientDef);
    legend.addContent(this.bar);
    legend.addContent(this.minText);
    legend.addContent(this.maxText);
  }

  public removeFrom(legend: LegendUi): void {
    legend.removeDef(this.gradientDef);
    legend.removeContent(this.bar);
    legend.removeContent(this.minText);
    legend.removeContent(this.maxText);
  }
}

/**
 * A class that periodically updates colorbar limits based on data and updates shape color correspondingly.
 *
 * While the periodic approach might be inefficient, updating the limits on each data update is impossibly because then the limits will never shrink.
 */
export class ColorbarLimitUpdater<K, T> {
  private timeoutHandle: number;
  private legend: LegendUi;

  constructor(
    private dataIterator: () => IterableIterator<[K, T, Path]>,
    private dataSizeAccessor: () => number,
    private displayDataSelector: (data: T) => number,
    private colorbar: ColorBar
  ) {}

  public addToLegend(legend: LegendUi): void {
    this.legend = legend;
  }

  public activate(): void {
    this.colorbar.attachTo(this.legend);

    // @ts-ignore
    this.updateLimitsHandle = setTimeout(() => this.updateLimits(), 1000);
  }

  public deactivate(): void {
    this.colorbar.removeFrom(this.legend);
    clearTimeout(this.timeoutHandle);
  }

  private updateLimits(): void {
    // Retrieve number values
    let values = Array.from(this.dataIterator())
      .map((value) => value[1])
      .map(this.displayDataSelector);

    const minData = Math.min(...values);
    const maxData = Math.max(...values);
    this.colorbar.updateDomain([minData, maxData]);

    // Update shape colors
    for (const [geokey, data, shape] of this.dataIterator()) {
      shape.setStyle({
        fillColor: this.colorbar.getColor(this.displayDataSelector(data)),
        fillOpacity: 0.7,
      });
    }

    let timeout = 50;
    if (this.dataSizeAccessor() > 10) {
      // Use larger timeout when there are many shapes to improve performance
      timeout = 1000;
    }
    // @ts-ignore
    this.updateLimitsHandle = setTimeout(() => this.updateLimits(), timeout);
  }
}
