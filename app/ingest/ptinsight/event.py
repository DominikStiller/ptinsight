from dataclasses import dataclass
from datetime import datetime


@dataclass
class Event:
    event_timestamp: datetime
    ingestion_timestamp: datetime
    payload: dict

    def to_dict(self) -> dict:
        return {
            "event_ts": self.event_timestamp.isoformat(),
            "ingest_ts": self.ingestion_timestamp.isoformat(),
            "payload": self.payload,
        }
