output "kafka_hosts" {
    value = module.kafka.kafka_hosts
}
output "flink_master_hosts" {
    value = module.flink.flink_master_hosts
}
output "flink_worker_hosts" {
    value = module.flink.flink_worker_hosts
}
output "ingest_hosts" {
    value = module.ingest.ingest_hosts
}
output "ui_hosts" {
    value = module.ui.ui_hosts
}
output "latencytracker_hosts" {
    value = module.latencytracker.latencytracker_hosts
}
