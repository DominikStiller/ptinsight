# Recordings


# Batch 1

Setup:
* 3x t3.small for Kafka with 2 partitions per topic
* 1x t3.small for Flink master
* 4x t3.large for Flink workers with job parallelism 2

The task slots per Flink worker are adjusted to the parallelism so only one job runs on a server.

| ID                  | Checkpointing | Time Characteristic | Volume Scaling | Latency Marker Interval | Data Source                             | Commit                                   |
| ------------------- | ------------- | ------------------- | -------------- | ----------------------- | --------------------------------------- | ---------------------------------------- |
| 2020-07-22T14-05-43 | Disabled      | Event, 1 s BOOD     | 1x             | 1000                    | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 79742e0eda481a0bf50f14be8879509a41d6aa43 |
| 2020-07-22T14-44-02 | Disabled      | Event, 1 s BOOD     | 2x             | 1000                    | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 79742e0eda481a0bf50f14be8879509a41d6aa43 |
| 2020-07-22T15-26-33 | Disabled      | Event, 1 s BOOD     | 4x             | 1000                    | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 79742e0eda481a0bf50f14be8879509a41d6aa43 |
| 2020-07-22T16-10-46 | Disabled      | Event, 1 s BOOD     | 8x             | 1000                    | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 79742e0eda481a0bf50f14be8879509a41d6aa43 |
| 2020-07-22T17-28-28 | Disabled      | Event, 1 s BOOD     | 16x            | 1000                    | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 79742e0eda481a0bf50f14be8879509a41d6aa43 |
| 2020-07-23T07-18-07 | Disabled      | Event, 1 s BOOD     | 1x             | 1000                    | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 1e7c0b728187b5f25976b958216281b3d1b725df |
| 2020-07-23T08-10-17 | Disabled      | Event, 1 s BOOD     | 2x             | 1000                    | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 1e7c0b728187b5f25976b958216281b3d1b725df |
| 2020-07-23T08-50-59 | Disabled      | Event, 1 s BOOD     | 4x             | 1000                    | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 6cc3f722323cf51aba0cc05db7e2b9e11aa9a387 |
| 2020-07-23T09-35-16 | Disabled      | Event, 1 s BOOD     | 8x             | 1000                    | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 6cc3f722323cf51aba0cc05db7e2b9e11aa9a387 |
| 2020-07-23T10-13-55 | Disabled      | Event, 1 s BOOD     | 16x            | 1000                    | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 6cc3f722323cf51aba0cc05db7e2b9e11aa9a387 |
| 2020-07-23T12-19-24 | Disabled      | Event, 1 s BOOD     | 1x             | 1000                    | Live                                    | 6cc3f722323cf51aba0cc05db7e2b9e11aa9a387 |

# Batch 2

Setup:
* 3x t3.small for Kafka with 4 partitions per topic
* 1x t3.small for Flink master
* 4x c5.2xlarge for Flink workers with job parallelism 4

The task slots per Flink worker are adjusted to the parallelism so only one job runs on a server.

## Abbreviations
* **BOOD**: bounded out of orderness watermarking strategy
