#!/usr/bin/env python

import argparse
import datetime
import os

import matplotlib.pyplot as plt
import seaborn as sb

sb.set()
sb.set_style("whitegrid")


class Analyzer:
    def __init__(self, recording_file, bin_size):
        self.file = open(recording_file, "r", encoding="utf-8")
        self.bin_size = self._convert_to_seconds(bin_size)

        self.broker = self.file.readline()[8:-1]
        self.topics = self.file.readline()[8:-1]
        self.t_start = datetime.datetime.fromisoformat(
            self.file.readline()[12:-1]
        ).replace(microsecond=0)

    def analyze(self):
        n_msg_total = 0
        n_msg_bins = []
        offsets = []

        t_offset_start = 0

        self.file.readline()
        while True:
            line = self.file.readline()
            if not line:
                print(
                    f"Processed bin {len(n_msg_bins)}: n_msg={n_msg_bins[-1]}   msg/s={n_msg_bins[-1]/self.bin_size:.1f}"
                )
                break

            t_offset = float(line.split(" ", 1)[0])
            if not offsets:
                t_offset_start = t_offset
            t_offset = t_offset - t_offset_start
            offsets.append(t_offset)

            if offsets[-1] // self.bin_size > len(n_msg_bins) - 1:
                if (
                    len(offsets) > 1
                    and offsets[-2] // self.bin_size == len(n_msg_bins) - 1
                ):
                    print(
                        f"Processed bin {len(n_msg_bins)}: n_msg={n_msg_bins[-1]}   msg/s={n_msg_bins[-1]/self.bin_size:.1f}"
                    )
                n_msg_bins.append(0)

            n_msg_total += 1
            n_msg_bins[-1] += 1

        self.file.close()

        print(f"\nBroker: {self.broker}")
        print(f"Start: {str(self.t_start)}")
        print(
            f"Start: {str(self.t_start + datetime.timedelta(seconds=round(offsets[-1])))}"
        )
        print(f"Topics: {self.topics}")
        print(f"Total n_msg: {n_msg_total}")
        print(f"Average msg/s: {n_msg_total / offsets[-1]:.1f}")

        bin_edges = [i * self.bin_size for i in range(0, len(n_msg_bins) + 1)]
        bin_labels = [
            (self.t_start + datetime.timedelta(seconds=n)).strftime("%X")
            for n in bin_edges
        ]

        plt.figure(figsize=[10, 4], facecolor="white")
        plt.hist(offsets, bins=bin_edges, color="black")
        plt.xlabel("Time")
        plt.xticks(bin_edges, bin_labels, rotation=45, ha="right", y=0.03)
        plt.ylabel("Number of messages")
        plt.tight_layout()
        plt.savefig(
            f"{os.path.splitext(os.path.basename(self.file.name))[0]}.png", dpi=300
        )
        plt.show()

    def _convert_to_seconds(self, duration):
        seconds_per_unit = {"s": 1, "m": 60, "h": 3600, "d": 86400, "w": 604800}
        return float(duration[:-1]) * seconds_per_unit[duration[-1]]


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Analyze MQTT messages.")
    parser.add_argument("recording", type=str)
    parser.add_argument("bin_size", type=str)

    args = parser.parse_args()

    analyzer = Analyzer(args.recording, args.bin_size)
    analyzer.analyze()
