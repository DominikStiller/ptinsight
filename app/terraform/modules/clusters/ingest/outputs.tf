output "ingest_hosts" {
    value = [aws_instance.ingest.public_ip]
}
