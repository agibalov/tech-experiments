resource "keycloak_realm" "realm" {
  realm = "dummy2"
  registration_allowed = true
  sso_session_max_lifespan = "3m"
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