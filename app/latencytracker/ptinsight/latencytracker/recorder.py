import csv
import os

from ptinsight.common.latency import LatencyMarker


class Recorder:
    def __init__(self, measurement_id: str, index: int):
        os.makedirs("recordings", exist_ok=True)
        self.file = open(
            f"recordings/{measurement_id}_{index}.csv",
            "w",
            encoding="utf-8",
            newline="",
            buffering=1,
        )
        self.writer = csv.writer(self.file)
        self.writer.writerow(LatencyMarker.tuple_columns())

    def write(self, marker: LatencyMarker):
        self.writer.writerow(marker.as_tuple())
