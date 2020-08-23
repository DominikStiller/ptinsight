#!/bin/bash
# Generates the type registry for JSON serialization of Any classes

java_dir="$(realpath "$(dirname "$0")")/../java/src/main/java/com/dxc/ptinsight/proto"
(
    cd $java_dir

    imports=""
    adds=""

    shopt -s globstar
    for f in **/*.java; do
        if [[ $f != "Registry.java" ]]; then
            # Remove extension
            path="${f%.*}"
            adds+="    .add(com.dxc.ptinsight.proto.${path/\//.}.getDescriptor().getMessageTypes())"$'\n'
        fi
    done
    shopt -u globstar

    cat > $java_dir/Registry.java <<-EOF 
// AUTO-GENERATED, DO NOT EDIT!
package com.dxc.ptinsight.proto;

$imports
import static com.google.protobuf.util.JsonFormat.TypeRegistry;

public class Registry {
  public static final TypeRegistry INSTANCE = TypeRegistry.newBuilder()
$adds    .build();
}
EOF
)
