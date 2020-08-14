import os
from typing import Tuple

import matplotlib
import matplotlib.pyplot as plt
import seaborn as sb
from matplotlib.figure import Figure
from matplotlib.axes import Axes
from pandas import DataFrame
from pandas.core.dtypes.common import is_datetime64_any_dtype, is_numeric_dtype


PALETTE = [
    "#6F2C91",
    "#0067B3",
    "#d9d9d9",
    "#666666",
    "#F7CF2B",
    "#97BE35",
    "#28AEE4",
    "#F38F20",
]


class Plotter:
    def __init__(
        self, context="notebook", save_folder: str = "plots", layout: str = "tight"
    ):
        self.context = context
        self.save_folder = save_folder
        self.layout = layout

        if context == "notebook":
            sb.set(
                style="white",
                palette=PALETTE,
                font_scale=1.6,
                rc={
                    "lines.linewidth": 1.2,
                    "figure.titleweight": "bold",
                    "axes.titleweight": "bold",
                },
            )
            self.default_figsize = [22, 7]
        elif context == "paper":
            sb.set(
                context="paper",
                style="white",
                palette=PALETTE,
                font="serif",
                font_scale=1.6,
                rc={
                    "lines.linewidth": 1.2,
                    "font.serif": "Latin Modern Roman",
                    "mathtext.fontset": "custom",
                    "mathtext.it": "Latin Modern Math:italic",
                    "mathtext.cal": "Latin Modern Math",
                    "mathtext.rm": "Latin Modern Math",
                    "axes.titleweight": "bold",
                },
            )
            self.default_figsize = [12, 4]
        else:
            raise Exception("Unsupported context")

    def make_subplots(
        self, rows: int = 1, cols: int = 1, figsize: Tuple[int, int] = None
    ):
        if figsize is None:
            figsize = [self.default_figsize[0], self.default_figsize[1] * rows]
        fig, axs = plt.subplots(
            rows,
            cols,
            sharex="col",
            figsize=figsize,
            constrained_layout=(self.layout == "constrained"),
        )
        if rows * cols == 1:
            axs = [axs]
        return fig, axs

    def make_marginal_plot(self, figsize: Tuple[int, int] = None):
        # https://matplotlib.org/3.2.1/gallery/lines_bars_and_markers/scatter_hist.html
        if figsize is None:
            figsize = self.default_figsize
        fig = plt.figure(figsize=figsize)
        gs = fig.add_gridspec(
            2,
            2,
            width_ratios=(7, 0.5),
            height_ratios=(0.5, 7),
            left=0.1,
            right=0.9,
            bottom=0.1,
            top=0.9,
            wspace=0,
            hspace=0,
        )

        ax_main = fig.add_subplot(gs[1, 0])
        ax_marginal_x = fig.add_subplot(gs[0, 0], sharex=ax_main)
        ax_marginal_y = fig.add_subplot(gs[1, 1], sharey=ax_main)

        # Remove ticks and spines
        plt.setp(ax_marginal_x.get_xticklabels(), visible=False)
        plt.setp(ax_marginal_x.get_yticklabels(), visible=False)
        plt.setp(ax_marginal_y.get_xticklabels(), visible=False)
        plt.setp(ax_marginal_y.get_yticklabels(), visible=False)

        sb.despine(ax=ax_marginal_x, left=True)
        sb.despine(ax=ax_marginal_y, bottom=True)

        return fig, ax_main, ax_marginal_x, ax_marginal_y

    def format(self, fig: Figure = None, space_factor: int = 1):
        """Format the plot using the common style"""
        if fig is None:
            fig = plt.gcf()
        for ax in fig.axes:
            self.add_grid(ax)
        if self.layout == "tight":
            self.apply_tight_layout(fig)

    def add_grid(self, ax: Axes):
        ax.get_xaxis().set_minor_locator(matplotlib.ticker.AutoMinorLocator())
        ax.get_yaxis().set_minor_locator(matplotlib.ticker.AutoMinorLocator())

        ax.grid(b=True, which="major", linewidth=1.0)
        ax.grid(b=True, which="minor", linewidth=0.5, linestyle="-.")

    def apply_tight_layout(self, fig: Figure, space_factor: int = 1):
        fig.tight_layout(
            pad=0.1 * space_factor, h_pad=0.4 * space_factor, w_pad=0.4 * space_factor
        )

    def save(self, name: str):
        """Save the plot to the plots folder"""
        if self.context == "paper":
            format = "pdf"
        else:
            format = "png"
        os.makedirs(self.save_folder, exist_ok=True)
        plt.savefig(
            f"{self.save_folder}/plot_{name}.{format}",
            dpi=300,
            bbox_inches="tight",
            pad_inches=0,
        )


def add_relative_time(data: DataFrame, col: str):
    """Add time column starting at 0 s based on the first timestamp"""
    data.sort_values(col, inplace=True)
    data.reset_index(inplace=True)
    if is_numeric_dtype(data[col]):
        data["t"] = (data[col] - data[col][0]) / 1000
    elif is_datetime64_any_dtype(data[col]):
        data["t"] = (data[col] - data[col][0]).astype("timedelta64[s]")
    else:
        raise ValueError("Invalid time column type")
