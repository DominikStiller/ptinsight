output "kafka_hosts" {
    value = module.kafka.kafka_hosts
}
output "flink_master_hosts" {
    value = module.flink.flink_master_hosts
}
output "flink_worker_hosts" {
    value = module.flink.flink_worker_hosts
}
output "ingestion_hosts" {
    value = module.ingestion.ingestion_hosts
}
output "visualization_hosts" {
    value = module.visualization.visualization_hosts
}
output "latencytracker_hosts" {
    value = module.latencytracker.latencytracker_hosts
}
