terraform {
  required_providers {
    keycloak = {
      source = "mrparkers/keycloak"
      version = ">= 2.0.0"
    }
  }
}

provider "keycloak" {
  client_id = "admin-cli"
  username = "admin"
  password = "admin"
  url = var.keycloak_url
}
