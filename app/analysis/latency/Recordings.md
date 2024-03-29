# Recordings

Latency marker interval is always 1000 ms.


## Setup 1

Infrastructure:
* 1x t3.nano for ingestion
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

Kafka configuration:
* `log.retention.minutes=5`
* `log.retention.check.interval.ms=300000` (5 minutes)


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


## Setup 2

Infrastructure:
* 1x t3.nano for ingestion
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

Kafka configuration:
* `log.retention.minutes=5`
* `log.retention.check.interval.ms=300000` (5 minutes)


| ID                  | Volume Scaling | Data Source                             | Commit                                   |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- |
| 2020-07-23T14-19-14 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |
| 2020-07-23T15-00-50 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |
| 2020-07-23T15-33-30 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |
| 2020-07-23T16-06-56 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |
| 2020-07-23T16-41-55 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |


## Setup 3

Infrastructure:
* 1x c5.2xlarge for ingestion
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

Kafka configuration:
* `log.retention.minutes=5`
* `log.retention.check.interval.ms=300000` (5 minutes)


| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment                  |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------------------------ |
| 2020-07-24T08-41-38 | 32x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede | stops working after 30 s |
| 2020-07-24T09-18-07 | 32x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede | stops working after 30 s |
| 2020-07-24T09-44-25 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |                          |
| 2020-07-24T10-18-10 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | ecb8ff7ce5941a4379e850c9fdc3837cd1841ede |                          |


## Setup 4

Infrastructure:
* 1x c5.2xlarge for ingestion
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

Kafka configuration:
* `log.retention.minutes=5`
* `log.retention.check.interval.ms=300000` (5 minutes)

Significant changes:
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


## Setup 5

Infrastructure:
* 1x c5.2xlarge for ingestion
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


Kafka configuration:
* `log.retention.minutes=5`
* `log.retention.check.interval.ms=300000` (5 minutes)


5 minutes between runs to drain the Kafka logs based on retention period

| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-07-27T17-38-04 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |         |
| 2020-07-27T18-12-59 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |         |
| 2020-07-27T18-52-11 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |         |
| 2020-07-27T19-32-32 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 993d7fef1c8f804c1114f4ecc84d6a6b613fff35 |         |


## Setup 6

Infrastructure:
* 1x c5.4xlarge for ingestion
* 1x t3.small for latencytracker
* 3x t3.large for Kafka with 4 partitions per topic
* 1x t3.large for Flink master
* 4x c5.2xlarge (4 cores x 1 threads = 4 vCPUs) for Flink workers with job parallelism 4 and 4 task slots per worker
  * 1 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: Memory
* Memory:
  * `jobmanager.memory.flink.size: 6114m`
  * `taskmanager.memory.flink.size: 12288m`

Kafka configuration:
* `log.retention.minutes=10`
* `log.retention.check.interval.minutes=10`


Significant changes:
* Changes ingestion from multithreading to multiprocessing for proper parallelization. Before, the scaling could not be achieved due to performance bottlenecks.
* Increase ingestion instance size so each process can have a dedicated vCPU



| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-08-03T20-49-55 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 84658b11446ce76d239c47d17015a02c8a51b876 |         |
| 2020-08-04T12-57-53 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 84658b11446ce76d239c47d17015a02c8a51b876 |         |



## Setup 7

Infrastructure:
* 1x c5.9xlarge for ingestion
* 1x t3.small for latencytracker
* 3x t3.large for Kafka with 4 partitions per topic
* 1x t3.large for Flink master
* 4x c5.xlarge (2 cores x 2 threads = 4 vCPUs) for Flink workers with job parallelism 4 and 4 task slots per worker
  * 2 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: Memory
* Memory:
  * `jobmanager.memory.flink.size: 2048m`
  * `taskmanager.memory.flink.size: 6114m`

Kafka configuration:
* `log.retention.minutes=10`
* `log.retention.check.interval.minutes=10`


Significant changes:
* Increase ingestion instance size to support higher scales
* Decrease Flink worker instance size because c5.2xlarge they had low CPU utilization (1-20% with bursts to 40% on window evaluation)

Observations:
* Flink workers still have low CPU utilization even 32x
* Latencies follow the same pattern but vary a but more with 32x, also it seems that there are less messages written to Kafka than expected with 32x
* Ingest CPU utilization is about 60% on all cores



| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-08-04T13-26-08 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 84658b11446ce76d239c47d17015a02c8a51b876 |         |
| 2020-08-04T14-07-42 | 32x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 84658b11446ce76d239c47d17015a02c8a51b876 |         |
| 2020-08-04T14-49-19 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 84658b11446ce76d239c47d17015a02c8a51b876 |         |
| 2020-08-04T15-21-48 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 84658b11446ce76d239c47d17015a02c8a51b876 |         |



