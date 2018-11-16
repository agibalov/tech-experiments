# angular-couchdb-experiment

1. `docker-compose up` to start CouchDB
2. Give it some time to initialize and then run `init-couchdb.sh` to create the system tables.
3. `npm start` to start the app
4. In browser A go to `http://localhost:4200`
5. In browser B (or incognito window of browser A) go there as well
6. See how they synchronize
