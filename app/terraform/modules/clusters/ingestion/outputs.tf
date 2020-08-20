output "ingestion_hosts" {
    value = [aws_instance.ingestion.public_ip]
}
