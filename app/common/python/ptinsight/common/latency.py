from datetime import datetime

from pandas import DataFrame

from ptinsight.common import Event


class LatencyMarker:
    def __init__(self):
        self.seen_jobs = []
        self.event_timestamp = None
        self.ingress_ingestion_timestamp = None
        self.ingress_consumption_timestamp = None
        self.egress_ingestion_timestamp = None
        self.egress_consumption_timestamp = None

    def mark_ingress(self, event: Event):
        self.event_timestamp = event.event_timestamp.ToMilliseconds()
        self.ingress_ingestion_timestamp = event.ingestion_timestamp.ToMilliseconds()
        self.ingress_consumption_timestamp = int(datetime.now().timestamp() * 1000)

    def mark_egress(self, event: Event, job: str):
        self.seen_jobs.append(job)
        self.egress_ingestion_timestamp = event.ingestion_timestamp.ToMilliseconds()
        self.egress_consumption_timestamp = int(datetime.now().timestamp() * 1000)

    def as_tuple(self):
        return (
            self.seen_jobs[-1],
            self.event_timestamp,
            self.ingress_ingestion_timestamp,
            self.ingress_consumption_timestamp,
            self.egress_ingestion_timestamp,
            self.egress_consumption_timestamp,
        )

    @staticmethod
    def tuple_columns():
        return (
            "job",
            "event_timestamp",
            "ingress_ingestion_timestamp",
            "ingress_consumption_timestamp",
            "egress_ingestion_timestamp",
            "egress_consumption_timestamp",
        )


def calculate_latencies(df: DataFrame) -> None:
    df["time_lag"] = (
        df["ingress_ingestion_timestamp"] - df["event_timestamp"]
    )
    df["latency_end_to_end"] = (
        df["egress_consumption_timestamp"] - df["ingress_ingestion_timestamp"]
    )
    df["latency_ingest_to_processing"] = (
        df["ingress_consumption_timestamp"] - df["ingress_ingestion_timestamp"]
    )
    df["latency_processing"] = (
        df["egress_ingestion_timestamp"] - df["ingress_consumption_timestamp"]
    )
    df["latency_processing_to_ui"] = (
        df["egress_consumption_timestamp"] - df["egress_ingestion_timestamp"]
    )
