#!/bin/bash

terraform_root=`realpath "$(dirname "$0")"`
(
    cd $terraform_root;
    TF_STATE=. terraform destroy -auto-approve
)
