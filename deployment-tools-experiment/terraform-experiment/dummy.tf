variable "something" {
  description = "Just some variable"
  default = "some default value"
}

# A "managed" file - TF knows how to create and delete them
resource "local_file" "somefile" {
  content = "hello there! something is '${var.something}'"
  filename = "${path.module}/1.txt"
}

# An "unmanaged" file
resource "null_resource" "somefile2" {
  provisioner "local-exec" {
    when = "create"
    command = "echo 'hello world' > 123.txt"
  }

  provisioner "local-exec" {
    when = "destroy"
    command = "rm 123.txt"
  }
}

output "some_output" {
  value = "hi there"
}
