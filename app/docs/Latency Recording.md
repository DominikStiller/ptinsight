# Latency Recording

Latencies can be recorded using the latency tracker as described [here](Latency Tracker.md). Multiple runs are necessary to test different configurations and increase the sample size.
This document describes the recording process.

1. Stop the ingest and latency tracker components.
```
./ssh.sh ingest
sudo systemctl stop ptinsight-ingest
exit

./ssh.sh latencytracker
sudo systemctl stop ptinsight-latencytracker
exit
```

2. Configure the setup and deploy the affected components. For example, the Flink cluster needs to be redeployed when the Flink configuration changes, and the ingest component needs to be re-deployed when the volume scaling factor changes.

3. Cancel all Flink jobs in the web UI and and re-deploy them. This is necessary because watermarks cannot go backwards when starting the replay again from the beginning, therefore it needs to be reset.
```
make deploy-processing
```

4. Start the latency tracker and ingest components. Recordings should always start from the beginning of the MQTT recording file to ensure comparability.
```
./ssh.sh ingest
sudo systemctl start ptinsight-ingest
exit

./ssh.sh latencytracker
sudo systemctl start ptinsight-latencytracker
exit
```

5. Identify the latest recording file and download it to the analysis folder.
```
./ssh.sh latencytracker
ls -l /opt/ptinsight/latencytracker/recordings
exit

scp centos@[latencytracker-ip]:/opt/ptinsight/latencytracker/recordings/[id].csv analysis/latency/recordings
```

6. Add the recording id and configuration to the [Recordings.md](analysis/latency/Recordings.md) table.
