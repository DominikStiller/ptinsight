# Latency Recording

Latencies can be recorded using the latency tracker which is described [here](Latency%20Tracking.md). Multiple runs are necessary to test different configurations and increase the sample size.
This document describes the recording process.

1. Stop the ingestion and latency tracker components.
```
./ssh.sh ingestion
sudo systemctl stop ptinsight-ingestion
exit

./ssh.sh latencytracker
sudo systemctl stop ptinsight-latencytracker
exit
```

2. Configure the setup and deploy the affected components. For example, the Flink cluster needs to be redeployed when the Flink configuration changes, and the ingestion component needs to be re-deployed when the volume scaling factor changes.

3. Cancel all Flink jobs in the web UI and and re-deploy them. This is necessary because watermarks cannot go backwards when starting the replay again from the beginning, therefore it needs to be reset.
```
make deploy-processing
```

4. Start the latency tracker and ingestion components. Recordings should always start from the beginning of the MQTT recording file to ensure comparability.
```
./ssh.sh ingestion
sudo systemctl start ptinsight-ingestion
exit

./ssh.sh latencytracker
sudo systemctl start ptinsight-latencytracker
exit
```

5. Identify the latest recording files and download it to the analysis folder.
```
./ssh.sh latencytracker
ls -l /opt/ptinsight/latencytracker/recordings
exit

scp -i ~/.ssh/id_rsa_ptinsight_deploy centos@[latencytracker-ip]:/opt/ptinsight/latencytracker/recordings/[id]_* analysis/latency/recordings
```

6. Add the recording id and configuration to the [Recordings.md](analysis/latency/Recordings.md) table.

7. You might also want to monitor the CPU load and memory usage of all components for bottlenecks.
```
./ssh.sh [component]
htop
```
