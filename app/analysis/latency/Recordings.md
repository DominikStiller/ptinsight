# Recordings


Setup:
* 3x t3.small for Kafka
* 1x t3.small for Flink master
* 6x t3.large for Flink workers

The task slots per Flink worker are adjusted to the parallelism so only one job runs on a server,

| ID                  | Checkpointing | Parallelism | Time Characteristic | Volume Scaling | Latency Marker Interval | Commit                                   |
| ------------------- | ------------- | ----------- | ------------------- | -------------- | ----------------------- | ---------------------------------------- |

| 2020-07-22T14-05-43 | Disabled      | 2           | Event, 1 s BOOD     | 1x             | 1000                    | 79742e0eda481a0bf50f14be8879509a41d6aa43  |
| 2020-07-22T14-44-02 | Disabled      | 2           | Event, 1 s BOOD     | 2x             | 1000                    | 79742e0eda481a0bf50f14be8879509a41d6aa43  |
| 2020-07-22T15-26-33 | Disabled      | 2           | Event, 1 s BOOD     | 4x             | 1000                    | 79742e0eda481a0bf50f14be8879509a41d6aa43  |
| 2020-07-22T16-10-46 | Disabled      | 2           | Event, 1 s BOOD     | 8x             | 1000                    | 79742e0eda481a0bf50f14be8879509a41d6aa43  |
| 2020-07-22T17-28-28 | Disabled      | 2           | Event, 1 s BOOD     | 16x             | 1000                    | 79742e0eda481a0bf50f14be8879509a41d6aa43  |


## Abbreviations
* **BOOD**: bounded out of orderness watermarking strategy
