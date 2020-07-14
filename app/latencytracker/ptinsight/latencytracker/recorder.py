import csv
import os
from datetime import datetime

from ptinsight.common.latency import LatencyMarkerRecord


class Recorder:
    def __init__(self):
        measurement_id = (
            datetime.now().replace(microsecond=0).isoformat().replace(":", "-")
        )
        os.makedirs(f"recordings", exist_ok=True)
        self.file = open(
            f"recordings/{measurement_id}.csv",
            "w",
            encoding="utf-8",
            newline="",
            buffering=1,
        )
        self.writer = csv.writer(self.file)
        self.writer.writerow(
            [
                "job",
                "ingress_ingestion_timestamp",
                "ingress_consumption_timestamp",
                "egress_ingestion_timestamp",
                "egress_consumption_timestamp",
            ]
        )

    def write(self, marker: LatencyMarkerRecord):
        self.writer.writerow(
            [
                marker.job,
                marker.ingress_ingestion_timestamp,
                marker.ingress_consumption_timestamp,
                marker.egress_ingestion_timestamp,
                marker.egress_consumption_timestamp,
            ]
        )
