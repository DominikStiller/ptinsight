output "visualization_hosts" {
    value = [aws_instance.visualization.public_ip]
}
