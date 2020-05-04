# Event-Driven Architecture

This is the repository for the bachelor's thesis project of Dominik Stiller (dominik.stiller@hpe.com).

Goal of the project is the setup of an example application based on an event-driven architecture to gain experience with this technology.


## Development Environment Setup

Follow these steps to set up your development environment. Ubuntu 18.04 was used, but it should work with any Linux distribution.

1. Install Ansible (https://docs.ansible.com/ansible/latest/installation_guide/intro_installation.html)
```
sudo apt update
sudo apt install software-properties-common
sudo apt-add-repository --yes --update ppa:ansible/ansible
sudo apt install ansible
```

2. Install the AWS CLI (https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2-linux.html)
```
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

3. Install Vagrant (https://www.vagrantup.com/downloads.html) using the .deb package

4. Install the Vagrant AWS plugin (https://github.com/mitchellh/vagrant-aws)
```
vagrant plugin install vagrant-aws
```

If you get the error `ovirt-engine-sdk requires Ruby version >= 2.5`, try replacing Vagrant's Ruby with a newer version. (https://github.com/hashicorp/vagrant/issues/11518)
```
cd /opt/vagrant/embedded/bin
sudo mv ruby ruby.old
sudo ln -s `which ruby` ruby
vagrant plugin install vagrant-aws
```

5. Add a dummy box for the Vagrant AWS provider.
```
vagrant box add dummy https://github.com/mitchellh/vagrant-aws/raw/master/dummy.box
```

6. Create a vault directory.
```
mkdir ~/.vault
chmod 700 ~/.vault
```

7. Set up your AWS deployment user credentials in `~/.vault/aws-credentials.yaml`.
```
key_id: ACCESS_KEY_ID
secret: SECRET_ACCESS_KEY
ssh_key: SSH_KEY_NAME
```

8. Run `vagrant up` and enjoy.
