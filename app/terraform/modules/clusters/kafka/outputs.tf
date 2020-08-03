output "kafka_hosts" {
    value = aws_instance.kafka.*.public_ip
}
