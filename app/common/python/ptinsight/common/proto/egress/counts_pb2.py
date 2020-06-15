# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: ptinsight/common/proto/egress/counts.proto

from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from ptinsight.common.proto import base_pb2 as ptinsight_dot_common_dot_proto_dot_base__pb2
from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='ptinsight/common/proto/egress/counts.proto',
  package='com.dxc.ptinsight.egress',
  syntax='proto3',
  serialized_options=b'\n\036com.dxc.ptinsight.proto.egress',
  create_key=_descriptor._internal_create_key,
  serialized_pb=b'\n*ptinsight/common/proto/egress/counts.proto\x12\x18\x63om.dxc.ptinsight.egress\x1a!ptinsight/common/proto/base.proto\x1a\x1fgoogle/protobuf/timestamp.proto\"\xb5\x01\n\x0c\x41rrivalCount\x12\x34\n\x0cvehicle_type\x18\x01 \x01(\x0e\x32\x1e.com.dxc.ptinsight.VehicleType\x12\x30\n\x0cwindow_start\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12.\n\nwindow_end\x18\x03 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\r\n\x05\x63ount\x18\x04 \x01(\x05\"\xb7\x01\n\x0e\x44\x65partureCount\x12\x34\n\x0cvehicle_type\x18\x01 \x01(\x0e\x32\x1e.com.dxc.ptinsight.VehicleType\x12\x30\n\x0cwindow_start\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12.\n\nwindow_end\x18\x03 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\r\n\x05\x63ount\x18\x04 \x01(\x05\".\n\x0cVehicleCount\x12\x0f\n\x07h3index\x18\x01 \x01(\x03\x12\r\n\x05\x63ount\x18\x02 \x01(\x05\x42 \n\x1e\x63om.dxc.ptinsight.proto.egressb\x06proto3'
  ,
  dependencies=[ptinsight_dot_common_dot_proto_dot_base__pb2.DESCRIPTOR,google_dot_protobuf_dot_timestamp__pb2.DESCRIPTOR,])




_ARRIVALCOUNT = _descriptor.Descriptor(
  name='ArrivalCount',
  full_name='com.dxc.ptinsight.egress.ArrivalCount',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='vehicle_type', full_name='com.dxc.ptinsight.egress.ArrivalCount.vehicle_type', index=0,
      number=1, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='window_start', full_name='com.dxc.ptinsight.egress.ArrivalCount.window_start', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='window_end', full_name='com.dxc.ptinsight.egress.ArrivalCount.window_end', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='count', full_name='com.dxc.ptinsight.egress.ArrivalCount.count', index=3,
      number=4, type=5, cpp_type=1, label=1,
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
  serialized_start=141,
  serialized_end=322,
)


_DEPARTURECOUNT = _descriptor.Descriptor(
  name='DepartureCount',
  full_name='com.dxc.ptinsight.egress.DepartureCount',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='vehicle_type', full_name='com.dxc.ptinsight.egress.DepartureCount.vehicle_type', index=0,
      number=1, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='window_start', full_name='com.dxc.ptinsight.egress.DepartureCount.window_start', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='window_end', full_name='com.dxc.ptinsight.egress.DepartureCount.window_end', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='count', full_name='com.dxc.ptinsight.egress.DepartureCount.count', index=3,
      number=4, type=5, cpp_type=1, label=1,
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
  serialized_start=325,
  serialized_end=508,
)


_VEHICLECOUNT = _descriptor.Descriptor(
  name='VehicleCount',
  full_name='com.dxc.ptinsight.egress.VehicleCount',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='h3index', full_name='com.dxc.ptinsight.egress.VehicleCount.h3index', index=0,
      number=1, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='count', full_name='com.dxc.ptinsight.egress.VehicleCount.count', index=1,
      number=2, type=5, cpp_type=1, label=1,
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
  serialized_start=510,
  serialized_end=556,
)

_ARRIVALCOUNT.fields_by_name['vehicle_type'].enum_type = ptinsight_dot_common_dot_proto_dot_base__pb2._VEHICLETYPE
_ARRIVALCOUNT.fields_by_name['window_start'].message_type = google_dot_protobuf_dot_timestamp__pb2._TIMESTAMP
_ARRIVALCOUNT.fields_by_name['window_end'].message_type = google_dot_protobuf_dot_timestamp__pb2._TIMESTAMP
_DEPARTURECOUNT.fields_by_name['vehicle_type'].enum_type = ptinsight_dot_common_dot_proto_dot_base__pb2._VEHICLETYPE
_DEPARTURECOUNT.fields_by_name['window_start'].message_type = google_dot_protobuf_dot_timestamp__pb2._TIMESTAMP
_DEPARTURECOUNT.fields_by_name['window_end'].message_type = google_dot_protobuf_dot_timestamp__pb2._TIMESTAMP
DESCRIPTOR.message_types_by_name['ArrivalCount'] = _ARRIVALCOUNT
DESCRIPTOR.message_types_by_name['DepartureCount'] = _DEPARTURECOUNT
DESCRIPTOR.message_types_by_name['VehicleCount'] = _VEHICLECOUNT
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

ArrivalCount = _reflection.GeneratedProtocolMessageType('ArrivalCount', (_message.Message,), {
  'DESCRIPTOR' : _ARRIVALCOUNT,
  '__module__' : 'ptinsight.common.proto.egress.counts_pb2'
  # @@protoc_insertion_point(class_scope:com.dxc.ptinsight.egress.ArrivalCount)
  })
_sym_db.RegisterMessage(ArrivalCount)

DepartureCount = _reflection.GeneratedProtocolMessageType('DepartureCount', (_message.Message,), {
  'DESCRIPTOR' : _DEPARTURECOUNT,
  '__module__' : 'ptinsight.common.proto.egress.counts_pb2'
  # @@protoc_insertion_point(class_scope:com.dxc.ptinsight.egress.DepartureCount)
  })
_sym_db.RegisterMessage(DepartureCount)

VehicleCount = _reflection.GeneratedProtocolMessageType('VehicleCount', (_message.Message,), {
  'DESCRIPTOR' : _VEHICLECOUNT,
  '__module__' : 'ptinsight.common.proto.egress.counts_pb2'
  # @@protoc_insertion_point(class_scope:com.dxc.ptinsight.egress.VehicleCount)
  })
_sym_db.RegisterMessage(VehicleCount)


DESCRIPTOR._options = None
# @@protoc_insertion_point(module_scope)
