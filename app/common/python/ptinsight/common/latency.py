from datetime import datetime

from ptinsight.common import Event


class LatencyMarkerRecord:
    def __init__(self):
        self.job = None
        self.ingress_ingestion_timestamp = None
        self.ingress_consumption_timestamp = None
        self.egress_ingestion_timestamp = None
        self.egress_consumption_timestamp = None

    def mark_ingress(self, event: Event):
        self.ingress_ingestion_timestamp = event.ingestion_timestamp.ToMilliseconds()
        self.ingress_consumption_timestamp = int(datetime.now().timestamp() * 1000)

    def mark_egress(self, event: Event):
        self.egress_ingestion_timestamp = event.ingestion_timestamp.ToMilliseconds()
        self.egress_consumption_timestamp = int(datetime.now().timestamp() * 1000)

    def latency_end_to_end(self):
        return self.egress_consumption_timestamp - self.ingress_ingestion_timestamp

    def latency_ingest_to_processing(self):
        return self.ingress_consumption_timestamp - self.ingress_ingestion_timestamp

    def latency_processing(self):
        return self.egress_ingestion_timestamp - self.ingress_consumption_timestamp

    def latency_processing_to_ui(self):
        return self.egress_consumption_timestamp - self.egress_ingestion_timestamp
