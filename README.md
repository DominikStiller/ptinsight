# Event Stream Analytics

This is the repository for the bachelor's thesis project of Dominik Stiller (dominik.stiller@hpe.com). Goal of the project is the setup of an example application for real-time event stream analytics to gain experience with this technology.


## Repository Structure
* `ansible`: Ansible roles for common platforms and applications (e.g., Java, Python, Flink)
* `app`: The main proof of concept application "PT Insight"
* `litreview`: A review of literature for stream analytics in smart cities
* `playground`: Testing ground for different platforms and applications
    * `1_vagrant-ansible`: Setup of an EC2 instance with an httpd using Vagrant and Ansible
    * `2_terraform-ansible`: Setup of an EC2 instance with an httpd using Terraform and Ansible
    * `3_flink-singlenode`: Flink application with input from MQTT and output to Kafka and AWS Lambda
    * `4_cep`: Simulation and Flink application with event processing that communicate via MQTT
* `tools`: Useful scripts
    * `mqtt-replay`: Recording and replay of MQTT messages
    * `terraform-inventory`: Dynamically generate Ansible inventory from Terraform state
    * `hsl-client`: A collection of scripts for interacting with the HSL.fi API


## Development

### Environment Setup

Follow these steps to set up your development environment on Ubuntu 18.04. Adapt to your package manager for other Linux distributions.

1. Install common packages
```
sudo apt install -y make software-properties-common
```

2. Install the Java 11 JDK
```
sudo apt install -y openjdk-11-jdk
```

3. Install Python 3.8 and Poetry
```
sudo add-apt-repository -y ppa:deadsnakes/ppa
sudo apt install -y python3.8 python3.8-venv python3.8-distutils
wget -O - https://raw.githubusercontent.com/python-poetry/poetry/master/get-poetry.py | python
```

4. Install Ansible
```
sudo apt-add-repository -y ppa:ansible/ansible
sudo apt install -y ansible
```

5. Install Terraform
```
wget -P /tmp https://releases.hashicorp.com/terraform/0.12.24/terraform_0.12.24_linux_amd64.zip
sudo unzip -d /usr/local/bin /tmp/terraform_*.zip
rm /tmp/terraform_*.zip
```

6. Install Node.js using Node Version Manager
```
wget -O- https://raw.githubusercontent.com/nvm-sh/nvm/v0.35.3/install.sh | bash
nvm install --lts
```

7. Install protoc
```
wget -P /tmp https://github.com/protocolbuffers/protobuf/releases/download/v3.12.3/protoc-3.12.3-linux-x86_64.zip
sudo unzip -d /opt/protoc /tmp/protoc-*.zip
rm /tmp/protoc-*.zip
sudo chmod -R 755 /opt/protoc
sudo ln -s /opt/protoc/bin/protoc /usr/local/bin
```

8. Set up AWS credentials (see https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html)
```
mkdir -p ~/.aws
echo "[default]
aws_access_key_id = ACCESS_KEY_ID
aws_secret_access_key = SECRET_ACCESS_KEY" >> ~/.aws/credentials
```

The user needs following policies attached:
* `AmazonEC2FullAccess`
* `IAMFullAccess`
* `AWSLambdaFullAccess`

9. Generate an SSH key to use for EC2 instances
```
ssh-keygen -t rsa -b 4096 -N "" -f ~/.ssh/id_rsa_ptinsight_deploy
```

10. Logout and login again to reload the environment

### Common Tools
We use common tools for consistency. Execute the commands below in the project's folder.

#### Java
* Build & dependency management: [Gradle](https://gradle.org/). No environment setup is necessary.
* Code style: [Google Style](https://google.github.io/styleguide/javaguide.html). Run `./gradlew spotlessJavaApply` to format all .java files.

#### Python
* Build & dependency management: [poetry](https://python-poetry.org/). Run `poetry install` to set up the environment. You might have to explicitly specify the Python version before using `poetry env use python3.8`. Use the venv from `poetry env info` as interpreter in PyCharm.
* Code style: [black](https://black.readthedocs.io/en/stable/). Run `poetry run black .` to format all .py files.

#### TypeScript
* Build & dependency management: [npm](https://www.npmjs.com/). Run `npm install` to set up the environment.
* Code style: [Prettier](https://prettier.io/). Run `npm run format` to format all web frontend files.


## Abbreviations
* **protobuf**: [Protocol Buffers](https://developers.google.com/protocol-buffers)