## Setup 8

Infrastructure:
* 1x c5.2xlarge for ingestion
* 1x t3.small for latencytracker
* 3x t3.large for Kafka with 2 partitions per topic
* 1x t3.medium for Flink master
* 4x c5.large (1 cores x 2 threads = 2 vCPUs) for Flink workers with job parallelism 2 and 2 task slots per worker
  * 2 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: Memory
* Memory:
  * `jobmanager.memory.flink.size: 2048m`
  * `taskmanager.memory.flink.size: 6114m`

Kafka configuration:
* `log.retention.minutes=10`
* `log.retention.check.interval.minutes=10`


Significant changes:
* Decrease Flink worker instance size to see scaling effects earlier without needing to use extremely large ingestion instance



| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-08-13T18-35-00 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |         |
| 2020-08-13T19-13-21 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |         |
| 2020-08-13T19-48-57 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |         |
| 2020-08-13T20-22-04 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |         |
| 2020-08-13T20-56-01 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |         |



## Setup 9

Infrastructure:
* 1x c5.4xlarge for ingestion
* 1x t3.small for latencytracker
* 3x t3.large for Kafka with 2 partitions per topic
* 1x t3.small for Flink master
* 4x t3.medium (1 cores x 2 threads = 2 vCPUs) for Flink workers with job parallelism 2 and 2 task slots per worker
  * 2 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: Memory
* Memory:
  * `jobmanager.memory.flink.size: 1024m`
  * `taskmanager.memory.flink.size: 2048m`

Kafka configuration:
* `log.retention.minutes=10`
* `log.retention.check.interval.minutes=10`


Significant changes:
* Decrease Flink worker and increase ingestion instance size to see scaling effects earlier without needing to use extremely large ingestion instance


Observations:
* 16x: CPU utilization: Flink worker 10%-50%, ingestion 60%
* 32x: CPU utilization: Flink worker 2%-10% (delay detection) 60%-100% (others), ingestion 95%
* Amount of messages reaching Flink seems about correct (645k with 16x, 1.2m with 32x) -> ingestion is no bottleneck
* 64x with small ingestion: CPU utilization: ingestion 100%, produces messages very slowly and Flink jobs stop quickly
* 64x with large ingestion: CPU utilization: ingestion 60%, still not full volume, but Kafka is probably bottleneck since only 2 partitions



| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment                       |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ----------------------------- |
| 2020-08-14T08-28-55 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T09-20-02 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T09-52-49 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T10-31-35 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T11-11-09 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T12-13-21 | 32x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T13-14-40 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T13-52-30 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T14-30-08 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T15-08-27 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T15-47-53 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T16-34-22 | 32x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-14T17-24-12 | 64x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | c19af37cab0652eb9271e4ceec43e59d1d2bf3bd |                               |
| 2020-08-18T17-27-51 | 32x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 | with c5.9xlarge for ingestion |
| 2020-08-18T18-09-20 | 32x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 | with c5.9xlarge for ingestion |
| 2020-08-18T18-19-34 | 64x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 | with c5.metal for ingestion   |




## Setup 10
Infrastructure:
* 1x c5.9xlarge for ingestion
* 1x t3.small for latencytracker
* 3x t3.large for Kafka with 2 partitions per topic
* 1x t3.small for Flink master
* 4x t3.medium (1 cores x 2 threads = 2 vCPUs) for Flink workers with job parallelism 2 and 2 task slots per worker
  * 2 tasks per real core

Flink configuration:
* Checkpointing: unaligned, every 10 s, min. 5 s pause
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: RocksDB
* Memory:
  * `jobmanager.memory.flink.size: 1024m`
  * `taskmanager.memory.flink.size: 2048m`

Kafka configuration:
* `log.retention.minutes=10`
* `log.retention.check.interval.minutes=10`


Significant changes:
* Use unaligned checkpointing and RocksDB state backend


Observations:



| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-08-18T08-57-37 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 |         |
| 2020-08-18T09-35-47 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 |         |



## Setup 11
Infrastructure:
* 1x c5.9xlarge for ingestion
* 1x t3.small for latencytracker
* 3x t3.large for Kafka with 2 partitions per topic
* 1x t3.small for Flink master
* 4x t3.medium (1 cores x 2 threads = 2 vCPUs) for Flink workers with job parallelism 2 and 2 task slots per worker
  * 2 tasks per real core

