# Recordings

Latency marker interval is always 1000 ms.


# Setup 1

Infrastructure:
* 1x t3.nano for ingest
* 1x t3.nano for latencytracker
* 3x t3.small for Kafka with 2 partitions per topic
* 1x t3.small for Flink master
* 4x t3.large (1 core x 2 threads = 2 vCPUs) for Flink workers with job parallelism 2 and 2 task slots per worker
  * 2 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: RocksDB
* Memory:
  * `jobmanager.memory.process.size: 1600m`
  * `taskmanager.memory.process.size: 1728m`


| ID                  | Volume Scaling | Data Source                             | Commit                                   |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- |
| 2020-07-22T14-05-43 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 79742e0eda481a0bf50f14be8879509a41d6aa43 |
| 2020-07-22T14-44-02 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 79742e0eda481a0bf50f14be8879509a41d6aa43 |
| 2020-07-22T15-26-33 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 79742e0eda481a0bf50f14be8879509a41d6aa43 |
| 2020-07-22T16-10-46 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 79742e0eda481a0bf50f14be8879509a41d6aa43 |
| 2020-07-22T17-28-28 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 79742e0eda481a0bf50f14be8879509a41d6aa43 |
| 2020-07-23T07-18-07 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 1e7c0b728187b5f25976b958216281b3d1b725df |
| 2020-07-23T08-10-17 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 1e7c0b728187b5f25976b958216281b3d1b725df |
| 2020-07-23T08-50-59 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 6cc3f722323cf51aba0cc05db7e2b9e11aa9a387 |
| 2020-07-23T09-35-16 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 6cc3f722323cf51aba0cc05db7e2b9e11aa9a387 |
| 2020-07-23T10-13-55 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 6cc3f722323cf51aba0cc05db7e2b9e11aa9a387 |
| 2020-07-23T12-19-24 | 1x             | Live                                    | 6cc3f722323cf51aba0cc05db7e2b9e11aa9a387 |


# Setup 2

Infrastructure:
* 1x t3.nano for ingest
* 1x t3.nano for latencytracker
* 3x t3.small for Kafka with 4 partitions per topic
* 1x t3.small for Flink master
* 4x c5.2xlarge (4 cores x 1 threads = 4 vCPUs) for Flink workers with job parallelism 4 and 4 task slots per worker
  * 1 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: RocksDB
* Memory:
  * `jobmanager.memory.process.size: 1600m`
  * `taskmanager.memory.process.size: 1728m`

| ID                  | Volume Scaling | Data Source                             | Commit                                   |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- |
| 2020-07-23T14-19-14 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |
| 2020-07-23T15-00-50 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |
| 2020-07-23T15-33-30 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |
| 2020-07-23T16-06-56 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |
| 2020-07-23T16-41-55 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |


# Setup 3

Infrastructure:
* 1x c5.2xlarge for ingest
* 1x t3.small for latencytracker
* 3x t3.small for Kafka with 4 partitions per topic
* 1x t3.small for Flink master
* 4x c5.2xlarge (4 cores x 1 threads = 4 vCPUs) for Flink workers with job parallelism 4 and 4 task slots per worker
  * 1 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: RocksDB
* Memory:
  * `jobmanager.memory.process.size: 1600m`
  * `taskmanager.memory.process.size: 1728m`

| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment                  |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------------------------ |
| 2020-07-24T08-41-38 | 32x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede | stops working after 30 s |
| 2020-07-24T09-18-07 | 32x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede | stops working after 30 s |
| 2020-07-24T09-44-25 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |                          |
| 2020-07-24T10-18-10 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |                          |


# Setup 4

Infrastructure:
* 1x c5.2xlarge for ingest
* 1x t3.small for latencytracker
* 3x t3.small for Kafka with 4 partitions per topic
* 1x t3.small for Flink master
* 4x c5.2xlarge (4 cores x 1 threads = 4 vCPUs) for Flink workers with job parallelism 4 and 4 task slots per worker
  * 1 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: RocksDB
* Memory:
  * `jobmanager.memory.process.size: 1600m`
  * `taskmanager.memory.process.size: 1728m`

Significant code changes:
* Decrease payload adjustment time range from ±1 s to ±0.1 to prevent too many late data


| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment                                        |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ---------------------------------------------- |
| 2020-07-27T10-25-49 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |                                                |
| 2020-07-27T11-02-10 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |                                                |
| 2020-07-27T11-34-51 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |                                                |
| 2020-07-27T12-07-03 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |                                                |
| 2020-07-27T12-42-29 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |                                                |
| 2020-07-27T14-51-04 | 32x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |                                                |
| 2020-07-27T15-27-28 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 | Emergency stop streaming stops working halfway |
| 2020-07-27T15-59-57 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 | Emergency stop streaming stops working halfway |


# Setup 5

Infrastructure:
* 1x c5.2xlarge for ingest
* 1x t3.small for latencytracker
* 3x t3.small for Kafka with 4 partitions per topic
* 1x c5.2xlarge for Flink master
* 4x c5.2xlarge (4 cores x 1 threads = 4 vCPUs) for Flink workers with job parallelism 4 and 4 task slots per worker
  * 1 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: Memory
* Memory:
  * `jobmanager.memory.flink.size: 12252m`
  * `taskmanager.memory.flink.size: 12252m`

5 minutes between runs to drain the Kafka logs based on retention period

| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-07-27T17-38-04 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |         |
| 2020-07-27T18-12-59 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |         |
| 2020-07-27T18-52-11 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |         |
| 2020-07-27T19-32-32 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |         |
