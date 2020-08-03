resource "aws_vpc" "main" {
    cidr_block           = "10.0.0.0/16"
    enable_dns_hostnames = true

    tags = {
        Name = "${var.prefix}main"
    }
}

resource "aws_subnet" "main" {
    vpc_id                  = aws_vpc.main.id
    cidr_block              = "10.0.0.0/24"
    map_public_ip_on_launch = true

    tags = {
        Name = "${var.prefix}main"
    }
}

resource "aws_internet_gateway" "main" {
    vpc_id = aws_vpc.main.id

    tags = {
        Name = "${var.prefix}main"
    }
}

resource "aws_default_route_table" "main" {
    default_route_table_id = aws_vpc.main.default_route_table_id

    route {
        cidr_block = "0.0.0.0/0"
        gateway_id = aws_internet_gateway.main.id
    }

    tags = {
        Name = "${var.prefix}main"
    }
}
