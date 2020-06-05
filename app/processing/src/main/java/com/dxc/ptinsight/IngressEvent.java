package com.dxc.ptinsight;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IngressEvent extends Event {

    @JsonProperty("ingest_ts")
    private Instant ingestionTimestamp;

    public Instant getIngestionTimestamp() {
        return ingestionTimestamp;
    }

    public void setIngestionTimestamp(Instant ingestionTimestamp) {
        this.ingestionTimestamp = ingestionTimestamp;
    }
}
