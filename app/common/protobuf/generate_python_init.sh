#!/bin/bash
# Generates the __init__.py file for ptinsight.common.proto to import all protobuf classes

root=$(realpath "$(dirname "$0")")
init_file="${root}/../python/ptinsight/common/proto/__init__.py"
(
    cd $root

    # Clear init file
    > $init_file
    echo "# AUTO-GENERATED, DO NOT EDIT!" >> $init_file

    shopt -s globstar
    for f in **/*.proto; do
        package="${f%.*}"
        echo "from ${package//\//.}_pb2 import *" >> $init_file
    done
    shopt -u globstar
)