Flink configuration:
* Checkpointing: aligned, every 10 s, min. 5 s pause
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: RocksDB
* Memory:
  * `jobmanager.memory.flink.size: 1024m`
  * `taskmanager.memory.flink.size: 2048m`

Kafka configuration:
* `log.retention.minutes=10`
* `log.retention.check.interval.minutes=10`


Significant changes:
* Use aligned checkpointing


Observations:
* RocksDB is much slower than memory backend
* vehicle count job is backpressured with 16x, probably because RocksDB cannot keep up with storing state (vehicle count job cannot aggregate eagerly due to deduplication)



| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-08-18T10-13-04 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 |         |
| 2020-08-18T10-47-20 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 |         |
| 2020-08-18T14-19-34 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 |         |
| 2020-08-18T14-53-35 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 |         |
| 2020-08-18T15-32-33 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 |         |



## Setup 12
Infrastructure:
* 1x c5.9xlarge for ingestion
* 1x t3.small for latencytracker
* 3x t3.large for Kafka with 2 partitions per topic
* 1x t3.small for Flink master
* 4x t3.medium (1 cores x 2 threads = 2 vCPUs) for Flink workers with job parallelism 2 and 2 task slots per worker
  * 2 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: RocksDB
* Memory:
  * `jobmanager.memory.flink.size: 1024m`
  * `taskmanager.memory.flink.size: 2048m`

Kafka configuration:
* `log.retention.minutes=10`
* `log.retention.check.interval.minutes=10`


Significant changes:
* Disable checkpointing but keep RocksDB state backend


Observations:
* 100% Flink worker CPU utilization for CEP jobs even with 2x
* CEP jobs cannot keep up at 2x while working perfectly at this scale with memory backend
* Much higher memory usage with RocksDB compared to memory state backend



| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-08-18T11-33-11 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 |         |



## Setup 13
Infrastructure:
* 1x c5.9xlarge for ingestion
* 1x t3.small for latencytracker
* 3x t3.large for Kafka with 2 partitions per topic
* 1x t3.small for Flink master
* 4x t3.large (1 cores x 2 threads = 2 vCPUs) for Flink workers with job parallelism 2 and 2 task slots per worker
  * 2 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: RocksDB
* Memory:
  * `jobmanager.memory.flink.size: 1024m`
  * `taskmanager.memory.flink.size: 6144m`

Kafka configuration:
* `log.retention.minutes=10`
* `log.retention.check.interval.minutes=10`


Significant changes:
* Use instance with more memory for Flink workers to possibly improve RocksDB performance


