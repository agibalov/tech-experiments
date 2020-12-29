# keycloak-experiment

## Prerequisites

* Docker
* Docker Compose  
* Java 11
* NodeJS 12
* Terraform v0.14

## How to run

* (optional) One of the things this hello world demonstrates is "Sign In with Google". If you want to try this, you need to create a Google app first - see "How to create a Google app" below. Once you get the client ID and the client secret, do `export TF_VAR_google_client_id=<id>` and `export TF_VAR_google_client_secret=<secret>` to provide them to Terraform. If you don't do this, it will just use the dummy values.
* `docker-compose up`
* `./tool.sh init && ./tool.sh deploy`
* `cd backend && ./gradlew clean bootRun`
* `cd frontend && yarn && yarn start`

## How to use

* Go to http://localhost:4200 and click "Sign In". You'll get sent to Keycloak at http://localhost:8081
* Sign in with `bob`/`qwerty123`. You'll get sent back to the app at http://localhost:4200
* Click "Test" to make an API call. See backend logs for authentication details.
* Click "Refresh" to force ID token refresh.
* Click "Sign Out" to sign out - the app will send you to the Sign In page.
* Or just wait 3 minutes for SSO session to expire - the app will send you to the Sign In page.
* You can try to register or reset your password. The fake mail server is at http://localhost:8082

## Notes

* The realm's "SSO Session Max" is the amount of time for users to stay authenticated with Keycloak. The "silent refresh" relies on this. If you set this to 3 minutes, silent refresh will only be able to get fresh tokens during the period of 3 minutes. So looks like this needs to be set to 1 month or something like this. The default is only 10 hours.

## How to create a Google app

1. Go to https://console.developers.google.com/ and create a new project
2. "Configure consent screen". User Type: External
3. App name: `My App`
4. User support email: your valid email
5. Developer contact information: your valid email
6. "Save and continue"
7. "Add or remove scopes" - add: `userinfo.email`, `userinfo.profile`, `openid`
8. "Save and continue"
9. "Add users" - add yourself
10. "Save and continue"
11. Go to "Credentials" and "Create Credentials" of type "OAuth client ID"
12. Application type: `Web application`
13. Name: `Keycloak`
14. Authorized redirect URIs: add `http://localhost:8081/auth/realms/dummy2/broker/google/endpoint`
15. It will show you the Client ID and the Client Secret
16. Go to `realm.tf` and put these instead of placeholders

### TODO

1. Figure out how to embed sign in, register, etc pages to the app itself
2. Figure out how to use with Mysql (create DB, etc)
