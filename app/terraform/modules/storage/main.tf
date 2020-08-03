resource "aws_s3_bucket" "flink" {
  bucket = "${var.prefix}flink"
  acl    = "private"
  force_destroy = true
}
