# Terraform + Ansible

Set up an EC2 instance with Vagrant and install an Apache httpd via Ansible.
Uses a local backend for storing Terraform state.

## Deployment

1. Initialize Terraform
```
terraform init
```

2. Set up infrastructure specified in `main.tf` using Terraform
```
terraform apply
```

3. Deploy Apache HTTP server using Ansible, pulling the inventory from the Terraform state.
```
./deploy_httpd.sh
```

4. Open the host name from 2. or the IP from 3. in your browser.

