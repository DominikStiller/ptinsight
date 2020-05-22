# Simple CEP

## Use Case: Pooled Ridesharing
* The city Demotown has 5 locations and 10 people distributed across these locations
    * Locations: Restaurant, Bar, Beach, City Hall, Hotel
    * People: Anna, Brad, Clara, Dave, Emilia, Freddy, Gwen, Harry, Isabell, Jacob
* At random times, people want to ride to another location
* Once 3 people want to ride from one to another location, the pool car brings them tere
* If a person needs to wait more than 10 minutes, the car drives with less than 3 people to avoid deadlocks
* Simulation using simpy, event processing using Flink, communication using MQTT

## Deployment

1. Initialize Terraform
```
make init
```

_Note: Steps 2-4 can be executed in a single command using `make all`_

2. Set up infrastructure using Terraform, you might need to wait a couple of seconds after this for the server to boot
```
make apply
```

3. Install Python, mosquitto and Flink using Ansible, pulling the inventory from the Terraform state. If Ansible reports the server to be unreachable, the server is likely not yet ready. Try again a minute later.
```
make setup
```

4. Deploy simulation and Flink job using Ansible, pulling the inventory from the Terraform state.
```
make deploy
```

### Makefile Targets

* `all`: apply, setup, deploy
* `apply`: Set up AWS infrastructure
* `destroy`: Destroy AWS infrastructure
* `reapply`: Destroy, then set up AWS infrastructure
* `setup`: Install Python, mosquitto and Flink
* `deploy`: Deploy simulation and Flink job

## Running
1. Run simple carpool service to answer order requests
```
./ssh.sh
cd ~/deploy
pipenv run python simulation/mock_carpool_service.py
```

2. Run the simulation
```
./ssh.sh
cd ~/deploy
pipenv run python simulation/main.py
```