Observations:
* RocksDB performance does not increase when increasing memory (https://ci.apache.org/projects/flink/flink-docs-release-1.11/ops/state/large_state_tuning.html#tuning-rocksdb-memory)
* Seems to not be using more memory even when more is available


| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment                                                                                                   |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| 2020-08-18T13-19-34 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 | `taskmanager.memory.flink.size: 6144m`, `taskmanager.memory.managed.fraction: 0.6`                        |
| 2020-08-18T12-47-40 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 | `taskmanager.memory.flink.size: 6144m`, `taskmanager.memory.managed.fraction: 0.4`                        |
| 2020-08-18T13-30-46 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 | `taskmanager.memory.flink.size: 7169m`, `taskmanager.memory.managed.fraction: 0.7`                        |
| 2020-08-18T13-53-04 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 | `taskmanager.memory.flink.size: 7169m`, `taskmanager.memory.managed.fraction: 0.7`, aligned checkpointing |



## Setup 14
Infrastructure:
* 1x c5.9xlarge for ingestion
* 1x t3.small for latencytracker
* 3x t3.large for Kafka with 2 partitions per topic
* 1x t3.small for Flink master
* 4x t3.medium (1 cores x 2 threads = 2 vCPUs) for Flink workers with job parallelism 2 and 2 task slots per worker
  * 2 tasks per real core

Flink configuration:
* Checkpointing: aligned, every 10 s, min. 5 s pause
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: Memory
* Memory:
  * `jobmanager.memory.flink.size: 1024m`
  * `taskmanager.memory.flink.size: 2048m`

Kafka configuration:
* `log.retention.minutes=10`
* `log.retention.check.interval.minutes=10`


Significant changes:
* Use aligned checkpointing with memory backend


Observations:
* RocksDB is much slower than memory backend
* vehicle count job is backpressured with 16x, probably because RocksDB cannot keep up with storing state (vehicle count job cannot aggregate eagerly due to deduplication)
* Checkpointing with memory backend fails becaus jobmanager memory is insufficient, also not recommended for production



| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-08-18T16-27-50 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 8efda848519a764e9f1c3e6132c45cdcfb81b767 |         |



## General Observations
* When ingestion does not degrade performance, there is a sawtooth pattern with the period of the Kafka log retention check interval (i.e. latency increases every time logs are deleted)
* When Kafka latency spikes, it does so for all jobs



## Setup 15
Infrastructure:
* 1x c5.4xlarge for ingestion
* 1x m5.large for latencytracker
* 3x m5.xlarge for Kafka with 4 partitions per topic
* 1x t3.small for Flink master
* 4x m5.xlarge (2 cores x 2 threads = 4 vCPUs) for Flink workers with job parallelism 2 and 2 task slots per worker
  * 0.5 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: RocksDB
* Memory:
  * `jobmanager.memory.flink.size: 1024m`
  * `taskmanager.memory.flink.size: 12288m`
  * `taskmanager.memory.managed.fraction: 0.6`

Kafka configuration:
* `log.retention.minutes=10`
* `log.retention.check.interval.minutes=10`

Significant changes:
* Use better Flink worker instance to check if RocksDB bottleneck for CEP jobs can be removed

Observations:
* CEP and RocksDB do not work well even at 1x



| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-08-24T20-59-28 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 308e611599e0836258c9c3859d577383af62c993 |         |
| 2020-08-24T21-09-29 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 308e611599e0836258c9c3859d577383af62c993 |         |



## Setup 16
Infrastructure:
* 1x c5.9xlarge for ingestion
* 1x m5.large for latencytracker
* 3x m3.xlarge for Kafka with 4 partitions per topic
* 1x t3.small for Flink master
* 4x t3.medium (1 cores x 2 threads = 2 vCPUs) for Flink workers with job parallelism 2 and 2 task slots per worker
  * 2 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: Memory
* Memory:
  * `jobmanager.memory.flink.size: 1024m`
  * `taskmanager.memory.flink.size: 2048m`

Kafka configuration:
* `log.retention.minutes=10`
* `log.retention.check.interval.minutes=10`

Significant changes:
* Like setup 9 but with better Kafka instances and 4 instead of 2 partitions

Observations:
* Ingestion is way too slow at 64x -> fault of 4 partitions or not using c5.metal?


| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-08-24T21-32-22 | 64x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 308e611599e0836258c9c3859d577383af62c993 |         |



## Setup 17
Infrastructure:
* 1x c5.24xlarge for ingestion
* 1x m5.xlarge for latencytracker
* 4x m5.xlarge for Kafka with 4 partitions per topic
* 1x t3.medium for Flink master
* 4x t3.medium (1 cores x 2 threads = 2 vCPUs) for Flink workers with job parallelism 2 and 2 task slots per worker
  * 2 tasks per real core

Flink configuration:
* Checkpointing: disabled
* Time characteristic: Event time, 1 s bounded out of orderness watermarking
* State backend: Memory
* Memory:
  * `jobmanager.memory.flink.size: 2048m`
  * `taskmanager.memory.flink.size: 2048m`

Kafka configuration:
* `log.retention.minutes=15`
* `log.retention.check.interval.minutes=15`

Significant changes:
* Like setup 16 but with more and better Kafka instances, and better ingestion instance

Observations:
* Tasks from one job not all on same node
* 32x: Flink worker 20%-70%, latencytracker 80%-90% (one core)
* 64x: Flink worker 0%-30%, latencytracker 10% (one core), Kafka load also low -> ingestion is bottleneck, starts already at 48x



| ID                  | Volume Scaling | Data Source                             | Commit                                   | Comment |
| ------------------- | -------------- | --------------------------------------- | ---------------------------------------- | ------- |
| 2020-08-25T11-50-37 | 1x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 5db2beda702f04363c4eea22714e1bf18cb94b22 |         |
| 2020-08-25T12-22-47 | 2x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 5db2beda702f04363c4eea22714e1bf18cb94b22 |         |
| 2020-08-25T12-59-34 | 4x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 5db2beda702f04363c4eea22714e1bf18cb94b22 |         |
| 2020-08-25T13-57-07 | 8x             | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 5db2beda702f04363c4eea22714e1bf18cb94b22 |         |
| 2020-08-25T15-10-58 | 16x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 5db2beda702f04363c4eea22714e1bf18cb94b22 |         |
| 2020-08-25T15-46-03 | 32x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 5db2beda702f04363c4eea22714e1bf18cb94b22 |         |
| 2020-08-25T16-19-05 | 48x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 5db2beda702f04363c4eea22714e1bf18cb94b22 |         |
| 2020-08-25T17-25-07 | 64x            | mqtt.hsl.fi/2020-06-02T10-31-46.rec.bz2 | 5db2beda702f04363c4eea22714e1bf18cb94b22 |         |
