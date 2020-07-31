import os
from typing import Tuple

import matplotlib
import matplotlib.pyplot as plt
import seaborn as sb
from matplotlib.figure import Figure
from pandas import DataFrame
from pandas.core.dtypes.common import is_datetime64_any_dtype, is_numeric_dtype


def plot_init():
    sb.set(
        style="white",
        palette=[
            "#6F2C91",
            "#0067B3",
            "#d9d9d9",
            "#666666",
            "#F7CF2B",
            "#97BE35",
            "#28AEE4",
            "#F38F20",
        ],
        font_scale=1.6,
        rc={
            "lines.linewidth": 1.2,
            "figure.titleweight": "bold",
            "axes.titleweight": "bold",
        },
    )


def plot_subplot(n: int = 1, cols: int = 1, figsize: Tuple[int, int] = None):
    if figsize is None:
        figsize = [22, 7 * n]
    fig, axs = plt.subplots(
        n, cols, sharex="col", figsize=figsize, constrained_layout=True
    )
    if n * cols == 1:
        axs = [axs]
    return fig, axs


def plot_format(fig: Figure = None):
    """Format the plot using the common style"""
    if fig is None:
        fig = plt.gcf()
    for ax in fig.axes:
        ax.get_xaxis().set_minor_locator(matplotlib.ticker.AutoMinorLocator())
        ax.get_yaxis().set_minor_locator(matplotlib.ticker.AutoMinorLocator())

        ax.grid(b=True, which="major", linewidth=1.0)
        ax.grid(b=True, which="minor", linewidth=0.5, linestyle="-.")


def plot_save(name: str, format: str = "png"):
    """Save the plot to the plots folder"""
    if format is not None:
        os.makedirs("plots", exist_ok=True)
        plt.savefig(f"plots/plot_{name}.{format}", dpi=300)


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
