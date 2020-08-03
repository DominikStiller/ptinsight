output "flink_master_hosts" {
    value = aws_instance.flink_master.*.public_ip
}
output "flink_worker_hosts" {
    value = aws_instance.flink_worker.*.public_ip
}
