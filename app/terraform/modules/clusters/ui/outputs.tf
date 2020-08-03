output "ui_hosts" {
    value = [aws_instance.ui.public_ip]
}
