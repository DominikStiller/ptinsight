# Troubleshooting

This is a list of common problems and possible solutions.


## AWS

### Cannot connect to EC2 instances
#### Problem
Your connections are refused on all ports like SSH and HTTP.
#### Solution
Check if your IP address block has been added to the allowed ranges in `terraform/variables.tf`.


## Flink

### Windows are not triggered when using event time
#### Problem
When using the event time stream characteristic, event time windows are not being triggered. This often happens when switching between recorded and live data.
#### Solution
Operator watermarks always increase monotonously. When recorded data with a much earlier timestamp than the current time are processed after the job already processed live data, the watermark has advanced too far and the recorded data will be dropped. Restart the job to reset watermarks.


## Gradle

### Cannot access cache
#### Problem
When executing Gradle commands, a long exception message mentioning cache locks is thrown.
#### Solution
Check if another Gradle instance is running, possibly on the VM guest or host.

### Cannot create services
#### Problem
When executing Gradle commands, a long exception message mentioning failed service creation is thrown.
#### Solution
Stop your Gradle daemon using `./gradlew --stop`. The next time you run a command, a new daemon instance will be started.


## Terraform

### AWS Provider Signature Expired
#### Problem
When running a Terraform command, you get the following error:
```
error using credentials to get account ID: error calling sts:GetCallerIdentity: SignatureDoesNotMatch: Signature expired: 20200706T195306Z is now earlier than 20200707T071347Z (20200707T072847Z - 15 min.)
```
#### Solution
Your VM's clock might be out of sync. Update your time from the internet.
