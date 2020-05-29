# Event-Driven Architecture

This is the repository for the bachelor's thesis project of Dominik Stiller (dominik.stiller@hpe.com). Meeting notes, presentations and formalia can be found in [this repo](https://github.dxc.com/bgloss/dhbw-eda).

Goal of the project is the setup of an example application based on an event-driven architecture to gain experience with this technology.


## Repository Structure
* `ansible`: Ansible roles for common platforms and applications (e.g., Java, Python, Flink)
* `litreview`: A review of literature for EDA and CPE in smart cities
* `playground`: Testing ground for different platforms and applications
    * `1_vagrant-ansible`: Setup of an EC2 instance with an httpd using Vagrant and Ansible
    * `2_terraform-ansible`: Setup of an EC2 instance with an httpd using Terraform and Ansible
    * `3_flink-singlenode`: Flink application with input from MQTT and output to Kafka and AWS Lambda
    * `4_cep`: Simulation and Flink application with CEP that communicate via MQTT
* `tools`: Useful scripts
    * `mqtt-replay`: Recording and replay of MQTT messages
    * `terraform-inventory`: Dynamically generate Ansible inventory from Terraform state
    * `hsl-client`: A collection of scripts for interacting with the HSL.fi API


## Development

### Environment Setup

Follow these steps to set up your development environment on Ubuntu 18.04. Adapt the `apt` commands to your package manager for other Linux distributions.

1. Install the Java 11 JDK
```
sudo apt install openjdk-11-jdk
```

2. Install Python 3.8
```
sudo apt install -y software-properties-common
sudo add-apt-repository -y ppa:deadsnakes/ppa
sudo apt install -y python3.8
```

3. Install Ansible (https://docs.ansible.com/ansible/latest/installation_guide/intro_installation.html)
```
sudo apt update
sudo apt install software-properties-common
sudo apt-add-repository --yes --update ppa:ansible/ansible
sudo apt install ansible
```

4. Install Terraform to local bin (https://www.terraform.io/downloads.html). 
```
wget https://releases.hashicorp.com/terraform/0.12.24/terraform_0.12.24_linux_amd64.zip
unzip terraform_0.12.24_linux_amd64.zip
mv terraform ~/.local/bin  # add this directory to PATH
rm terraform_0.12.24_linux_amd64.zip
```

5. Set up AWS credentials (https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html)
```
mkdir ~/.aws
echo "[default]
aws_access_key_id = ACCESS_KEY_ID
aws_secret_access_key = SECRET_ACCESS_KEY" >> ~/.aws/credentials
```

The user needs following policies attached:
* `AmazonEC2FullAccess`
* `IAMFullAccess`
* `AWSLambdaFullAccess`

6. Generate an SSH key to use for EC2 instances
```
ssh-keygen -t rsa -b 4096 -N "" -f ~/.ssh/id_rsa_eda_deployer
```

### Python
Common tools:
* `pipenv`: dependency management and virtual environments, run `pipenv install --dev` in the project folder to set up the environment
* `black`: code formatter, run `black .` in the project folder to format all .py files


## Abbreviations
* **CPE**: Complex Event Processing.
* **EDA**: Event-Driven Architecture


## Todo for actual application
* Use Route 53/CloudMap for service discovery
* Use remote backend for Terraform
* Use Zookeeper/YARN for high availability
