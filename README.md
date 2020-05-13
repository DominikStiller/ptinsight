# Event-Driven Architecture

This is the repository for the bachelor's thesis project of Dominik Stiller (dominik.stiller@hpe.com).

Goal of the project is the setup of an example application based on an event-driven architecture to gain experience with this technology.


## Development Environment Setup

Follow these steps to set up your development environment. Ubuntu 18.04 was used, but it should work with any Linux distribution.

1. Install the Java 11 JDK (https://docs.oracle.com/en/java/javase/11/install/overview-jdk-installation.html)
```
wget --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" -P /tmp \
   https://download.oracle.com/otn-pub/java/jdk/11.0.7%2B8/8c7daf89330c48f0b9e32f57169f7bac/jdk-11.0.7_linux-x64_bin.tar.gz
sudo tar xf /tmp/jdk-11.0.7_linux-x64_bin.tar.gz -C /opt
sudo ln -s /opt/jdk-11.0.7/ /opt/java
sudo ln -s /opt/java/bin /usr/bin/*
echo "JAVA_HOME=/opt/java" >> ~/.profile
```

2. Install Ansible (https://docs.ansible.com/ansible/latest/installation_guide/intro_installation.html)
```
sudo apt update
sudo apt install software-properties-common
sudo apt-add-repository --yes --update ppa:ansible/ansible
sudo apt install ansible
```

3. Install Terraform to local bin (https://www.terraform.io/downloads.html). 
```
wget https://releases.hashicorp.com/terraform/0.12.24/terraform_0.12.24_linux_amd64.zip
unzip terraform_0.12.24_linux_amd64.zip
mv terraform ~/.local/bin  # add this directory to PATH
rm terraform_0.12.24_linux_amd64.zip
```

4. Set up AWS credentials (https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html)
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

5. Generate an SSH key to use for EC2 instances
```
ssh-keygen -t rsa -b 4096 -N "" -f ~/.ssh/id_rsa_eda_deployer
```

## Todo for actual application
* Use Route 53/CloudMap for service discovery
* Use remote backend for Terraform
* Use Zookeeper/YARN for high availability
