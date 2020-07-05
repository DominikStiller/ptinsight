import { select, create, Selection, BaseType } from "d3-selection";
import { ScaleSequential, scaleSequential } from "d3-scale";
import { interpolateRgbBasis } from "d3-interpolate";

type D3Selection = Selection<BaseType, unknown, HTMLElement, any>;

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
      .attr("x", "180")
      .attr("width", 20)
      .attr("height", 300)
      .attr("fill", `url(#gradient-${this.name})`);

    this.minText = create("svg:text")
      .attr("x", "175")
      .attr("y", "300")
      .style("text-anchor", "end");

    this.maxText = create("svg:text")
      .attr("x", "175")
      .attr("y", "20")
      .attr("text-anchor", "end");
  }

  public updateDomain(domain: [number, number]): void {
    this.scale.domain(domain);
    this.minText.text(
      domain[0] == Number.NEGATIVE_INFINITY ? 0 : this.formatLimit(domain[0])
    );
    this.maxText.text(
      domain[1] == Number.POSITIVE_INFINITY ? 0 : this.formatLimit(domain[1])
    );
  }

  private formatLimit(limit: number): string {
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
