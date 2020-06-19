# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: ptinsight/common/proto/egress/delays.proto

from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='ptinsight/common/proto/egress/delays.proto',
  package='com.dxc.ptinsight.egress',
  syntax='proto3',
  serialized_options=b'\n\036com.dxc.ptinsight.proto.egress',
  create_key=_descriptor._internal_create_key,
  serialized_pb=b'\n*ptinsight/common/proto/egress/delays.proto\x12\x18\x63om.dxc.ptinsight.egress\"j\n\x0f\x44\x65layStatistics\x12\x0f\n\x07geocell\x18\x01 \x01(\x03\x12\x16\n\x0epercentile50th\x18\x02 \x01(\x03\x12\x16\n\x0epercentile90th\x18\x03 \x01(\x03\x12\x16\n\x0epercentile99th\x18\x04 \x01(\x03\x42 \n\x1e\x63om.dxc.ptinsight.proto.egressb\x06proto3'
)




_DELAYSTATISTICS = _descriptor.Descriptor(
  name='DelayStatistics',
  full_name='com.dxc.ptinsight.egress.DelayStatistics',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='geocell', full_name='com.dxc.ptinsight.egress.DelayStatistics.geocell', index=0,
      number=1, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='percentile50th', full_name='com.dxc.ptinsight.egress.DelayStatistics.percentile50th', index=1,
      number=2, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='percentile90th', full_name='com.dxc.ptinsight.egress.DelayStatistics.percentile90th', index=2,
      number=3, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='percentile99th', full_name='com.dxc.ptinsight.egress.DelayStatistics.percentile99th', index=3,
      number=4, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=72,
  serialized_end=178,
)

DESCRIPTOR.message_types_by_name['DelayStatistics'] = _DELAYSTATISTICS
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

DelayStatistics = _reflection.GeneratedProtocolMessageType('DelayStatistics', (_message.Message,), {
  'DESCRIPTOR' : _DELAYSTATISTICS,
  '__module__' : 'ptinsight.common.proto.egress.delays_pb2'
  # @@protoc_insertion_point(class_scope:com.dxc.ptinsight.egress.DelayStatistics)
  })
_sym_db.RegisterMessage(DelayStatistics)


DESCRIPTOR._options = None
# @@protoc_insertion_point(module_scope)