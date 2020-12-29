# keycloak-experiment

Things to pay attention to:

1. Client's `Valid Redirect URIs` should be: `http://localhost:4200/` and `http://localhost:4200/silent-refresh.html`
2. Client's `Web Origins` should be `*`
3. Realm's Login / User registration should be `on` to allow registration
4. Realm's Tokens SSO `Session Max` is set 10 hours by default.

### TODO

1. Figure out how to deploy the realm, client and users to Keycloak (see https://hub.docker.com/r/jboss/keycloak/)
2. Figure out how to sign in with Google
3. Figure out how to configure email messages (maybe use some dummy SMTP server that just logs the stuff)   
4. Figure out how to embed sign in, register, etc pages to the app itself
5. Figure out how to build a custom theme
6. Figure out how to use with Mysql (create DB, etc)
7. Handle the situation when Keycloak user session expires and the app fails to refresh silently.
8. Add Sign In page, add home page, add guards, handle "session_terminated" properly.
