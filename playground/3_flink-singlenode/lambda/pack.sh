#!/bin/bash

# Pack deployment zip in /tmp because VirtualBox does not allow chmod 644 in mounted folder

dir=`realpath "$(dirname "$0")"`
(
    cd $dir
    cp lambda_function.py /tmp
    cd /tmp
    chmod 644 lambda_function.py
    zip deploy.zip lambda_function.py
    mv deploy.zip $dir
)
