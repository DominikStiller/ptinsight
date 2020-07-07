# Troubleshooting

This is a list of common problems and their solutions.


## AWS

## Cannot connect to EC2 instances

#### Problem

Your connections are refused on all ports like SSH and HTTP.

#### Solution

Check if your IP address block has been added to the allowed ranges in `terraform/variables.tf`.


## Terraform

### AWS Provider Signature Expired

#### Problem
When running a Terraform command, you get the following error:
```
error using credentials to get account ID: error calling sts:GetCallerIdentity: SignatureDoesNotMatch: Signature expired: 20200706T195306Z is now earlier than 20200707T071347Z (20200707T072847Z - 15 min.)
```

#### Solution
Your VM's clock might be out of sync. Update your time from the internet.
