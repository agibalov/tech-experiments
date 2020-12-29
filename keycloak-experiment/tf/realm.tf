resource "keycloak_realm" "realm" {
  realm = "dummy2"
  registration_allowed = true
  reset_password_allowed = true
  sso_session_max_lifespan = "3m"

  smtp_server {
    host = "smtp"
    port = 1025
    from = "example@example.com"
    from_display_name = "The App"
  }
}

resource "keycloak_openid_client" "app" {
  realm_id = keycloak_realm.realm.id
  client_id = "app"
  access_type = "PUBLIC"
  standard_flow_enabled = true
  valid_redirect_uris = [
    "http://localhost:4200/sign-in",
    "http://localhost:4200/silent-refresh.html"
  ]
  web_origins = [ "*" ]
}

resource "keycloak_user" "user" {
  realm_id = keycloak_realm.realm.id
  username = "bob"
  first_name = "Bob"
  last_name = "Bobson"
  email = "bob@example.org"
  email_verified = true

  initial_password {
    value = "qwerty123"
  }
}

resource "keycloak_oidc_google_identity_provider" "google" {
  realm = keycloak_realm.realm.id
  client_id = var.google_client_id
  client_secret = var.google_client_secret
}
