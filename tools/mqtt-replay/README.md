# MQTT Recording and Replay

This tool can record MQTT messages from a broker and replay them at a later point.


## Prerequisites
* Python 3.8
* pipenv (`pip install pipenv`)


## Usage

Install dependencies (`pipenv install`) and enter the pipenv (`pipenv shell`) before using this tool.

### Recording
Usage: `./record.py [-v] host port duration topics`  
Example: `./record.py mqtt.hsl.fi 8883 10s /hfp/v2/journey/#`

Record MQTT messages (time offset, topic, qos, retain, payload) from a broker for a certain duration. The recording
 is saved in `recordings/host/time.rec`.
* `host`: the broker's host
* `port`: the broker's port
* `duration`: the duration of the recording
    * e.g. `5s`, `30m`, `0.75h`, `1d`, `2w`
* `topics`: the list of topics to record, separated by whitespace
    * usually brokers support [wildcards](https://www.hivemq.com/blog/mqtt-essentials-part-5-mqtt-topics-best-practices/#wildcards)
* `-v`: enable debug log

### Replay
Usage: `./replay.py [-v] host port recording`  
Example: `./replay.py localhost 1883 recordings/mqtt.hsl.fi/time.rec`

Replay MQTT messages to some broker with the same relative timing as the original.
* `host`: the broker's host
* `port`: the broker's port
* `recording`: the path to the recording file
* `-v`: enable debug log

### Analyze
Usage: `./analyze.py recording bin_size`  
Example: `./analyze.py recordings/mqtt.hsl.fi/time.rec 10m`

Analyze a recording and print metrics.
* `recording`: the path to the recording file
* `bin_size`: the size for histogram bins (same format at `duration` in `record.py`)


## Deployment
This tool can be deployed to EC2.

1. Initialize Terraform
```
make init
```

2. Set up infrastructure, you might need to wait a couple of seconds after this for the server to boot
```
make apply
```

3. Set up server, deploy tool, start recording and upload to S3 afterwards
```
make deploy
```

### Makefile Targets

* `all`: apply, deploy
* `apply`: Set up AWS infrastructure
* `destroy`: Destroy AWS infrastructure
* `reapply`: Destroy, then set up AWS infrastructure
* `deploy`: Set up und run
