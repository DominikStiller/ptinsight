# Recordings

## Old setup

Kafka + Flink on the same servers
* 3 x t3.xlarge

| ID                  | Checkpointing   | Parallelism | Time Characteristic | Commit                                   |
| ------------------- | --------------- | ----------- | ------------------- | ---------------------------------------- |
| 2020-07-16T16-40-18 | 10 s, unaligned | 2           | Event, 1 s BOOD     | 41f19c7ff10df4ff9a9514d7999ce9687cfc07ff |
| 2020-07-17T09-01-20 | 10 s, unaligned | 2           | Event, 1 s BOOD     | 41f19c7ff10df4ff9a9514d7999ce9687cfc07ff |
| 2020-07-17T13-52-03 | 10 s, unaligned | 2           | Event, 1 s BOOD     | 41f19c7ff10df4ff9a9514d7999ce9687cfc07ff |
| 2020-07-17T15-24-15 | Disabled        | 2           | Event, 1 s BOOD     | 41f19c7ff10df4ff9a9514d7999ce9687cfc07ff |

## New setup
Kafka + Flink on separate servers
* 3x t3.small for Kafka
* 1x t3.micro for Flink master
* 6x t3.xlarge for Flink workers

The task slots per Flink worker are adjusted to the parallelism so only one job runs on a server,

| ID                  | Checkpointing | Parallelism | Time Characteristic | Volume Scaling | Latency Marker Interval | Commit                                   |
| ------------------- | ------------- | ----------- | ------------------- | -------------- | ----------------------- | ---------------------------------------- |
| 2020-07-21T17-54-09 | Disabled      | 2           | Event, 1 s BOOD     | 2x             | 1000                    | 5e02c6426c684af0f894066066fc209556548ed0 |
| 2020-07-21T18-45-26 | Disabled      | 2           | Event, 1 s BOOD     | 1x             | 1000                    | 5e02c6426c684af0f894066066fc209556548ed0 |
| 2020-07-21T18-56-49 | Disabled      | 2           | Event, 1 s BOOD     | 3x             | 1000                    | 5e02c6426c684af0f894066066fc209556548ed0 |


## Abbreviations
* **BOOD**: bounded out of orderness watermarking strategy
