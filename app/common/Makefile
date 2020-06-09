.ONESHELL:
.SILENT:

ROOT := $(dir $(realpath $(firstword $(MAKEFILE_LIST))))

default:
	@echo "no default target"

PROTOBUF_SOURCES := $(shell find protobuf -name '*.proto')
proto:
	rm -rf python/ptinsight/common/proto/*
	rm -rf java/src/main/java/com/dxc/ptinsight/proto/*
	protoc --proto_path=protobuf --python_out=python --java_out=java/src/main/java $(PROTOBUF_SOURCES)
	protobuf/generate_python_init.sh
	protobuf/generate_java_typeregistry.sh
