output "issuer_uri" {
  value = format("%s/auth/realms/%s", var.keycloak_url, keycloak_realm.realm.realm)
}

output "app_client_id" {
  value = keycloak_openid_client.app.client_id
}
