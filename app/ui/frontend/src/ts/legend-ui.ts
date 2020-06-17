import * as d3 from "d3";

type D3Selection = d3.Selection<d3.BaseType, unknown, HTMLElement, any>;

export default class LegendUi {
  private defs: D3Selection;
  private content: D3Selection;
  private canvas: D3Selection;

  constructor() {
    this.canvas = d3.select("#legend-container").append("svg");
    this.defs = this.canvas.append("defs");
    this.content = this.canvas.append("g").attr("id", "content");
  }

  public addDef(def: D3Selection): void {
    this.defs.append(() => def.node());
  }

  public addContent(content: D3Selection): void {
    this.content.append(() => content.node());
  }
}

export class ColorBar {
  private scale: d3.ScaleSequential<string>;

  private gradientDef: D3Selection;
  private bar: D3Selection;
  private minText: D3Selection;
  private maxText: D3Selection;

  constructor(private name: string, private colorScheme: string[]) {
    this.scale = d3
      .scaleSequential<string>(d3.interpolateRgbBasis(colorScheme))
      .domain([0, 1]);
    this.createElements();
  }

  private createElements(): void {
    this.gradientDef = d3
      .create("svg:linearGradient")
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

    this.bar = d3
      .create("svg:rect")
      .attr("x", "180")
      .attr("width", 20)
      .attr("height", 300)
      .attr("fill", `url(#gradient-${this.name})`);

    this.minText = d3
      .create("svg:text")
      .attr("x", "175")
      .attr("y", "300")
      .style("text-anchor", "end");

    this.maxText = d3
      .create("svg:text")
      .attr("x", "175")
      .attr("y", "20")
      .attr("text-anchor", "end");
  }

  public updateDomain(domain: [number, number]): void {
    this.scale.domain(domain);
    this.minText.text(domain[0]);
    this.maxText.text(domain[1]);
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
}
