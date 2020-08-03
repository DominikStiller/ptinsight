output "latencytracker_hosts" {
    value = [aws_instance.latencytracker.public_ip]
}
