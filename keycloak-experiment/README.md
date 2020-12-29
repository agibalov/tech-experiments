# keycloak-experiment

## Prerequisites

* Docker
* Docker Compose  
* Java 11
* NodeJS 12
* Terraform v0.14

## How to run

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

## Notes

* The realm's "SSO Session Max" is the amount of time for users to stay authenticated with Keycloak. The "silent refresh" relies on this. If you set this to 3 minutes, silent refresh will only be able to get fresh tokens during the period of 3 minutes. So looks like this needs to be set to 1 month or something like this. The default is only 10 hours.

### TODO

1. Figure out how to sign in with Google
2. Figure out how to configure email messages (maybe use some dummy SMTP server that just logs the stuff)   
3. Figure out how to embed sign in, register, etc pages to the app itself
4. Figure out how to build a custom theme
5. Figure out how to use with Mysql (create DB, etc)
