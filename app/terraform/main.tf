provider "aws" {
    version = "~> 2.64"
    region = "eu-central-1"
}



# --------------------------------------------
#      Network
# --------------------------------------------

module "network" {
  source = "./modules/network"
  
  prefix = local.prefix
}



# --------------------------------------------
#      Instances
# --------------------------------------------

resource "aws_key_pair" "deploy" {
    key_name = "${local.prefix}deploy"
    public_key = file("${var.ssh_privatekey}.pub")
}

module "kafka" {
  source = "./modules/clusters/kafka"

  prefix = local.prefix
  vpc = module.network.vpc
  subnet = module.network.subnet
  trusted_cidr = var.trusted_cidr
  keypair = aws_key_pair.deploy
  ssh_privatekey = var.ssh_privatekey
}

module "flink" {
  source = "./modules/clusters/flink"

  prefix = local.prefix
  vpc = module.network.vpc
  subnet = module.network.subnet
  trusted_cidr = var.trusted_cidr
  keypair = aws_key_pair.deploy
  ssh_privatekey = var.ssh_privatekey
  flink_bucket = module.storage.flink_bucket
}

module "ingest" {
  source = "./modules/clusters/ingest"

  prefix = local.prefix
  vpc = module.network.vpc
  subnet = module.network.subnet
  trusted_cidr = var.trusted_cidr
  keypair = aws_key_pair.deploy
  ssh_privatekey = var.ssh_privatekey
}

module "ui" {
  source = "./modules/clusters/ui"

  prefix = local.prefix
  vpc = module.network.vpc
  subnet = module.network.subnet
  trusted_cidr = var.trusted_cidr
  keypair = aws_key_pair.deploy
  ssh_privatekey = var.ssh_privatekey
}

module "latencytracker" {
  source = "./modules/clusters/latencytracker"

  prefix = local.prefix
  vpc = module.network.vpc
  subnet = module.network.subnet
  trusted_cidr = var.trusted_cidr
  keypair = aws_key_pair.deploy
  ssh_privatekey = var.ssh_privatekey
}



# --------------------------------------------
#      Storage
# --------------------------------------------
module "storage" {
  source = "./modules/storage"

  prefix = local.prefix
}
